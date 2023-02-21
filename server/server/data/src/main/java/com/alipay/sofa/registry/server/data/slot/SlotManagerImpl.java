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
package com.alipay.sofa.registry.server.data.slot;

import static com.alipay.sofa.registry.server.data.slot.SlotMetrics.Manager.*;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.DataChangeType;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.pubiterator.DatumBiConsumer;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.alipay.sofa.registry.task.KeyedTask;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.TaskErrorSilenceException;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 09:44 yuzhi.lyz Exp $
 */
public final class SlotManagerImpl implements SlotManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SlotManagerImpl.class);

  private static final Logger MIGRATING_LOGGER = LoggerFactory.getLogger("MIGRATING");

  private static final Logger SYNC_ERROR_LOGGER = LoggerFactory.getLogger("SYNC-ERROR");

  private static final Logger SYNC_DIGEST_LOGGER = LoggerFactory.getLogger("SYNC-DIGEST");

  private static final Logger DIFF_LOGGER = LoggerFactory.getLogger("SYNC-DIFF");

  private final SlotFunction slotFunction = SlotFunctionRegistry.getFunc();

  @Autowired private DataNodeExchanger dataNodeExchanger;

  @Autowired private SessionNodeExchanger sessionNodeExchanger;

  @Autowired private MetaServerServiceImpl metaServerService;

  @Autowired private DataServerConfig dataServerConfig;

  @Resource private DatumStorageDelegate datumStorageDelegate;

  @Autowired private DataChangeEventCenter dataChangeEventCenter;

  @Autowired private SessionLeaseManager sessionLeaseManager;

  @Autowired(required = false)
  private List<SlotTableRecorder> recorders;

  @Resource private SyncSlotAcceptorManager syncSlotAcceptAllManager;

  @Autowired private SlotChangeListenerManager slotChangeListenerManager;

  private KeyedThreadPoolExecutor migrateSessionExecutor;
  private KeyedThreadPoolExecutor syncSessionExecutor;
  private KeyedThreadPoolExecutor syncLeaderExecutor;

  /**
   * the sync and migrating may happen parallelly when slot role has modified. make sure the datum
   * merging is idempotent
   */
  private final SyncingWatchDog watchDog = new SyncingWatchDog();

  private final AtomicReference<SlotTable> updatingSlotTable = new AtomicReference<SlotTable>();
  private final ReadWriteLock updateLock = new ReentrantReadWriteLock();
  private final SlotTableStates slotTableStates = new SlotTableStates();

  @PostConstruct
  public void init() {
    initExecutors();
    ConcurrentUtils.createDaemonThread("SyncingWatchDog", watchDog).start();
  }

  void initExecutors() {
    this.migrateSessionExecutor =
        new KeyedThreadPoolExecutor(
            "migrate-session",
            dataServerConfig.getSlotLeaderSyncSessionExecutorThreadSize(),
            dataServerConfig.getSlotLeaderSyncSessionExecutorQueueSize());

    this.syncSessionExecutor =
        new KeyedThreadPoolExecutor(
            "sync-session",
            dataServerConfig.getSlotLeaderSyncSessionExecutorThreadSize(),
            dataServerConfig.getSlotLeaderSyncSessionExecutorQueueSize());

    this.syncLeaderExecutor =
        new KeyedThreadPoolExecutor(
            "sync-leader",
            dataServerConfig.getSlotFollowerSyncLeaderExecutorThreadSize(),
            dataServerConfig.getSlotFollowerSyncLeaderExecutorQueueSize());
  }

  @Override
  public int slotOf(String dataInfoId) {
    return slotFunction.slotOf(dataInfoId);
  }

  @Override
  public Slot getSlot(String dataCenter, int slotId) {
    final SlotState state = slotTableStates.slotStates.get(slotId);
    return state == null ? null : state.slot;
  }

  @Override
  public SlotAccess checkSlotAccess(
      String dataCenter, int slotId, long srcSlotEpoch, long srcLeaderEpoch) {
    SlotTable currentSlotTable;
    SlotState state;
    updateLock.readLock().lock();
    try {
      currentSlotTable = slotTableStates.table;
      state = slotTableStates.slotStates.get(slotId);
    } finally {
      updateLock.readLock().unlock();
    }

    final long currentEpoch = currentSlotTable.getEpoch();
    if (currentEpoch < srcSlotEpoch) {
      triggerUpdateSlotTable(srcSlotEpoch);
    }
    return checkSlotAccess(slotId, currentEpoch, state, srcLeaderEpoch);
  }

  public SlotAccess checkSlotAccess(
      int slotId, long currentSlotTableEpoch, ISlotState state, long srcLeaderEpoch) {
    if (state == null) {
      return new SlotAccess(slotId, currentSlotTableEpoch, SlotAccess.Status.Moved, -1);
    }
    final Slot slot = state.getSlot();
    if (!localIsLeader(slot)) {
      return new SlotAccess(
          slotId, currentSlotTableEpoch, SlotAccess.Status.Moved, slot.getLeaderEpoch());
    }
    if (!state.isMigrated()) {
      return new SlotAccess(
          slotId, currentSlotTableEpoch, SlotAccess.Status.Migrating, slot.getLeaderEpoch());
    }
    if (slot.getLeaderEpoch() != srcLeaderEpoch) {
      return new SlotAccess(
          slotId, currentSlotTableEpoch, SlotAccess.Status.MisMatch, slot.getLeaderEpoch());
    }
    return new SlotAccess(
        slotId, currentSlotTableEpoch, SlotAccess.Status.Accept, slot.getLeaderEpoch());
  }

  public boolean hasSlot() {
    updateLock.readLock().lock();
    try {
      for (SlotState state : slotTableStates.slotStates.values()) {
        Slot slot = state.slot;
        if (StringUtils.equals(slot.getLeader(), ServerEnv.IP)) {
          return true;
        }
        if (slot.getFollowers() != null && slot.getFollowers().contains(ServerEnv.IP)) {
          return true;
        }
      }
    } finally {
      updateLock.readLock().unlock();
    }
    return false;
  }

  @Override
  public List<BaseSlotStatus> getSlotStatuses() {
    List<BaseSlotStatus> slotStatuses =
        Lists.newArrayListWithCapacity(slotTableStates.slotStates.size());
    updateLock.readLock().lock();
    try {
      for (Map.Entry<Integer, SlotState> entry : slotTableStates.slotStates.entrySet()) {
        int slotId = entry.getKey();
        SlotState slotState = entry.getValue();
        if (localIsLeader(slotState.slot)) {
          LeaderSlotStatus status =
              new LeaderSlotStatus(
                  slotId,
                  slotState.slot.getLeaderEpoch(),
                  ServerEnv.IP,
                  slotState.migrated
                      ? BaseSlotStatus.LeaderStatus.HEALTHY
                      : BaseSlotStatus.LeaderStatus.UNHEALTHY);
          slotStatuses.add(status);
        } else {
          final KeyedTask syncLeaderTask = slotState.syncLeaderTask;
          FollowerSlotStatus status =
              new FollowerSlotStatus(
                  slotId,
                  slotState.slot.getLeaderEpoch(),
                  ServerEnv.IP,
                  syncLeaderTask != null ? syncLeaderTask.getStartTime() : 0,
                  slotState.lastSuccessLeaderSyncTime);
          slotStatuses.add(status);
        }
      }
      return slotStatuses;
    } finally {
      updateLock.readLock().unlock();
    }
  }

  @Override
  public boolean isLeader(String dataCenter, int slotId) {
    final SlotState state = slotTableStates.slotStates.get(slotId);
    return state != null && localIsLeader(state.slot);
  }

  @Override
  public boolean isFollower(String dataCenter, int slotId) {
    final SlotState state = slotTableStates.slotStates.get(slotId);
    return state != null && state.slot.getFollowers().contains(ServerEnv.IP);
  }

  @Override
  public boolean updateSlotTable(SlotTable update) {
    final SlotTable curSlotTable = this.slotTableStates.table;
    if (curSlotTable.getEpoch() >= update.getEpoch()) {
      return false;
    }
    final SlotTable updating = this.updatingSlotTable.get();
    if (updating != null && updating.getEpoch() >= update.getEpoch()) {
      return false;
    }
    recordSlotTable(update);
    // confirmed that slotTable is related to us
    update = update.filter(ServerEnv.IP);

    curSlotTable.assertSlotLessThan(update);
    if (updating != null) {
      updating.assertSlotLessThan(update);
    }

    // do that async, not block the heartbeat
    // v2 > v1, compareAndSet to avoid v1 cover v2
    if (updatingSlotTable.compareAndSet(updating, update)) {
      watchDog.wakeup();
      LOGGER.info(
          "updating slot table, new={}, current={}", update.getEpoch(), curSlotTable.getEpoch());
      return true;
    }
    return false;
  }

  private void recordSlotTable(SlotTable slotTable) {
    if (recorders == null) {
      return;
    }
    for (SlotTableRecorder recorder : recorders) {
      recorder.record(slotTable);
    }
  }

  private void updateSlotState(SlotTable updating) {
    for (Slot s : updating.getSlots()) {
      SlotState state = slotTableStates.slotStates.get(s.getId());
      listenAddUpdate(s);
      if (state != null) {
        state.update(s);
      } else {
        slotTableStates.slotStates.put(s.getId(), new SlotState(s));
        LOGGER.info("add slot, slot={}", s);
      }
    }

    final Iterator<Map.Entry<Integer, SlotState>> it =
        slotTableStates.slotStates.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Integer, SlotState> e = it.next();
      if (updating.getSlot(e.getKey()) == null) {
        final Slot slot = e.getValue().slot;
        it.remove();
        // important, first remove the slot for GetData Access check, then clean the data
        listenRemoveUpdate(slot);
        observeLeaderMigratingFinish(slot.getId());
        LOGGER.info("remove slot, slot={}", slot);
      }
    }
    slotTableStates.table = updating;

    observeLeaderAssignGauge(slotTableStates.table.getLeaderNum(ServerEnv.IP));
    observeFollowerAssignGauge(slotTableStates.table.getFollowerNum(ServerEnv.IP));
  }

  public static final class SlotTableStates {
    volatile SlotTable table = SlotTable.INIT;
    final Map<Integer, SlotState> slotStates = Maps.newConcurrentMap();
  }

  public boolean processUpdating() {
    final SlotTable updating = updatingSlotTable.getAndSet(null);
    if (updating != null) {
      if (updating.getEpoch() > slotTableStates.table.getEpoch()) {
        // lock for update, avoid the checkAccess get the wrong epoch
        updateLock.writeLock().lock();
        try {
          updateSlotState(updating);
        } finally {
          updateLock.writeLock().unlock();
        }
        List<DataNodeSlot> leaders = updating.transfer(ServerEnv.IP, true);
        LOGGER.info("updating slot table, leaders={}, {}, ", leaders, updating);
        return true;
      } else {
        LOGGER.warn(
            "skip updating={}, current={}", updating.getEpoch(), slotTableStates.table.getEpoch());
      }
    }
    return false;
  }

  private final class SyncingWatchDog extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      try {
        processUpdating();
        syncWatch();
      } catch (Throwable e) {
        SYNC_ERROR_LOGGER.error("[syncWatch]failed to do sync watching", e);
      }
    }

    @Override
    public int getWaitingMillis() {
      return 200;
    }
  }

  void syncWatch() {
    final int syncSessionIntervalMs =
        dataServerConfig.getSlotLeaderSyncSessionIntervalSecs() * 1000;
    final int syncLeaderIntervalMs =
        dataServerConfig.getSlotFollowerSyncLeaderIntervalSecs() * 1000;
    final long slotTableEpoch = slotTableStates.table.getEpoch();
    for (SlotState slotState : slotTableStates.slotStates.values()) {
      try {
        sync(slotState, syncSessionIntervalMs, syncLeaderIntervalMs, slotTableEpoch);
      } catch (Throwable e) {
        SYNC_ERROR_LOGGER.error(
            "[syncCommit]failed to do sync slot {}, migrated={}",
            slotState.slot,
            slotState.migrated,
            e);
      }
    }
  }

  boolean sync(
      SlotState slotState,
      int syncSessionIntervalMs,
      int syncLeaderIntervalMs,
      long slotTableEpoch) {
    final Slot slot = slotState.slot;
    if (localIsLeader(slot)) {
      final KeyedTask<SyncLeaderTask> syncLeaderTask = slotState.syncLeaderTask;
      if (syncLeaderTask != null && !syncLeaderTask.isFinished()) {
        // must wait the sync leader finish, avoid the sync-leader conflict with sync-session
        LOGGER.warn("[waitSyncLeader]wait for sync-leader to finish, {},{}", slot, syncLeaderTask);
        return false;
      }
      slotState.syncLeaderTask = null;
      final Set<String> sessions = metaServerService.getSessionServerList();
      if (slotState.migrated) {

        syncSessions(slotState, sessions, syncSessionIntervalMs, slotTableEpoch);
      } else {
        syncMigrating(slotState, sessions, syncSessionIntervalMs, slotTableEpoch);
        // check all migrating task
        checkMigratingTask(slotState, sessions);
      }
    } else {
      // sync leader
      syncLeader(slotState, syncLeaderIntervalMs, slotTableEpoch);
    }
    return true;
  }

  private boolean checkMigratingTask(SlotState slotState, Collection<String> sessions) {
    final Slot slot = slotState.slot;
    final long span = System.currentTimeMillis() - slotState.migratingStartTime;
    MIGRATING_LOGGER.info(
        "[migrating]{},span={},tasks={}/{},sessions={}/{},remains={}",
        slotState.slotId,
        span,
        slotState.migratingTasks.size(),
        slotState.migratingTasks.keySet(),
        sessions.size(),
        sessions,
        getMigratingSpans(slotState));

    // monitor slow migrating
    if (span > 1000 * 8) {
      MIGRATING_LOGGER.error("[slowSlot]{},span={}", slotState.slotId, span);
    }

    // check all migrating task
    if (slotState.migratingTasks.isEmpty() || sessions.isEmpty()) {
      LOGGER.warn("sessionNodes or migratingTask is empty when migrating, {}", slot);
      return false;
    }
    // TODO the session down and up in a short time. session.processId is important
    if (slotState.isAnywaySuccess(sessions)) {
      // after migrated, force to update the version
      // make sure the version is newly than old leader's
      Map<String, DatumVersion> versions =
          datumStorageDelegate.updateVersion(
              dataServerConfig.getLocalDataCenter(), slotState.slotId);
      slotState.migrated = true;
      // versions has update, notify change
      dataChangeEventCenter.onChange(
          versions.keySet(), DataChangeType.MIGRATED, dataServerConfig.getLocalDataCenter());
      LOGGER.info(
          "[finish]slotId={}, span={}, slot={}, sessions={}",
          slotState.slotId,
          span,
          slot,
          sessions);
      slotState.migratingTasks.clear();
      observeLeaderMigratingFinish(slotState.slotId);
      observeLeaderMigratingHistogram(slotState.slotId, span);
      return true;
    }
    return false;
  }

  private Map<String, Long> getMigratingSpans(SlotState slotState) {
    final long now = System.currentTimeMillis();
    Map<String, Long> spans = Maps.newTreeMap();
    for (Map.Entry<String, MigratingTask> e : slotState.migratingTasks.entrySet()) {
      MigratingTask m = e.getValue();
      if (!m.task.isFinished() || (m.task.isFailed() && !m.forceSuccess)) {
        spans.put(e.getKey(), now - m.createTimestamp);
      }
    }
    return spans;
  }

  boolean triggerEmergencyMigrating(
      SlotState slotState, Collection<String> sessions, MigratingTask mtask, int notSyncedCount) {
    try {
      // session.size=1 means only one session, could not skip
      if (sessions.size() <= 1) {
        return false;
      }
      final long span = System.currentTimeMillis() - mtask.createTimestamp;
      int emergencyCases = 0;
      if (span > dataServerConfig.getMigratingMaxSecs() * 1000) {
        emergencyCases++;
      }
      if (mtask.tryCount > dataServerConfig.getMigratingMaxRetry()) {
        emergencyCases++;
      }
      if (emergencyCases != 0) {
        if (notSyncedCount <= dataServerConfig.getMigratingMaxUnavailable()) {
          emergencyCases++;
        }
        LOGGER.error(
            "[slowSession]{},span={},try={},ing={}/{},session={}",
            slotState.slotId,
            span,
            mtask.tryCount,
            notSyncedCount,
            sessions.size(),
            mtask.sessionIp);
        if (emergencyCases >= 3) {
          // mark the session migrating success, avoid block
          mtask.forceSuccess = true;
          return true;
        }
      }
    } catch (Throwable e) {
      // cache unexpect exception, make sure the check not break the migrating
      LOGGER.error(
          "failed to check slow migrating, slotId={}, session={}",
          slotState.slotId,
          mtask.sessionIp,
          e);
    }
    return false;
  }

  private void syncMigrating(
      SlotState slotState,
      Collection<String> sessions,
      int syncSessionIntervalMs,
      long slotTableEpoch) {
    final Slot slot = slotState.slot;
    if (slotState.migratingStartTime == 0) {
      slotState.migratingStartTime = System.currentTimeMillis();
      slotState.migratingTasks.clear();
      observeLeaderMigratingStart(slot.getId());
      LOGGER.info(
          "start migrating, slotId={}, sessionSize={}, sessions={}",
          slotState.slotId,
          sessions.size(),
          sessions);
    }
    final int notSyncedCount = sessions.size() - slotState.countSyncSuccess(sessions);
    for (String sessionIp : sessions) {
      MigratingTask mtask = slotState.migratingTasks.get(sessionIp);
      if (mtask == null) {
        KeyedTask<SyncSessionTask> ktask =
            commitSyncSessionTask(slot, slotTableEpoch, sessionIp, null, true);
        mtask = new MigratingTask(sessionIp, ktask);
        slotState.migratingTasks.put(sessionIp, mtask);
        LOGGER.info("migrating start,slotId={},session={}", slot.getId(), sessionIp);
        continue;
      }

      if (mtask.task.isFailed() && !mtask.forceSuccess) {
        // failed and not force Success, try to trigger emergency
        if (triggerEmergencyMigrating(slotState, sessions, mtask, notSyncedCount)) {
          LOGGER.info("[emergency]{},session={}", slotState.slotId, mtask.sessionIp);
        } else {
          KeyedTask<SyncSessionTask> ktask =
              commitSyncSessionTask(slot, slotTableEpoch, sessionIp, null, true);
          mtask.task = ktask;
          mtask.tryCount++;
          LOGGER.error(
              "migrating retry,slotId={},try={},session={},create={}/{}",
              slot.getId(),
              mtask.tryCount,
              sessionIp,
              mtask.createTimestamp,
              System.currentTimeMillis() - mtask.createTimestamp);
          continue;
        }
      }
      // force success or migrating finish. try to sync session
      // avoid the time of migrating is too long and block the syncing of session
      if (mtask.task.isOverAfter(syncSessionIntervalMs)) {
        if (syncSession(slotState, sessionIp, null, syncSessionIntervalMs, slotTableEpoch)) {
          LOGGER.info("slotId={}, sync session in migrating, session={}", slot.getId(), sessionIp);
        }
      }
    }
  }

  private void syncSessions(
      SlotState slotState,
      Collection<String> sessions,
      int syncSessionIntervalMs,
      long slotTableEpoch) {

    final Set<String> doSyncSet = Sets.newHashSetWithExpectedSize(16);
    for (String sessionIp : sessions) {
      if (needSessionSync(slotState, sessionIp, syncSessionIntervalMs)) {
        doSyncSet.add(sessionIp);
      }
    }

    if (CollectionUtils.isEmpty(doSyncSet)) {
      return;
    }

    final Map<String, Map<String, DatumSummary>> datumSummary =
        Maps.newHashMapWithExpectedSize(doSyncSet.size());

    datumStorageDelegate.foreach(
        dataServerConfig.getLocalDataCenter(),
        slotState.slotId,
        DatumBiConsumer.publisherGroupsBiConsumer(
            datumSummary, doSyncSet, syncSlotAcceptAllManager));
    for (String sessionIp : doSyncSet) {
      Map<String, DatumSummary> summary = datumSummary.get(sessionIp);
      syncSession(slotState, sessionIp, summary, syncSessionIntervalMs, slotTableEpoch);
    }
  }

  /**
   * summary == null means can not assembly summary at first(migrating); do
   * getDatumSummary(sessionIp) later
   *
   * @param slotState
   * @param sessionIp
   * @param summary
   * @param syncSessionIntervalMs
   * @param slotTableEpoch
   * @return
   */
  private boolean syncSession(
      SlotState slotState,
      String sessionIp,
      Map<String, DatumSummary> summary,
      int syncSessionIntervalMs,
      long slotTableEpoch) {
    final Slot slot = slotState.slot;
    if (needSessionSync(slotState, sessionIp, syncSessionIntervalMs)) {
      KeyedTask<SyncSessionTask> task =
          commitSyncSessionTask(slot, slotTableEpoch, sessionIp, summary, false);
      slotState.syncSessionTasks.put(sessionIp, task);
      return true;
    }
    return false;
  }

  private boolean needSessionSync(
      SlotState slotState, String sessionIp, int syncSessionIntervalMs) {
    KeyedTask<SyncSessionTask> task = slotState.syncSessionTasks.get(sessionIp);
    return task == null || task.isOverAfter(syncSessionIntervalMs);
  }

  private void syncLeader(SlotState slotState, int syncLeaderIntervalMs, long slotTableEpoch) {
    final Slot slot = slotState.slot;
    final KeyedTask<SyncLeaderTask> syncLeaderTask = slotState.syncLeaderTask;
    if (syncLeaderTask != null && syncLeaderTask.isFinished()) {
      slotState.completeSyncLeaderTask();
    }
    if (syncLeaderTask == null || syncLeaderTask.isOverAfter(syncLeaderIntervalMs)) {
      // sync leader no need to notify event
      SlotDiffSyncer syncer =
          new SlotDiffSyncer(
              dataServerConfig,
              datumStorageDelegate,
              null,
              sessionLeaseManager,
              syncSlotAcceptAllManager,
              DIFF_LOGGER);
      SyncContinues continues =
          new SyncContinues() {
            @Override
            public boolean continues() {
              return isFollower(dataServerConfig.getLocalDataCenter(), slot.getId());
            }
          };
      SyncLeaderTask task =
          new SyncLeaderTask(
              dataServerConfig.getLocalDataCenter(),
              dataServerConfig.getLocalDataCenter(),
              slotTableEpoch,
              slot,
              syncer,
              dataNodeExchanger,
              continues,
              SYNC_DIGEST_LOGGER,
              SYNC_ERROR_LOGGER);
      slotState.syncLeaderTask = syncLeaderExecutor.execute(slot.getId(), task);
    } else if (!syncLeaderTask.isFinished()) {
      if (System.currentTimeMillis() - syncLeaderTask.getCreateTime() > 5000) {
        // the sync leader is running more than 5secs, print
        LOGGER.info("sync-leader running, {}", syncLeaderTask);
      }
    }
  }

  private KeyedTask<SyncSessionTask> commitSyncSessionTask(
      Slot slot,
      long slotTableEpoch,
      String sessionIp,
      Map<String, DatumSummary> summary,
      boolean migrate) {
    SlotDiffSyncer syncer =
        new SlotDiffSyncer(
            dataServerConfig,
            datumStorageDelegate,
            dataChangeEventCenter,
            sessionLeaseManager,
            syncSlotAcceptAllManager,
            DIFF_LOGGER);
    SyncContinues continues =
        new SyncContinues() {
          @Override
          public boolean continues() {
            // if not leader, the syncing need to break
            return isLeader(dataServerConfig.getLocalDataCenter(), slot.getId());
          }
        };
    SyncSessionTask task =
        new SyncSessionTask(
            dataServerConfig.getLocalDataCenter(),
            migrate,
            slotTableEpoch,
            slot,
            sessionIp,
            syncer,
            sessionNodeExchanger,
            continues,
            summary);
    if (migrate) {
      // group by slotId and session
      return migrateSessionExecutor.execute(new Tuple(slot.getId(), sessionIp), task);
    } else {
      // at most there is 4 tasks for a session, avoid too many tasks hit the same session
      return syncSessionExecutor.execute(new Tuple((slot.getId() % 4), sessionIp), task);
    }
  }

  public interface ISlotState {
    boolean isMigrated();

    Slot getSlot();
  }

  static final class SlotState implements ISlotState {
    final int slotId;
    volatile Slot slot;
    volatile boolean migrated;
    volatile long migratingStartTime;
    volatile long lastSuccessLeaderSyncTime = -1L;
    final Map<String, MigratingTask> migratingTasks = Maps.newTreeMap();
    final Map<String, KeyedTask<SyncSessionTask>> syncSessionTasks = Maps.newTreeMap();
    volatile KeyedTask<SyncLeaderTask> syncLeaderTask;

    SlotState(Slot slot) {
      this.slotId = slot.getId();
      this.slot = slot;
    }

    void update(Slot s) {
      ParaCheckUtil.checkEquals(slotId, s.getId(), "slot.id");
      if (slot.getLeaderEpoch() != s.getLeaderEpoch()) {
        this.migrated = false;
        this.syncSessionTasks.clear();
        this.migratingTasks.clear();
        this.migratingStartTime = 0;
        if (localIsLeader(s)) {
          // leader change
          observeLeaderUpdateCounter();
        }
        observeLeaderMigratingFinish(slot.getId());
        LOGGER.info("update slot with leaderEpoch, exist={}, now={}", slot, s);
      }
      this.slot = s;
      LOGGER.info("update slot, slot={}", slot);
    }

    void completeSyncLeaderTask() {
      if (syncLeaderTask != null && syncLeaderTask.isSuccess()) {
        this.lastSuccessLeaderSyncTime = syncLeaderTask.getEndTime();
      }
    }

    int countSyncSuccess(Collection<String> sessions) {
      int count = 0;
      for (String session : sessions) {
        MigratingTask t = migratingTasks.get(session);
        // not contains forceSuccess
        if (t != null && t.task.isSuccess()) {
          count++;
        }
      }
      return count;
    }

    boolean isAnywaySuccess(Collection<String> sessions) {
      if (sessions.isEmpty()) {
        return false;
      }
      // contains forceSuccess
      for (String session : sessions) {
        final MigratingTask t = migratingTasks.get(session);
        if (t == null) {
          return false;
        }
        // contains forceSuccess
        if (t.forceSuccess) {
          continue;
        }
        if (!t.task.isSuccess()) {
          // could not use isFailed to replace !isSuccess
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean isMigrated() {
      return this.migrated;
    }

    @Override
    public Slot getSlot() {
      return this.slot;
    }
  }

  static class MigratingTask {
    final long createTimestamp = System.currentTimeMillis();
    final String sessionIp;
    KeyedTask<SyncSessionTask> task;
    int tryCount;
    boolean forceSuccess;

    MigratingTask(String sessionIp, KeyedTask<SyncSessionTask> task) {
      this.sessionIp = sessionIp;
      this.task = task;
    }
  }

  private static final class SyncSessionTask implements Runnable {
    final long startTimestamp = System.currentTimeMillis();
    final String syncDataCenter;
    final boolean migrating;
    final long slotTableEpoch;
    final Slot slot;
    final String sessionIp;
    final SlotDiffSyncer syncer;
    final SessionNodeExchanger sessionNodeExchanger;
    final SyncContinues continues;
    final Map<String, DatumSummary> summary;

    SyncSessionTask(
        String syncDataCenter,
        boolean migrating,
        long slotTableEpoch,
        Slot slot,
        String sessionIp,
        SlotDiffSyncer syncer,
        SessionNodeExchanger sessionNodeExchanger,
        SyncContinues continues,
        Map<String, DatumSummary> summary) {
      this.syncDataCenter = syncDataCenter;
      this.migrating = migrating;
      this.slotTableEpoch = slotTableEpoch;
      this.slot = slot;
      this.sessionIp = sessionIp;
      this.syncer = syncer;
      this.sessionNodeExchanger = sessionNodeExchanger;
      this.continues = continues;
      this.summary = summary;
    }

    public void run() {
      boolean success = false;
      try {

        success =
            syncer.syncSession(
                syncDataCenter,
                slot.getId(),
                sessionIp,
                slot.getLeaderEpoch(),
                sessionNodeExchanger,
                slotTableEpoch,
                continues,
                summary);
        if (!success) {
          // sync failed
          throw new RuntimeException("sync session failed");
        }
      } catch (Throwable e) {
        if (migrating) {
          observeLeaderMigratingFail(slot.getId(), sessionIp);
          SYNC_ERROR_LOGGER.error("[migrating]failed: {}, slot={}", sessionIp, slot.getId(), e);
        } else {
          SYNC_ERROR_LOGGER.error("[syncSession]failed: {}, slot={}", sessionIp, slot.getId(), e);
        }
        // rethrow silence exception, notify the task is failed
        throw TaskErrorSilenceException.INSTANCE;
      } finally {
        SYNC_DIGEST_LOGGER.info(
            "{},{},{},{},span={}",
            success ? 'Y' : 'N',
            migrating ? 'M' : 'S',
            slot.getId(),
            sessionIp,
            System.currentTimeMillis() - startTimestamp);
      }
    }

    @Override
    public String toString() {
      return "SyncSession{epoch="
          + slotTableEpoch
          + ", session="
          + sessionIp
          + ", slot="
          + slot
          + '}';
    }
  }

  @Override
  public void triggerUpdateSlotTable(long expectEpoch) {
    // TODO
  }

  @Override
  public Set<Integer> leaderSlotIds() {
    Set<Integer> ret = Sets.newTreeSet();
    updateLock.readLock().lock();
    try {
      for (SlotState state : slotTableStates.slotStates.values()) {
        Slot slot = state.slot;
        if (StringUtils.equals(slot.getLeader(), ServerEnv.IP)) {
          ret.add(state.slotId);
        }
      }
    } finally {
      updateLock.readLock().unlock();
    }
    return ret;
  }

  @Override
  public Tuple<Long, List<BaseSlotStatus>> getSlotTableEpochAndStatuses(String dataCenter) {
    updateLock.readLock().lock();
    try {
      long slotTableEpoch = getSlotTableEpoch();
      List<BaseSlotStatus> slotStatuses = getSlotStatuses();
      return Tuple.of(slotTableEpoch, slotStatuses);
    } finally {
      updateLock.readLock().unlock();
    }
  }

  @Override
  public long getSlotTableEpoch() {
    return slotTableStates.table.getEpoch();
  }

  private static Slot.Role getRole(Slot s) {
    return localIsLeader(s) ? Slot.Role.Leader : Slot.Role.Follower;
  }

  private void listenAddUpdate(Slot s) {
    slotChangeListenerManager
        .localUpdateListeners()
        .forEach(
            listener ->
                listener.onSlotAdd(dataServerConfig.getLocalDataCenter(), s.getId(), getRole(s)));
  }

  private void listenRemoveUpdate(Slot s) {
    slotChangeListenerManager
        .localUpdateListeners()
        .forEach(
            listener ->
                listener.onSlotRemove(
                    dataServerConfig.getLocalDataCenter(), s.getId(), getRole(s)));
  }

  private static boolean localIsLeader(Slot slot) {
    return ServerEnv.isLocalServer(slot.getLeader());
  }

  @VisibleForTesting
  void setMetaServerService(MetaServerServiceImpl metaServerService) {
    this.metaServerService = metaServerService;
  }

  @VisibleForTesting
  void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }

  @VisibleForTesting
  void setDataChangeEventCenter(DataChangeEventCenter dataChangeEventCenter) {
    this.dataChangeEventCenter = dataChangeEventCenter;
  }

  @VisibleForTesting
  void setSessionLeaseManager(SessionLeaseManager sessionLeaseManager) {
    this.sessionLeaseManager = sessionLeaseManager;
  }

  /**
   * Setter method for property <tt>datumStorageDelegate</tt>.
   *
   * @param datumStorageDelegate value to be assigned to property datumStorageDelegate
   */
  @VisibleForTesting
  public void setDatumStorageDelegate(DatumStorageDelegate datumStorageDelegate) {
    this.datumStorageDelegate = datumStorageDelegate;
  }

  /**
   * Setter method for property <tt>syncSlotAcceptAllManager</tt>.
   *
   * @param syncSlotAcceptAllManager value to be assigned to property syncSlotAcceptAllManager
   */
  @VisibleForTesting
  public void setSyncSlotAcceptAllManager(SyncSlotAcceptorManager syncSlotAcceptAllManager) {
    this.syncSlotAcceptAllManager = syncSlotAcceptAllManager;
  }

  /**
   * Setter method for property <tt>slotChangeListenerManager</tt>.
   *
   * @param slotChangeListenerManager value to be assigned to property slotChangeListenerManager
   */
  @VisibleForTesting
  public void setSlotChangeListenerManager(SlotChangeListenerManager slotChangeListenerManager) {
    this.slotChangeListenerManager = slotChangeListenerManager;
  }

  @VisibleForTesting
  SlotManagerImpl setRecorders(List<SlotTableRecorder> recorders) {
    this.recorders = recorders;
    return this;
  }
}
