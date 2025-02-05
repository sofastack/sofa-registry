/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.data.change;

import static com.alipay.sofa.registry.server.data.change.ChangeMetrics.*;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.TraceTimes;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataPushRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.task.FastRejectedExecutionException;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.CollectionUtils;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author qian.lqlq
 * @version $Id: DataChangeEventCenter.java, v 0.1 2018-03-09 14:25 qian.lqlq Exp $
 */
public class DataChangeEventCenter {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataChangeEventCenter.class);

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private MultiClusterDataServerConfig multiClusterDataServerConfig;

  @Autowired private DatumStorageDelegate datumStorageDelegate;

  @Autowired private Exchange boltExchange;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  private final Map<String, DataChangeMerger> dataCenter2Changes = Maps.newConcurrentMap();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final LinkedList<ChangeNotifierRetry> retryNotifiers = Lists.newLinkedList();

  private final Map<String, Map<String, Datum>> dataCenter2TempChanges = Maps.newConcurrentMap();
  private final ReadWriteLock tempLock = new ReentrantReadWriteLock();

  private final TempChangeMerger tempChangeMerger = new TempChangeMerger();
  private final ChangeMerger changeMerger = new ChangeMerger();

  private KeyedThreadPoolExecutor notifyExecutor;
  private KeyedThreadPoolExecutor notifyTempExecutor;

  public void init() {
    this.notifyExecutor =
        new KeyedThreadPoolExecutor(
            "notify",
            dataServerConfig.getNotifyExecutorPoolSize(),
            dataServerConfig.getNotifyExecutorQueueSize());
    this.notifyTempExecutor =
        new KeyedThreadPoolExecutor(
            "notifyTemp",
            dataServerConfig.getNotifyTempExecutorPoolSize(),
            dataServerConfig.getNotifyTempExecutorQueueSize());

    ConcurrentUtils.createDaemonThread("changeMerger", changeMerger).start();
    ConcurrentUtils.createDaemonThread("tempChangeMerger", tempChangeMerger).start();
    LOGGER.info(
        "start DataChange NotifyIntervalMs={}, NotifyTempIntervalMs={}",
        dataServerConfig.getNotifyIntervalMillis(),
        dataServerConfig.getNotifyTempDataIntervalMillis());
  }

  public void onTempPubChange(Publisher publisher, String dataCenter) {
    Map<String, Datum> changes =
        dataCenter2TempChanges.computeIfAbsent(dataCenter, k -> Maps.newConcurrentMap());
    tempLock.readLock().lock();
    try {
      Datum existing =
          changes.computeIfAbsent(publisher.getDataInfoId(), k -> new Datum(publisher, dataCenter));
      existing.addPublisher(publisher);
    } finally {
      tempLock.readLock().unlock();
    }
  }

  public void onChange(
      Collection<String> dataInfoIds, DataChangeType dataChangeType, String dataCenter) {
    if (dataInfoIds.isEmpty()) {
      return;
    }
    DataChangeMerger changes =
        dataCenter2Changes.computeIfAbsent(dataCenter, k -> new DataChangeMerger());
    lock.readLock().lock();
    try {
      changes.addChanges(dataInfoIds, dataChangeType);
    } finally {
      lock.readLock().unlock();
    }
  }

  final class TempNotifier implements Runnable {
    final Channel channel;
    final Datum datum;

    TempNotifier(Channel channel, Datum datum) {
      this.channel = channel;
      this.datum = datum;
    }

    @Override
    public void run() {
      try {
        if (!channel.isConnected()) {
          CHANGETEMP_FAIL_COUNTER.inc();
          LOGGER.info("temp change notify failed, conn is closed, {}", channel);
          return;
        }
        notifyTempPub(channel, datum);
        CHANGETEMP_SUCCESS_COUNTER.inc();
      } catch (Throwable e) {
        CHANGETEMP_FAIL_COUNTER.inc();
        LOGGER.error("failed to notify temp {}, {}", channel, datum, e);
      }
    }
  }

  private final class ChangeNotifierRetry {
    final ChangeNotifier notifier;
    final long expireTimestamp;

    ChangeNotifierRetry(ChangeNotifier notifier, long expireTimestamp) {
      this.notifier = notifier;
      this.expireTimestamp = expireTimestamp;
    }
  }

  final class ChangeNotifier implements Runnable {
    final Channel channel;
    final int notifyPort;
    final String dataCenter;
    final Map<String, DatumVersion> dataInfoIds;
    final TraceTimes times;

    volatile int retryCount;

    private ChangeNotifier(
        Channel channel,
        int notifyPort,
        String dataCenter,
        Map<String, DatumVersion> dataInfoIds,
        TraceTimes parentTimes) {
      this.dataCenter = dataCenter;
      this.channel = channel;
      this.notifyPort = notifyPort;
      this.dataInfoIds = dataInfoIds;
      this.times = parentTimes.copy();
      this.times.setDatumNotifyCreate(System.currentTimeMillis());
    }

    @Override
    public void run() {
      try {
        if (!channel.isConnected()) {
          CHANGE_FAIL_COUNTER.inc();
          LOGGER.info("change notify failed, conn is closed, {}", channel);
          return;
        }
        DataChangeRequest request = new DataChangeRequest(dataCenter, dataInfoIds, times);
        request.getTimes().setDatumNotifySend(System.currentTimeMillis());
        doNotify(request, channel, notifyPort);
        LOGGER.info("success to notify {}, {}", channel.getRemoteAddress(), this);
        CHANGE_SUCCESS_COUNTER.inc();
      } catch (Throwable e) {
        CHANGE_FAIL_COUNTER.inc();
        LOGGER.error("failed to notify {}, {}", channel, this, e);
        retry(this);
      }
    }

    int size() {
      int size = 0;
      for (String dataInfoIds : dataInfoIds.keySet()) {
        size += dataInfoIds.length();
      }
      return size;
    }

    @Override
    public String toString() {
      return StringFormatter.format(
          "ChangeNotifier{{},notifyPort={},num={},size={},retry={},traceTimes={}}",
          dataCenter,
          notifyPort,
          dataInfoIds.size(),
          size(),
          retryCount,
          times.format(System.currentTimeMillis()));
    }
  }

  private void retry(ChangeNotifier notifier) {
    notifier.retryCount++;
    if (notifier.retryCount <= dataServerConfig.getNotifyRetryTimes()) {
      if (commitRetry(notifier)) {
        CHANGE_RETRY_COUNTER.inc();
        return;
      }
    }
    CHANGE_SKIP_COUNTER.inc();
    LOGGER.warn("skip retry of full, {}", notifier);
  }

  boolean commitRetry(ChangeNotifier retry) {
    final int maxSize = dataServerConfig.getNotifyRetryQueueSize();
    final long expireTimestamp =
        System.currentTimeMillis() + dataServerConfig.getNotifyRetryBackoffMillis();
    synchronized (retryNotifiers) {
      if (retryNotifiers.size() >= maxSize) {
        // remove first
        retryNotifiers.removeFirst();
      }
      retryNotifiers.add(new ChangeNotifierRetry(retry, expireTimestamp));
    }
    return true;
  }

  List<ChangeNotifier> getExpires() {
    final List<ChangeNotifier> expires = Lists.newLinkedList();
    final long now = System.currentTimeMillis();
    synchronized (retryNotifiers) {
      final Iterator<ChangeNotifierRetry> it = retryNotifiers.iterator();
      while (it.hasNext()) {
        ChangeNotifierRetry retry = it.next();
        if (retry.expireTimestamp <= now) {
          expires.add(retry.notifier);
          it.remove();
        }
      }
    }
    return expires;
  }

  private void notifyTempPub(Channel channel, Datum datum) {
    // has temp pub, need to update the datum.version, we use the cache.datum.version as
    // push.version
    final DatumVersion v =
        datumStorageDelegate.updateVersion(datum.getDataCenter(), datum.getDataInfoId());
    if (v == null) {
      LOGGER.warn("not owns the DataInfoId when temp pub to {},{}", channel, datum.getDataInfoId());
      return;
    }
    Datum existDatum = datumStorageDelegate.get(datum.getDataCenter(), datum.getDataInfoId());
    if (existDatum != null) {
      datum.addPublishers(existDatum.getPubMap());
    }
    datum.setVersion(v.getValue());
    SubDatum subDatum = DatumUtils.of(datum);
    DataPushRequest request = new DataPushRequest(subDatum);
    LOGGER.info("temp pub to {}, {}", channel, subDatum);
    doNotify(request, channel, dataServerConfig.getNotifyPort());
  }

  private void doNotify(Object request, Channel channel, int notifyPort) {
    Server server = boltExchange.getServer(notifyPort);
    server.sendSync(channel, request, dataServerConfig.getRpcTimeoutMillis());
  }

  boolean handleTempChanges(List<Channel> channels) {
    // first clean the event
    List<Datum> datums = Lists.newArrayList();
    tempLock.writeLock().lock();
    try {
      for (Map<String, Datum> change : dataCenter2TempChanges.values()) {
        datums.addAll(change.values());
        change.clear();
      }
    } finally {
      tempLock.writeLock().unlock();
    }
    if (datums.isEmpty()) {
      return false;
    }
    if (channels.isEmpty()) {
      LOGGER.warn("session conn is empty when temp change");
      return false;
    }
    for (Datum datum : datums) {
      for (Channel channel : channels) {
        try {
          // group by connect && dataInfoId
          notifyTempExecutor.execute(
              Tuple.of(datum.getDataInfoId(), channel.getRemoteAddress()),
              new TempNotifier(channel, datum));
          CHANGETEMP_COMMIT_COUNTER.inc();
        } catch (FastRejectedExecutionException e) {
          CHANGETEMP_SKIP_COUNTER.inc();
          LOGGER.warn("commit notify temp full, {}, {}, {}", channel, datum, e.getMessage());
        } catch (Throwable e) {
          CHANGETEMP_SKIP_COUNTER.inc();
          LOGGER.error("commit notify temp failed, {}, {}", channel, datum, e);
        }
      }
    }
    return true;
  }

  private final class TempChangeMerger extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      try {
        Server server = boltExchange.getServer(dataServerConfig.getNotifyPort());
        Map<String, Channel> channelMap = server.selectAvailableChannelsForHostAddress();
        handleTempChanges(Lists.newArrayList(channelMap.values()));
      } catch (Throwable e) {
        LOGGER.error("failed to merge temp change", e);
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          dataServerConfig.getNotifyTempDataIntervalMillis(), TimeUnit.MILLISECONDS);
    }
  }

  boolean handleChanges(
      List<DataChangeEvent> events,
      NodeType nodeType,
      int notifyPort,
      boolean errorWhenChannelEmpty) {

    if (org.springframework.util.CollectionUtils.isEmpty(events)) {
      return false;
    }

    Server server = boltExchange.getServer(notifyPort);
    Map<String, List<Channel>> channelsMap = server.selectAllAvailableChannelsForHostAddress();

    if (channelsMap.isEmpty()) {
      if (errorWhenChannelEmpty) {
        LOGGER.error("{} conn is empty when change", nodeType);
      }
      return false;
    }
    for (DataChangeEvent event : events) {
      String dataCenter = event.getDataCenter();
      if (nodeType == NodeType.DATA) {
        if (dataServerConfig.isLocalDataCenter(dataCenter)) {
          dataCenter = defaultCommonConfig.getDefaultClusterId();
          LOGGER.info(
              "[Notify]dataCenter={}, dataInfoIds={} notify local dataChange to remote.",
              dataCenter,
              event.getDataInfoIds());
        } else {
          LOGGER.info(
              "[skip]dataCenter={}, dataInfoIds={} change skip to notify remote data.",
              dataCenter,
              event.getDataInfoIds());
          continue;
        }
      }

      final Map<String, DatumVersion> changes =
          Maps.newHashMapWithExpectedSize(event.getDataInfoIds().size());
      for (String dataInfoId : event.getDataInfoIds()) {
        DatumVersion datumVersion =
            datumStorageDelegate.getVersion(event.getDataCenter(), dataInfoId);
        if (datumVersion != null) {
          changes.put(dataInfoId, datumVersion);
        }
      }
      if (changes.isEmpty()) {
        continue;
      }
      for (Map.Entry<String, DatumVersion> entry : changes.entrySet()) {
        LOGGER.info("datum change notify: {},{}", entry.getKey(), entry.getValue());
      }
      for (Map.Entry<String, List<Channel>> entry : channelsMap.entrySet()) {
        Channel channel = CollectionUtils.getRandom(entry.getValue());
        try {
          notifyExecutor.execute(
              channel.getRemoteAddress(),
              new ChangeNotifier(channel, notifyPort, dataCenter, changes, event.getTraceTimes()));
          CHANGE_COMMIT_COUNTER.inc();
        } catch (FastRejectedExecutionException e) {
          CHANGE_SKIP_COUNTER.inc();
          LOGGER.warn("commit notify full, {}, {}, {}", channel, changes.size(), e.getMessage());
        } catch (Throwable e) {
          CHANGE_SKIP_COUNTER.inc();
          LOGGER.error("commit notify failed, {}, {}", channel, changes.size(), e);
        }
      }
    }
    return true;
  }

  void handleExpire() {
    final List<ChangeNotifier> retries = getExpires();
    // commit retry
    for (ChangeNotifier retry : retries) {
      try {
        notifyExecutor.execute(retry.channel.getRemoteAddress(), retry);
        CHANGE_COMMIT_COUNTER.inc();
      } catch (FastRejectedExecutionException e) {
        CHANGE_SKIP_COUNTER.inc();
        LOGGER.warn(
            "commit retry notify full, {}, {}, {}",
            retry.channel,
            retry.dataInfoIds.size(),
            e.getMessage());
      } catch (Throwable e) {
        CHANGE_SKIP_COUNTER.inc();
        LOGGER.error(
            "commit retry notify failed, {}, {}", retry.channel, retry.dataInfoIds.size(), e);
      }
    }
  }

  List<DataChangeEvent> transferChangeEvent(int maxItems) {
    final List<DataChangeEvent> events = Lists.newArrayList();
    lock.writeLock().lock();
    try {
      for (Map.Entry<String, DataChangeMerger> change : dataCenter2Changes.entrySet()) {
        final String dataCenter = change.getKey();
        DataChangeMerger merger = change.getValue();
        TraceTimes traceTimes = merger.createTraceTime();
        List<List<String>> parts =
            Lists.partition(Lists.newArrayList(merger.getDataInfoIds()), maxItems);
        for (List<String> part : parts) {
          events.add(new DataChangeEvent(dataCenter, part, traceTimes));
        }
        merger.clear();
      }
    } finally {
      lock.writeLock().unlock();
    }
    return events;
  }

  private final class ChangeMerger extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      try {
        // first clean the event
        final int maxItems = dataServerConfig.getNotifyMaxItems();
        final List<DataChangeEvent> events = transferChangeEvent(maxItems);

        // notify local session
        handleChanges(events, NodeType.SESSION, dataServerConfig.getNotifyPort(), true);

        // notify remote data
        handleChanges(
            events,
            NodeType.DATA,
            multiClusterDataServerConfig.getSyncRemoteSlotLeaderPort(),
            false);
        handleExpire();
      } catch (Throwable e) {
        LOGGER.error("failed to merge change", e);
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          dataServerConfig.getNotifyIntervalMillis(), TimeUnit.MILLISECONDS);
    }
  }

  @VisibleForTesting
  Set<String> getOnChanges(String dataCenter) {
    DataChangeMerger changes = dataCenter2Changes.get(dataCenter);
    return changes == null ? Collections.emptySet() : Sets.newHashSet(changes.getDataInfoIds());
  }

  @VisibleForTesting
  Map<String, Datum> getOnTempPubChanges(String dataCenter) {
    Map<String, Datum> changes = dataCenter2TempChanges.get(dataCenter);
    return changes == null ? Collections.emptyMap() : Maps.newHashMap(changes);
  }

  @VisibleForTesting
  void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }

  @VisibleForTesting
  void setDatumDelegate(DatumStorageDelegate datumStorageDelegate) {
    this.datumStorageDelegate = datumStorageDelegate;
  }

  @VisibleForTesting
  void setExchange(Exchange boltExchange) {
    this.boltExchange = boltExchange;
  }

  @VisibleForTesting
  ChangeNotifier newChangeNotifier(
      Channel channel, int notifyPort, String dataCenter, Map<String, DatumVersion> dataInfoIds) {
    return new ChangeNotifier(channel, notifyPort, dataCenter, dataInfoIds, new TraceTimes());
  }

  @VisibleForTesting
  TempNotifier newTempNotifier(Channel channel, Datum datum) {
    return new TempNotifier(channel, datum);
  }

  @VisibleForTesting
  void setNotifyExecutor(KeyedThreadPoolExecutor notifyExecutor) {
    this.notifyExecutor = notifyExecutor;
  }

  @VisibleForTesting
  void setNotifyTempExecutor(KeyedThreadPoolExecutor notifyTempExecutor) {
    this.notifyTempExecutor = notifyTempExecutor;
  }

  /**
   * Setter method for property <tt>multiClusterDataServerConfig</tt>.
   *
   * @param multiClusterDataServerConfig value to be assigned to property
   *     multiClusterDataServerConfig
   */
  @VisibleForTesting
  void setMultiClusterDataServerConfig(MultiClusterDataServerConfig multiClusterDataServerConfig) {
    this.multiClusterDataServerConfig = multiClusterDataServerConfig;
  }

  /**
   * Setter method for property <tt>defaultCommonConfig</tt>.
   *
   * @param defaultCommonConfig value to be assigned to property defaultCommonConfig
   */
  @VisibleForTesting
  public void setDefaultCommonConfig(DefaultCommonConfig defaultCommonConfig) {
    this.defaultCommonConfig = defaultCommonConfig;
  }
}
