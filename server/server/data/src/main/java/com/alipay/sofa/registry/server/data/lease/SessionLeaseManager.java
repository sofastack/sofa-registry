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
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.store.ProcessIdCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.CleanContinues;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.DataChangeType;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-30 10:30 yuzhi.lyz Exp $
 */
public final class SessionLeaseManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionLeaseManager.class);

  private static final Logger COMPACT_LOGGER = LoggerFactory.getLogger("COMPACT");

  private static final int MIN_LEASE_SEC = 5;

  @Autowired DataServerConfig dataServerConfig;
  @Resource DatumStorageDelegate datumStorageDelegate;

  @Autowired Exchange boltExchange;

  @Autowired SlotManager slotManager;

  @Autowired MetaServerService metaServerService;

  @Autowired DataChangeEventCenter dataChangeEventCenter;

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

  private List<ProcessId> cleanExpireLease(
      int leaseMs, Set<ProcessId> connProcessIds, Set<ProcessId> metaProcessIds) {
    final long lastRenew = System.currentTimeMillis() - leaseMs;
    Map<ProcessId, Long> expires = Maps.newHashMap();
    for (Map.Entry<ProcessId, Long> e : connectIdRenewTimestampMap.entrySet()) {
      if (e.getValue() < lastRenew) {
        expires.put(e.getKey(), e.getValue());
      }
    }
    if (expires.isEmpty()) {
      return Collections.emptyList();
    }

    LOGGER.info("collectExpire={}, {}", expires.size(), expires);
    List<ProcessId> cleans = Lists.newArrayListWithCapacity(expires.size());
    for (Map.Entry<ProcessId, Long> expire : expires.entrySet()) {
      final ProcessId p = expire.getKey();
      if (connProcessIds.contains(p)) {
        renewSession(p);
        // maybe happens in fullGc or high start scenario
        LOGGER.info("[expireHasConn]{}", expire);
        continue;
      }
      if (metaProcessIds.contains(p)) {
        renewSession(p);
        // maybe happens in fullGc or or high start scenario
        LOGGER.info("[expireHasMeta]{}", expire);
        continue;
      }
      // only remove the expire with the collect timestamp
      // maybe renew happens in the cleaning
      if (connectIdRenewTimestampMap.remove(p, expire.getValue())) {
        LOGGER.info("[cleanExpire]{}", expire);
        cleans.add(p);
      }
    }
    return cleans;
  }

  void cleanStorage() {
    // make sure the existing processId be clean
    Set<ProcessId> stores =
        datumStorageDelegate.getSessionProcessIds(dataServerConfig.getLocalDataCenter());
    final int deadlineMillis = dataServerConfig.getSessionLeaseCleanDeadlineSecs() * 1000;
    for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
      if (slotManager.isFollower(dataServerConfig.getLocalDataCenter(), i)) {
        LOGGER.info("skip clean for follower, slotId={}", i);
        continue;
      }
      if (slotManager.isLeader(dataServerConfig.getLocalDataCenter(), i)) {
        // own the slot and is leader
        for (ProcessId p : stores) {
          if (!connectIdRenewTimestampMap.containsKey(p)) {
            // double check with newly meta.processIds
            long start = System.currentTimeMillis();
            if (metaServerService.getSessionProcessIds().contains(p)) {
              LOGGER.info("[expireHasMeta]{}", p);
              continue;
            }
            LOGGER.info("[cleanSession]{}", p);
            // in fullGC case, the cleaning maybe very slow
            // after double check, the session.processId is insert, but cleanup not know it
            // deadlineTs, ensure that the cleanup ends within the expected time
            final long deadlineTimestamp = start + deadlineMillis;
            CleanLeaseContinues continues = new CleanLeaseContinues(deadlineTimestamp);
            Map<String, DatumVersion> versionMap =
                datumStorageDelegate.cleanBySessionId(
                    dataServerConfig.getLocalDataCenter(), i, p, continues);
            if (!versionMap.isEmpty()) {
              dataChangeEventCenter.onChange(
                  versionMap.keySet(), DataChangeType.LEASE, dataServerConfig.getLocalDataCenter());
              LOGGER.info(
                  "[cleanDatum]broken={},slotId={},datas={},pubs={},span={},{}",
                  continues.broken,
                  i,
                  versionMap.size(),
                  continues.cleanNum,
                  System.currentTimeMillis() - start,
                  p);
            }
          }
        }
      }
    }
  }

  static final class CleanLeaseContinues implements CleanContinues {
    private final long deadlineTimestamp;
    private int cleanNum;
    volatile boolean broken;

    CleanLeaseContinues(long deadlineTimestamp) {
      this.deadlineTimestamp = deadlineTimestamp;
    }

    @Override
    public boolean continues() {
      final long now = System.currentTimeMillis();
      // make sure at lease clean one item
      if (cleanNum != 0 && now > deadlineTimestamp) {
        this.broken = true;
        return false;
      }
      return true;
    }

    @Override
    public void onClean(int num) {
      cleanNum += num;
    }
  }

  private void renewBy(Set<ProcessId> processIds) {
    for (ProcessId processId : processIds) {
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
    LOGGER.info("find processId from channels={}, {}:{}", channels.size(), ret.size(), ret);
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
    // 1. renew the lease by conn and meta
    Set<ProcessId> connProcessIds = getProcessIdsInConnection();
    renewBy(connProcessIds);
    Set<ProcessId> metaProcessIds = metaServerService.getSessionProcessIds();
    renewBy(metaProcessIds);
    // 2. update the processId again, if has full gc,
    connProcessIds = getProcessIdsInConnection();
    metaProcessIds = metaServerService.getSessionProcessIds();

    cleanExpireLease(dataServerConfig.getSessionLeaseSecs() * 1000, connProcessIds, metaProcessIds);
    cleanStorage();

    // compact the unpub
    long tombstoneTimestamp =
        System.currentTimeMillis() - dataServerConfig.getDatumCompactDelaySecs() * 1000;
    Map<String, Integer> compacted =
        datumStorageDelegate.compact(dataServerConfig.getLocalDataCenter(), tombstoneTimestamp);
    COMPACT_LOGGER.info("compact datum, {}", compacted);
  }
}
