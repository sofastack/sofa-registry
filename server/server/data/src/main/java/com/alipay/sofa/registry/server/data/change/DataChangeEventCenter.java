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

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataPushRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author qian.lqlq
 * @version $Id: DataChangeEventCenter.java, v 0.1 2018-03-09 14:25 qian.lqlq Exp $
 */
public final class DataChangeEventCenter {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataChangeEventCenter.class);

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private DatumCache datumCache;

  @Autowired private SessionServerConnectionFactory sessionServerConnectionFactory;

  @Autowired private Exchange boltExchange;

  private final Map<String, Set<String>> dataCenter2Changes = Maps.newConcurrentMap();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final LinkedList<ChangeNotifierRetry> retryNotifiers = Lists.newLinkedList();

  private final Map<String, Map<String, Datum>> dataCenter2TempChanges = Maps.newConcurrentMap();
  private final ReadWriteLock tempLock = new ReentrantReadWriteLock();

  private TempChangeMerger tempChangeMerger;
  private ChangeMerger changeMerger;

  private KeyedThreadPoolExecutor notifyExecutor;
  private KeyedThreadPoolExecutor notifyTempExecutor;

  @PostConstruct
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
            dataServerConfig.getNotifyExecutorQueueSize());

    this.changeMerger = new ChangeMerger();
    this.tempChangeMerger = new TempChangeMerger();

    ConcurrentUtils.createDaemonThread("changeMerger", changeMerger).start();
    ConcurrentUtils.createDaemonThread("tempChangeMerger", tempChangeMerger).start();

    LOGGER.info("start DataChange NotifyIntervalMs={}", dataServerConfig.getNotifyIntervalMillis());
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
    final Connection connection;
    final Datum datum;

    TempNotifier(Connection connection, Datum datum) {
      this.connection = connection;
      this.datum = datum;
    }

    @Override
    public void run() {
      try {
        if (!connection.isFine()) {
          CHANGETEMP_FAIL_COUNTER.inc();
          LOGGER.info(
              "temp change notify failed, conn is closed, {}", connection.getRemoteAddress());
          return;
        }
        notifyTempPub(connection, datum);
        CHANGETEMP_SUCCESS_COUNTER.inc();
      } catch (Throwable e) {
        CHANGETEMP_FAIL_COUNTER.inc();
        LOGGER.error("failed to notify temp {}, {}", connection.getRemoteAddress(), datum, e);
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

  private final class ChangeNotifier implements Runnable {
    final Connection connection;
    final String dataCenter;
    final Map<String, DatumVersion> dataInfoIds;
    volatile int retryCount;

    public ChangeNotifier(
        Connection connection, String dataCenter, Map<String, DatumVersion> dataInfoIds) {
      this.dataCenter = dataCenter;
      this.connection = connection;
      this.dataInfoIds = dataInfoIds;
    }

    @Override
    public void run() {
      try {
        if (!connection.isFine()) {
          CHANGE_FAIL_COUNTER.inc();
          LOGGER.info("change notify failed, conn is closed, {}", connection.getRemoteAddress());
          return;
        }
        DataChangeRequest request = new DataChangeRequest(dataCenter, dataInfoIds);
        doNotify(request, connection);
        CHANGE_SUCCESS_COUNTER.inc();
      } catch (Throwable e) {
        CHANGE_FAIL_COUNTER.inc();
        LOGGER.error("failed to notify {}", this, e);
        retry(this);
      }
    }

    @Override
    public String toString() {
      return "ChangeNotifier{"
          + "connection="
          + connection
          + ", dataCenter='"
          + dataCenter
          + '\''
          + ", dataInfoIds="
          + dataInfoIds.size()
          + ", retryCount="
          + retryCount
          + '}';
    }
  }

  private void retry(ChangeNotifier notifier) {
    notifier.retryCount++;
    if (notifier.retryCount < dataServerConfig.getNotifyRetryTimes()) {
      if (commitRetry(notifier)) {
        CHANGE_RETRY_COUNTER.inc();
        return;
      }
    }
    CHANGE_SKIP_COUNTER.inc();
    LOGGER.warn("skip retry of full, {}", notifier);
  }

  private boolean commitRetry(ChangeNotifier retry) {
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

  private List<ChangeNotifier> getExpires() {
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

  private void notifyTempPub(Connection connection, Datum datum) {
    // has temp pub, need to update the datum.version, we use the cache.datum.version as
    // push.version
    final DatumVersion v = datumCache.updateVersion(datum.getDataCenter(), datum.getDataInfoId());
    if (v == null) {
      LOGGER.warn("not owns the DataInfoId when temp pub,{}", datum.getDataInfoId());
      return;
    }
    Datum existDatum = datumCache.get(datum.getDataCenter(), datum.getDataInfoId());
    if (existDatum != null) {
      datum.addPublishers(existDatum.getPubMap());
    }
    datum.setVersion(v.getValue());
    DataPushRequest request = new DataPushRequest(datum);
    LOGGER.info("temp pub, {}", datum);
    doNotify(request, connection);
  }

  private void doNotify(Object request, Connection connection) {
    Server sessionServer = boltExchange.getServer(dataServerConfig.getPort());
    sessionServer.sendSync(
        sessionServer.getChannel(connection.getRemoteAddress()),
        request,
        dataServerConfig.getRpcTimeoutMillis());
  }

  private final class TempChangeMerger extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      final List<DataTempChangeEvent> events = Lists.newArrayList();
      tempLock.writeLock().lock();
      try {
        for (Map<String, Datum> change : dataCenter2TempChanges.values()) {
          change
              .values()
              .forEach(
                  d -> {
                    events.add(new DataTempChangeEvent(d));
                  });
          change.clear();
        }
      } finally {
        tempLock.writeLock().unlock();
      }
      if (events.isEmpty()) {
        return;
      }
      final List<Connection> connections = sessionServerConnectionFactory.getSessionConnections();
      if (connections.isEmpty()) {
        LOGGER.warn("session conn is empty when temp change");
        return;
      }
      for (Connection connection : connections) {
        for (DataTempChangeEvent event : events) {
          Datum datum = event.getDatum();
          try {
            // group by connect && dataInfoId
            notifyTempExecutor.execute(
                Tuple.of(datum.getDataInfoId(), connection.getRemoteAddress()),
                new TempNotifier(connection, datum));
            CHANGETEMP_COMMIT_COUNTER.inc();
          } catch (RejectedExecutionException e) {
            CHANGETEMP_SKIP_COUNTER.inc();
            LOGGER.warn("commit notify temp full, {}, {}", datum, e.getMessage());
          } catch (Throwable e) {
            CHANGETEMP_SKIP_COUNTER.inc();
            LOGGER.error("commit notify temp failed, {}", datum, e);
          }
        }
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          dataServerConfig.getNotifyTempDataIntervalMillis(), TimeUnit.MILLISECONDS);
    }
  }

  private final class ChangeMerger extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      final List<DataChangeEvent> events = Lists.newArrayList();
      final int maxItems = dataServerConfig.getNotifyMaxItems();
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

      if (!events.isEmpty()) {
        final List<Connection> connections = sessionServerConnectionFactory.getSessionConnections();
        if (connections.isEmpty()) {
          LOGGER.error("session conn is empty when change");
          return;
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
          for (Connection connection : connections) {
            // group by connection
            try {
              notifyExecutor.execute(
                  connection.getRemoteAddress(),
                  new ChangeNotifier(connection, event.getDataCenter(), changes));
              CHANGE_COMMIT_COUNTER.inc();
            } catch (RejectedExecutionException e) {
              CHANGE_SKIP_COUNTER.inc();
              LOGGER.warn("commit notify full, {}, {}", connection, changes.size(), e.getMessage());
            } catch (Throwable e) {
              CHANGE_SKIP_COUNTER.inc();
              LOGGER.error("commit notify failed, {}, {}", connection, changes.size(), e);
            }
          }
        }
      }

      final List<ChangeNotifier> retries = getExpires();
      // commit retry
      for (ChangeNotifier retry : retries) {
        notifyExecutor.execute(retry.connection.getRemoteAddress(), retry);
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          dataServerConfig.getNotifyIntervalMillis(), TimeUnit.MILLISECONDS);
    }
  }

  @VisibleForTesting
  public Set<String> getOnChanges(String dataCenter) {
    Set<String> changes = dataCenter2Changes.get(dataCenter);
    return changes == null ? Collections.emptySet() : Sets.newHashSet(changes);
  }
}
