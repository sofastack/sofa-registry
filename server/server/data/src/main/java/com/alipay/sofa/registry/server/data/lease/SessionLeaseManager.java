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
package com.alipay.sofa.registry.server.data.lease;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.ProcessIdCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-30 10:30 yuzhi.lyz Exp $
 */
public final class SessionLeaseManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionLeaseManager.class);

  private static final Logger COMPACT_LOGGER = LoggerFactory.getLogger("COMPACT");

  private static final int MIN_LEASE_SEC = 5;

  @Autowired private DataServerConfig dataServerConfig;
  @Autowired private DatumStorage localDatumStorage;

  @Autowired private Exchange boltExchange;

  private final Map<ProcessId, Long> connectIdRenewTimestampMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    validateSessionLeaseSec(dataServerConfig.getSessionLeaseSecs());
    ConcurrentUtils.createDaemonThread("session-lease-cleaner", new LeaseCleaner()).start();
  }

  public static void validateSessionLeaseSec(int sec) {
    if (sec < MIN_LEASE_SEC) {
      throw new IllegalArgumentException(
          String.format("min sessionLeaseSec(%d): %d", MIN_LEASE_SEC, sec));
    }
  }

  public void renewSession(ProcessId sessionProcessId) {
    sessionProcessId = ProcessIdCache.cache(sessionProcessId);
    connectIdRenewTimestampMap.put(sessionProcessId, System.currentTimeMillis());
  }

  boolean contains(ProcessId sessionProcessId) {
    return connectIdRenewTimestampMap.containsKey(sessionProcessId);
  }

  private Map<ProcessId, Date> getExpireProcessId(int leaseMs) {
    Map<ProcessId, Date> expires = Maps.newHashMap();
    final long lastRenew = System.currentTimeMillis() - leaseMs;
    for (Map.Entry<ProcessId, Long> e : connectIdRenewTimestampMap.entrySet()) {
      if (e.getValue() < lastRenew) {
        expires.put(e.getKey(), new Date(e.getValue()));
      }
    }
    return expires;
  }

  private List<ProcessId> cleanExpire(
      Set<ProcessId> connProcessIds, Collection<ProcessId> expires) {
    List<ProcessId> cleans = Lists.newArrayList();
    for (ProcessId p : expires) {
      if (connProcessIds.contains(p)) {
        LOGGER.warn("expire session has conn, {}", p);
        continue;
      }
      connectIdRenewTimestampMap.remove(p);
      cleans.add(p);
    }
    return cleans;
  }

  private void cleanStorage() {
    // make sure the existing processId be clean
    Set<ProcessId> stores = localDatumStorage.getSessionProcessIds();
    for (ProcessId p : stores) {
      if (!connectIdRenewTimestampMap.containsKey(p)) {
        Map<String, DatumVersion> versionMap = localDatumStorage.clean(p);
        LOGGER.info("expire session correct, {}, datums={}", p, versionMap.size());
      }
    }
  }

  private void renewByConnection(Set<ProcessId> connProcessIds) {
    for (ProcessId processId : connProcessIds) {
      renewSession(processId);
    }
  }

  Set<ProcessId> getProcessIdsInConnection() {
    Server server = boltExchange.getServer(dataServerConfig.getPort());
    if (server == null) {
      LOGGER.warn("Server not init when check session lease");
      return Collections.emptySet();
    }
    List<Channel> channels = server.getChannels();
    Set<ProcessId> ret = Sets.newHashSetWithExpectedSize(128);
    for (Channel channel : channels) {
      BoltChannel boltChannel = (BoltChannel) channel;
      ProcessId processId =
          (ProcessId)
              boltChannel.getConnection().getAttribute(ValueConstants.ATTR_RPC_CHANNEL_PROCESS_ID);
      if (processId != null) {
        ret.add(processId);
      }
    }
    return ret;
  }

  private final class LeaseCleaner extends LoopRunnable {
    @Override
    public void runUnthrowable() {
      clean();
    }

    @Override
    public void waitingUnthrowable() {
      // CPU overhead is high
      ConcurrentUtils.sleepUninterruptibly(
          dataServerConfig.getSessionLeaseCheckIntervalSecs(), TimeUnit.SECONDS);
    }
  }

  void clean() {
    Set<ProcessId> processIds = getProcessIdsInConnection();
    renewByConnection(processIds);
    Map<ProcessId, Date> expires =
        getExpireProcessId(dataServerConfig.getSessionLeaseSecs() * 1000);
    LOGGER.info("lease expire sessions, {}", expires);

    if (!expires.isEmpty()) {
      List<ProcessId> cleans = cleanExpire(processIds, expires.keySet());
      LOGGER.info("expire sessions clean, {}", cleans);
    }

    cleanStorage();

    // compact the unpub
    long tombstoneTimestamp =
        System.currentTimeMillis() - dataServerConfig.getDatumCompactDelaySecs() * 1000;
    Map<String, Integer> compacted = localDatumStorage.compact(tombstoneTimestamp);
    COMPACT_LOGGER.info("compact datum, {}", compacted);
  }

  @VisibleForTesting
  void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }

  @VisibleForTesting
  void setLocalDatumStorage(DatumStorage localDatumStorage) {
    this.localDatumStorage = localDatumStorage;
  }

  @VisibleForTesting
  void setExchange(Exchange boltExchange) {
    this.boltExchange = boltExchange;
  }
}
