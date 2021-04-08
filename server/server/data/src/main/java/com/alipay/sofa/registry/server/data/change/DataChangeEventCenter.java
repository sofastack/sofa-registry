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
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author qian.lqlq
 * @version $Id: DataChangeEventCenter.java, v 0.1 2018-03-09 14:25 qian.lqlq Exp $
 */
public final class DataChangeEventCenter {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataChangeEventCenter.class);

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private DatumCache datumCache;

  @Autowired private Exchange boltExchange;

  private final Map<String, Set<String>> dataCenter2Changes = Maps.newConcurrentMap();
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

  public void onChange(Collection<String> dataInfoIds, String dataCenter) {
    Set<String> changes =
        dataCenter2Changes.computeIfAbsent(dataCenter, k -> Sets.newConcurrentHashSet());
    lock.readLock().lock();
    try {
      changes.addAll(dataInfoIds);
    } finally {
      lock.readLock().unlock();
    }
  }

  private final class TempNotifier implements Runnable {
    final Channel channel;
    final Datum datum;

    TempNotifier(Channel connection, Datum datum) {
      this.channel = connection;
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
    final String dataCenter;
    final Map<String, DatumVersion> dataInfoIds;
    volatile int retryCount;

    private ChangeNotifier(
        Channel channel, String dataCenter, Map<String, DatumVersion> dataInfoIds) {
      this.dataCenter = dataCenter;
      this.channel = channel;
      this.dataInfoIds = dataInfoIds;
    }

    @Override
    public void run() {
      try {
        if (!channel.isConnected()) {
          CHANGE_FAIL_COUNTER.inc();
          LOGGER.info("change notify failed, conn is closed, {}", channel);
          return;
        }
        DataChangeRequest request = new DataChangeRequest(dataCenter, dataInfoIds);
        doNotify(request, channel);
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
          "ChangeNotifier{{},num={},size={},retry={}}",
          dataCenter,
          dataInfoIds.size(),
          size(),
          retryCount);
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
    final DatumVersion v = datumCache.updateVersion(datum.getDataCenter(), datum.getDataInfoId());
    if (v == null) {
      LOGGER.warn("not owns the DataInfoId when temp pub to {},{}", channel, datum.getDataInfoId());
      return;
    }
    Datum existDatum = datumCache.get(datum.getDataCenter(), datum.getDataInfoId());
    if (existDatum != null) {
      datum.addPublishers(existDatum.getPubMap());
    }
    datum.setVersion(v.getValue());
    SubDatum subDatum = DatumUtils.of(datum);
    DataPushRequest request = new DataPushRequest(subDatum);
    LOGGER.info("temp pub to {}, {}", channel, subDatum);
    doNotify(request, channel);
  }

  private void doNotify(Object request, Channel channel) {
    Server sessionServer = boltExchange.getServer(dataServerConfig.getNotifyPort());
    sessionServer.sendSync(channel, request, dataServerConfig.getRpcTimeoutMillis());
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
        } catch (RejectedExecutionException e) {
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

  boolean handleChanges(List<Channel> channels) {
    // first clean the event
    final int maxItems = dataServerConfig.getNotifyMaxItems();
    final List<DataChangeEvent> events = transferChangeEvent(maxItems);
    if (events.isEmpty()) {
      return false;
    }
    if (channels.isEmpty()) {
      LOGGER.error("session conn is empty when change");
      return false;
    }
    for (DataChangeEvent event : events) {
      final Map<String, DatumVersion> changes = new HashMap<>(events.size());
      final String dataCenter = event.getDataCenter();
      for (String dataInfoId : event.getDataInfoIds()) {
        DatumVersion datumVersion = datumCache.getVersion(dataCenter, dataInfoId);
        if (datumVersion != null) {
          changes.put(dataInfoId, datumVersion);
        }
      }
      if (changes.isEmpty()) {
        continue;
      }
      for (Channel channel : channels) {
        try {
          notifyExecutor.execute(
              channel.getRemoteAddress(),
              new ChangeNotifier(channel, event.getDataCenter(), changes));
          CHANGE_COMMIT_COUNTER.inc();
        } catch (RejectedExecutionException e) {
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
      } catch (RejectedExecutionException e) {
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
      for (Map.Entry<String, Set<String>> change : dataCenter2Changes.entrySet()) {
        final String dataCenter = change.getKey();
        List<String> dataInfoIds = Lists.newArrayList(change.getValue());
        change.getValue().clear();
        List<List<String>> parts = Lists.partition(dataInfoIds, maxItems);
        for (int i = 0; i < parts.size(); i++) {
          events.add(new DataChangeEvent(dataCenter, parts.get(i)));
        }
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
        Server server = boltExchange.getServer(dataServerConfig.getNotifyPort());
        Map<String, Channel> channelMap = server.selectAvailableChannelsForHostAddress();
        handleChanges(Lists.newArrayList(channelMap.values()));
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
    Set<String> changes = dataCenter2Changes.get(dataCenter);
    return changes == null ? Collections.emptySet() : Sets.newHashSet(changes);
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
  void setDatumCache(DatumCache datumCache) {
    this.datumCache = datumCache;
  }

  @VisibleForTesting
  void setBoltExchange(Exchange boltExchange) {
    this.boltExchange = boltExchange;
  }

  @VisibleForTesting
  ChangeNotifier newChangeNotifier(
      Channel channel, String dataCenter, Map<String, DatumVersion> dataInfoIds) {
    return new ChangeNotifier(channel, dataCenter, dataInfoIds);
  }
}
