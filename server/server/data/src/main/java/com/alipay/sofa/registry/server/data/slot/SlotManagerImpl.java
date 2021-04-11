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
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.alipay.sofa.registry.server.shared.slot.DiskSlotTableRecorder;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 09:44 yuzhi.lyz Exp $
 */
public final class SlotManagerImpl implements SlotManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SlotManagerImpl.class);

  private static final Logger MIGRATING_LOGGER = LoggerFactory.getLogger("MIGRATING");

  private static final Logger SYNC_ERROR_LOGGER = LoggerFactory.getLogger("SYNC-ERROR");

  private static final Logger SYNC_DIGEST_LOGGER = LoggerFactory.getLogger("SYNC-DIGEST");

  private final SlotFunction slotFunction = SlotFunctionRegistry.getFunc();

  @Autowired private DataNodeExchanger dataNodeExchanger;

  @Autowired private SessionNodeExchanger sessionNodeExchanger;

  @Autowired private MetaServerServiceImpl metaServerService;

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private DatumStorage localDatumStorage;

  @Autowired private DataChangeEventCenter dataChangeEventCenter;

  @Autowired private SessionLeaseManager sessionLeaseManager;

  @Autowired private SlotGenericResource slotGenericResource;

  private List<SlotTableRecorder> recorders = Collections.EMPTY_LIST;

  private final List<SlotChangeListener> slotChangeListeners = new ArrayList<>();

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
    recorders = Lists.newArrayList(slotGenericResource, new DiskSlotTableRecorder());
    initSlotChangeListener();
    initExecutors();
    ConcurrentUtils.createDaemonThread("SyncingWatchDog", watchDog).start();
  }

  void initSlotChangeListener() {
    SlotChangeListener l = localDatumStorage.getSlotChangeListener();
    if (l != null) {
      this.slotChangeListeners.add(l);
    }
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
  public Slot getSlot(int slotId) {
    final SlotState state = slotTableStates.slotStates.get(slotId);
    return state == null ? null : state.slot;
  }

  @Override
  public SlotAccess checkSlotAccess(int slotId, long srcSlotEpoch, long srcLeaderEpoch) {
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

  static SlotAccess checkSlotAccess(
      int slotId, long currentSlotTableEpoch, SlotState state, long srcLeaderEpoch) {
    if (state == null) {
      return new SlotAccess(slotId, currentSlotTableEpoch, SlotAccess.Status.Moved, -1);
    }
    final Slot slot = state.slot;
    if (!localIsLeader(slot)) {
      return new SlotAccess(
          slotId, currentSlotTableEpoch, SlotAccess.Status.Moved, slot.getLeaderEpoch());
    }
    if (!state.migrated) {
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
  public boolean isLeader(int slotId) {
    final SlotState state = slotTableStates.slotStates.get(slotId);
    return state != null && localIsLeader(state.slot);
  }

  @Override
  public boolean isFollower(int slotId) {
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
    updatingSlotTable.set(update);
    watchDog.wakeup();
    LOGGER.info(
        "updating slot table, new={}, current={}", update.getEpoch(), curSlotTable.getEpoch());
    return true;
  }

  private void recordSlotTable(SlotTable slotTable) {
    for (SlotTableRecorder recorder : recorders) {
      recorder.record(slotTable);
    }
  }

  private void updateSlotState(SlotTable updating) {
    for (Slot s : updating.getSlots()) {
      SlotState state = slotTableStates.slotStates.get(s.getId());
      listenAdd(s);
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
        listenRemove(slot);
        observeLeaderMigratingFinish(slot.getId());
        LOGGER.info("remove slot, slot={}", slot);
      }
    }
    slotTableStates.table = updating;
    observeLeaderAssignGauge(slotTableStates.table.getLeaderNum(ServerEnv.IP));
    observeFollowerAssignGauge(slotTableStates.table.getFollowerNum(ServerEnv.IP));
  }

  private static final class SlotTableStates {
    volatile SlotTable table = SlotTable.INIT;
    final Map<Integer, SlotState> slotStates = Maps.newConcurrentMap();
  }

  boolean processUpdating() {
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
        LOGGER.warn("wait for sync-leader to finish, {}", slot, syncLeaderTask);
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

    MIGRATING_LOGGER.info(
        "[migrating]{},span={},tasks={}/{},sessions={}/{},remains={}",
        slot.getId(),
        System.currentTimeMillis() - slotState.migratingStartTime,
        slotState.migratingTasks.size(),
        slotState.migratingTasks.keySet(),
        sessions.size(),
        sessions,
        getMigratingSpans(slotState));

    // check all migrating task
    if (slotState.migratingTasks.isEmpty() || sessions.isEmpty()) {
      LOGGER.warn("sessionNodes or migratingTask is empty when migrating, {}", slot);
      return false;
    }
    // TODO the session down and up in a short time. session.processId is important
    if (slotState.isFinish(sessions)) {
      // after migrated, force to update the version
      // make sure the version is newly than old leader's
      localDatumStorage.updateVersion(slot.getId());
      slotState.migrated = true;
      final long span = System.currentTimeMillis() - slotState.migratingStartTime;
      LOGGER.info(
          "slotId={}, migrating finish, span={}, slot={}, sessions={}",
          slot.getId(),
          span,
          slot,
          sessions);
      slotState.migratingTasks.clear();
      observeLeaderMigratingFinish(slot.getId());
      observeLeaderMigratingHistogram(slot.getId(), span);
      return true;
    }
    return false;
  }

  private Map<String, Long> getMigratingSpans(SlotState slotState) {
    final long now = System.currentTimeMillis();
    Map<String, Long> spans = Maps.newTreeMap();
    for (Map.Entry<String, MigratingTask> e : slotState.migratingTasks.entrySet()) {
      MigratingTask m = e.getValue();
      if (!m.task.isFinished() || m.task.isFailed()) {
        spans.put(e.getKey(), now - m.createTimestamp);
      }
    }
    return spans;
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
          slot.getId(),
          sessions.size(),
          sessions);
    }
    for (String sessionIp : sessions) {
      MigratingTask mtask = slotState.migratingTasks.get(sessionIp);
      if (mtask == null || mtask.task.isFailed()) {
        KeyedTask<SyncSessionTask> ktask =
            commitSyncSessionTask(slot, slotTableEpoch, sessionIp, true);
        if (mtask == null) {
          mtask = new MigratingTask(ktask);
          slotState.migratingTasks.put(sessionIp, mtask);
        } else {
          // fail
          mtask.task = ktask;
        }
        // TODO add max trycount, avoid the unhealth session block the migrating
        mtask.tryCount++;
        continue;
      }
      // migrating finish. try to sync session
      // avoid the time of migrating is too long and block the syncing of session
      if (mtask.task.isOverAfter(syncSessionIntervalMs)) {
        if (syncSession(slotState, sessionIp, syncSessionIntervalMs, slotTableEpoch)) {
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
    for (String sessionIp : sessions) {
      syncSession(slotState, sessionIp, syncSessionIntervalMs, slotTableEpoch);
    }
  }

  private boolean syncSession(
      SlotState slotState, String sessionIp, int syncSessionIntervalMs, long slotTableEpoch) {
    final Slot slot = slotState.slot;
    KeyedTask<SyncSessionTask> task = slotState.syncSessionTasks.get(sessionIp);
    if (task == null || task.isOverAfter(syncSessionIntervalMs)) {
      task = commitSyncSessionTask(slot, slotTableEpoch, sessionIp, false);
      slotState.syncSessionTasks.put(sessionIp, task);
      return true;
    }
    return false;
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
          new SlotDiffSyncer(dataServerConfig, localDatumStorage, null, sessionLeaseManager);
      SyncContinues continues =
          new SyncContinues() {
            @Override
            public boolean continues() {
              return isFollower(slot.getId());
            }
          };
      SyncLeaderTask task =
          new SyncLeaderTask(slotTableEpoch, slot, syncer, dataNodeExchanger, continues);
      slotState.syncLeaderTask = syncLeaderExecutor.execute(slot.getId(), task);
    } else if (!syncLeaderTask.isFinished()) {
      if (System.currentTimeMillis() - syncLeaderTask.getCreateTime() > 5000) {
        // the sync leader is running more than 5secs, print
        LOGGER.info("sync-leader running, {}", syncLeaderTask);
      }
    }
  }

  private KeyedTask<SyncSessionTask> commitSyncSessionTask(
      Slot slot, long slotTableEpoch, String sessionIp, boolean migrate) {
    SlotDiffSyncer syncer =
        new SlotDiffSyncer(
            dataServerConfig, localDatumStorage, dataChangeEventCenter, sessionLeaseManager);
    SyncContinues continues =
        new SyncContinues() {
          @Override
          public boolean continues() {
            // if not leader, the syncing need to break
            return isLeader(slot.getId());
          }
        };
    SyncSessionTask task =
        new SyncSessionTask(
            migrate, slotTableEpoch, slot, sessionIp, syncer, sessionNodeExchanger, continues);
    if (migrate) {
      // group by slotId and session
      return migrateSessionExecutor.execute(new Tuple(slot.getId(), sessionIp), task);
    } else {
      // at most there is 4 tasks for a session, avoid too many tasks hit the same session
      return syncSessionExecutor.execute(new Tuple((slot.getId() % 4), sessionIp), task);
    }
  }

  static final class SlotState {
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

    boolean isFinish(Collection<String> sessions) {
      if (sessions.isEmpty()) {
        return false;
      }
      boolean finished = true;
      for (String session : sessions) {
        MigratingTask t = migratingTasks.get(session);
        if (t == null || !t.task.isSuccess()) {
          finished = false;
          break;
        }
      }
      return finished;
    }
  }

  static class MigratingTask {
    final long createTimestamp = System.currentTimeMillis();
    KeyedTask<SyncSessionTask> task;
    int tryCount;

    MigratingTask(KeyedTask<SyncSessionTask> task) {
      this.task = task;
    }
  }

  private static final class SyncSessionTask implements Runnable {
    final long startTimestamp = System.currentTimeMillis();
    final boolean migrating;
    final long slotTableEpoch;
    final Slot slot;
    final String sessionIp;
    final SlotDiffSyncer syncer;
    final SessionNodeExchanger sessionNodeExchanger;
    final SyncContinues continues;

    SyncSessionTask(
        boolean migrating,
        long slotTableEpoch,
        Slot slot,
        String sessionIp,
        SlotDiffSyncer syncer,
        SessionNodeExchanger sessionNodeExchanger,
        SyncContinues continues) {
      this.migrating = migrating;
      this.slotTableEpoch = slotTableEpoch;
      this.slot = slot;
      this.sessionIp = sessionIp;
      this.syncer = syncer;
      this.sessionNodeExchanger = sessionNodeExchanger;
      this.continues = continues;
    }

    public void run() {
      boolean success = false;
      try {
        success =
            syncer.syncSession(
                slot.getId(), sessionIp, sessionNodeExchanger, slotTableEpoch, continues);
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

  private static final class SyncLeaderTask implements Runnable {
    final long startTimestamp = System.currentTimeMillis();
    final long slotTableEpoch;
    final Slot slot;
    final SlotDiffSyncer syncer;
    final DataNodeExchanger dataNodeExchanger;
    final SyncContinues continues;

    SyncLeaderTask(
        long slotTableEpoch,
        Slot slot,
        SlotDiffSyncer syncer,
        DataNodeExchanger dataNodeExchanger,
        SyncContinues continues) {
      this.slotTableEpoch = slotTableEpoch;
      this.slot = slot;
      this.syncer = syncer;
      this.dataNodeExchanger = dataNodeExchanger;
      this.continues = continues;
    }

    @Override
    public void run() {
      boolean success = false;
      try {
        success =
            syncer.syncSlotLeader(
                slot.getId(), slot.getLeader(), dataNodeExchanger, slotTableEpoch, continues);
        if (!success) {
          throw new RuntimeException("sync leader failed");
        }
      } catch (Throwable e) {
        SYNC_ERROR_LOGGER.error(
            "[syncLeader]failed: {}, slot={}", slot.getLeader(), slot.getId(), e);
        // rethrow silence exception, notify the task is failed
        throw TaskErrorSilenceException.INSTANCE;
      } finally {
        SYNC_DIGEST_LOGGER.info(
            "{},L,{},{},span={}",
            success ? 'Y' : 'N',
            slot.getId(),
            slot.getLeader(),
            System.currentTimeMillis() - startTimestamp);
      }
    }

    @Override
    public String toString() {
      return "SyncLeader{epoch=" + slotTableEpoch + ", slot=" + slot + '}';
    }
  }

  @Override
  public void triggerUpdateSlotTable(long expectEpoch) {
    // TODO
  }

  @Override
  public Tuple<Long, List<BaseSlotStatus>> getSlotTableEpochAndStatuses() {
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

  private void listenAdd(Slot s) {
    slotChangeListeners.forEach(listener -> listener.onSlotAdd(s.getId(), getRole(s)));
  }

  private void listenRemove(Slot s) {
    slotChangeListeners.forEach(listener -> listener.onSlotRemove(s.getId(), getRole(s)));
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
  void setLocalDatumStorage(DatumStorage localDatumStorage) {
    this.localDatumStorage = localDatumStorage;
  }

  @VisibleForTesting
  void setDataChangeEventCenter(DataChangeEventCenter dataChangeEventCenter) {
    this.dataChangeEventCenter = dataChangeEventCenter;
  }

  @VisibleForTesting
  void setSessionLeaseManager(SessionLeaseManager sessionLeaseManager) {
    this.sessionLeaseManager = sessionLeaseManager;
  }

  @VisibleForTesting
  void setSlotGenericResource(SlotGenericResource slotGenericResource) {
    this.slotGenericResource = slotGenericResource;
  }
}
