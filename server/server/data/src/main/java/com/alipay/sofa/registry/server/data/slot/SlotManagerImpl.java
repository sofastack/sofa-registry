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

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
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
import static com.alipay.sofa.registry.server.data.slot.SlotMetrics.Manager.*;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.alipay.sofa.registry.task.KeyedTask;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 09:44 yuzhi.lyz Exp $
 */
public final class SlotManagerImpl implements SlotManager {
    private static final Logger              LOGGER              = LoggerFactory
                                                                     .getLogger(SlotManagerImpl.class);

    private static final Logger              MIGRATING_LOGGER    = LoggerFactory
                                                                     .getLogger("MIGRATING");

    private final SlotFunction               slotFunction        = SlotFunctionRegistry.getFunc();

    @Autowired
    private DataNodeExchanger                dataNodeExchanger;

    @Autowired
    private SessionNodeExchanger             sessionNodeExchanger;

    @Autowired
    private MetaServerServiceImpl            metaServerService;

    @Autowired
    private DataServerConfig                 dataServerConfig;

    @Autowired
    private DatumStorage                     localDatumStorage;

    @Autowired
    private DataChangeEventCenter            dataChangeEventCenter;

    @Autowired
    private SessionLeaseManager              sessionLeaseManager;

    @Autowired
    private SlotGenericResource              slotGenericResource;

    private List<SlotTableRecorder>          recorders;

    private final List<SlotChangeListener>   slotChangeListeners = new ArrayList<>();

    private KeyedThreadPoolExecutor          migrateSessionExecutor;
    private KeyedThreadPoolExecutor          syncSessionExecutor;
    private KeyedThreadPoolExecutor          syncLeaderExecutor;

    /**
     * the sync and migrating may happen parallelly when slot role has modified.
     * make sure the datum merging is idempotent
     */
    private SyncingWatchDog                  watchDog;
    private final AtomicReference<SlotTable> updatingSlotTable   = new AtomicReference<SlotTable>();
    private final SlotTableStates            slotTableStates     = new SlotTableStates();

    @PostConstruct
    public void init() {
        this.migrateSessionExecutor = new KeyedThreadPoolExecutor("migrate-session",
            dataServerConfig.getSlotLeaderSyncSessionExecutorThreadSize(),
            dataServerConfig.getSlotLeaderSyncSessionExecutorQueueSize());

        this.syncSessionExecutor = new KeyedThreadPoolExecutor("sync-session",
            dataServerConfig.getSlotLeaderSyncSessionExecutorThreadSize(),
            dataServerConfig.getSlotLeaderSyncSessionExecutorQueueSize());

        this.syncLeaderExecutor = new KeyedThreadPoolExecutor("sync-leader",
            dataServerConfig.getSlotFollowerSyncLeaderExecutorThreadSize(),
            dataServerConfig.getSlotFollowerSyncLeaderExecutorQueueSize());

        SlotChangeListener l = localDatumStorage.getSlotChangeListener();
        if (l != null) {
            this.slotChangeListeners.add(l);
        }
        watchDog = new SyncingWatchDog();
        ConcurrentUtils.createDaemonThread("SyncingWatchDog", watchDog).start();
        recorders = Lists.newArrayList(slotGenericResource, new DiskSlotTableRecorder());
    }

    @Override
    public int slotOf(String dataInfoId) {
        return slotFunction.slotOf(dataInfoId);
    }

    @Override
    public SlotAccess checkSlotAccess(int slotId, long srcSlotEpoch) {
        final long currentEpoch = slotTableStates.table.getEpoch();
        if (currentEpoch < srcSlotEpoch) {
            triggerUpdateSlotTable(srcSlotEpoch);
        }

        final SlotState state = slotTableStates.slotStates.get(slotId);
        if (state == null || !localIsLeader(state.slot)) {
            return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Moved);
        }
        if (!state.migrated) {
            return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Migrating);
        }
        return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Accept);
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
        //confirmed that slotTable is related to us
        update = update.filter(ServerEnv.IP);

        curSlotTable.assertSlotLessThan(update);
        if (updating != null) {
            updating.assertSlotLessThan(update);
        }

        // do that async, not block the heartbeat
        updatingSlotTable.set(update);
        watchDog.wakeup();
        LOGGER.info("updating slot table, new={}, current={}", update.getEpoch(),
            curSlotTable.getEpoch());
        return true;
    }

    private void recordSlotTable(SlotTable slotTable) {
        recorders.forEach(recorder -> {
            if(recorder != null) {
                recorder.record(slotTable);
            }
        });
    }

    private void updateSlotState(SlotTable updating) {
        updating.getSlots().forEach(s -> {
            SlotState state = slotTableStates.slotStates.get(s.getId());
            listenAdd(s);
            if (state != null) {
                state.update(s);
            } else {
                slotTableStates.slotStates.put(s.getId(), new SlotState(s));
                LOGGER.info("add slot, slot={}", s);
            }
        });

        final Iterator<Map.Entry<Integer, SlotState>> it = slotTableStates.slotStates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, SlotState> e = it.next();
            if (updating.getSlot(e.getKey()) == null) {
                final Slot slot = e.getValue().slot;
                it.remove();
                listenRemove(slot);
                LOGGER.info("remove slot, slot={}", slot);
            }
        }
        slotTableStates.table = updating;
        observeLeaderAssignGauge(slotTableStates.table.getLeaderNum(ServerEnv.IP));
        observeFollowerAssignGauge(slotTableStates.table.getFollowerNum(ServerEnv.IP));
    }

    private static final class SlotTableStates {
        volatile SlotTable            table      = SlotTable.INIT;
        final Map<Integer, SlotState> slotStates = Maps.newConcurrentMap();
    }

    private final class SyncingWatchDog extends WakeUpLoopRunnable {

        @Override
        public void runUnthrowable() {
            try {
                final SlotTable updating = updatingSlotTable.getAndSet(null);
                if (updating != null && updating.getEpoch() > slotTableStates.table.getEpoch()) {
                    updateSlotState(updating);
                    LOGGER.info("updating slot table {}", updating);
                }

                final int syncIntervalMs = dataServerConfig.getSlotLeaderSyncSessionIntervalSec() * 1000;
                final long slotTableEpoch = slotTableStates.table.getEpoch();
                for (SlotState slotState : slotTableStates.slotStates.values()) {
                    final Slot slot = slotState.slot;
                    final KeyedTask<SyncLeaderTask> syncLeaderTask = slotState.syncLeaderTask;
                    if (localIsLeader(slot)) {
                        if (syncLeaderTask != null && !syncLeaderTask.isFinished()) {
                            // must wait the sync leader finish, avoid the sync-leader conflict with sync-session
                            LOGGER.warn("wait for sync-leader to finish, {}", slot, syncLeaderTask);
                            continue;
                        }
                        slotState.syncLeaderTask = null;
                        final Set<String> sessions = metaServerService.getSessionNodes().keySet();
                        if (slotState.migrated) {
                            for (String sessionIp : sessions) {
                                KeyedTask<SyncSessionTask> task = slotState.syncSessionTasks.get(sessionIp);
                                if (task == null || task.isOverAfter(syncIntervalMs)) {
                                    task = slotState.commitSyncSessionTask(slotTableEpoch, sessionIp, false);
                                    slotState.syncSessionTasks.put(sessionIp, task);
                                }
                            }
                        } else {
                            if (slotState.migratingStartTime == 0) {
                                slotState.migratingStartTime = System.currentTimeMillis();
                                observeLeaderMigratingStart(slot.getId());
                                LOGGER.info("start migrating, slotId={}, sessions={}", slot.getId(), sessions);
                            }
                            for (String sessionIp : sessions) {
                                MigratingTask mtask = slotState.migratingTasks.get(sessionIp);
                                if (mtask == null || mtask.task.isFailed()) {
                                    KeyedTask<SyncSessionTask> ktask = slotState
                                            .commitSyncSessionTask(slotTableEpoch, sessionIp, true);
                                    if (mtask == null) {
                                        mtask = new MigratingTask(ktask);
                                        slotState.migratingTasks.put(sessionIp, mtask);
                                    } else {
                                        // fail
                                        observeLeaderMigratingFail(slot.getId(), sessionIp);
                                        mtask.task = ktask;
                                    }
                                    // TODO add max trycount, avoid the unhealth session block the migrating
                                    mtask.tryCount++;
                                }
                            }

                            // check all migrating task
                            if (slotState.migratingTasks.isEmpty()) {
                                LOGGER.warn("sessionNodes is empty when migrating, {}", slot);
                            }

                            if (slotState.migratingTasks.values().stream().allMatch(m -> m.task.isSuccess())) {
                                // after migrated, force to update the version
                                // make sure the version is newly than old leader's
                                localDatumStorage.updateVersion(slot.getId());
                                slotState.migrated = true;
                                final long span = System.currentTimeMillis() - slotState.migratingStartTime;
                                LOGGER.info("slot migrating finish {}, span={}, sessions={}", slot, span,
                                        slotState.migratingTasks.keySet());
                                slotState.migratingTasks.clear();
                                observeLeaderMigratingFinish(slot.getId());
                                observeLeaderMigratingHistogram(slot.getId(), span);
                            }else{
                                MIGRATING_LOGGER.info("[migrating]{},{},{}", slot.getId(),
                                        System.currentTimeMillis() - slotState.migratingStartTime, sessions.size());
                            }
                        }
                    } else {
                        // sync leader
                        if (syncLeaderTask == null ||
                                syncLeaderTask.isOverAfter(dataServerConfig.getSlotFollowerSyncLeaderIntervalMs())) {
                            SyncLeaderTask task = new SyncLeaderTask(slotTableEpoch, slot);
                            slotState.syncLeaderTask = syncLeaderExecutor.execute(slot.getId(), task);
                        } else if(!syncLeaderTask.isFinished()){
                            // the sync leader is running or waiting, check next round
                            LOGGER.info("sync-leader running, {}", syncLeaderTask);
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("failed to do sync watching", e);
            }
        }

        @Override
        public int getWaitingMillis() {
            return 200;
        }
    }

    private final class SlotState {
        volatile Slot                                 slot;
        volatile boolean                              migrated;
        long                                          migratingStartTime;
        final Map<String, MigratingTask>              migratingTasks   = Maps.newHashMap();
        final Map<String, KeyedTask<SyncSessionTask>> syncSessionTasks = Maps.newHashMap();
        KeyedTask<SyncLeaderTask>                     syncLeaderTask;

        SlotState(Slot slot) {
            this.slot = slot;
        }

        void update(Slot s) {
            if (slot.getLeaderEpoch() != s.getLeaderEpoch()) {
                this.migrated = false;
                this.syncSessionTasks.clear();
                this.migratingTasks.clear();
                this.migratingStartTime = 0;
                if (localIsLeader(s)) {
                    // leader change
                    observeLeaderUpdateCounter();
                }
                LOGGER.info("update slot with leaderEpoch, exist={}, now={}", slot, s);
            }
            this.slot = s;
            LOGGER.info("update slot, slot={}", slot);
        }

        KeyedTask<SyncSessionTask> commitSyncSessionTask(long slotTableEpoch, String sessionIp,
                                                         boolean migrate) {
            SyncSessionTask task = new SyncSessionTask(slotTableEpoch, slot, sessionIp);
            if (migrate) {
                // group by slotId and session
                return migrateSessionExecutor.execute(new Tuple(slot.getId(), sessionIp), task);
            } else {
                // to a session node, at most there is 4 tasks running, avoid too many task hit the same session
                return syncSessionExecutor.execute(new Tuple((slot.getId() % 4), sessionIp), task);
            }
        }
    }

    private static class MigratingTask {
        final long                 createTimestamp = System.currentTimeMillis();
        KeyedTask<SyncSessionTask> task;
        int                        tryCount;

        MigratingTask(KeyedTask<SyncSessionTask> task) {
            this.task = task;
        }
    }

    private final class SyncSessionTask implements Runnable {
        final long   slotTableEpoch;
        final Slot   slot;
        final String sessionIp;

        SyncSessionTask(long slotTableEpoch, Slot slot, String sessionIp) {
            this.slotTableEpoch = slotTableEpoch;
            this.slot = slot;
            this.sessionIp = sessionIp;
        }

        public void run() {
            try {
                SlotDiffSyncer syncer = new SlotDiffSyncer(dataServerConfig, localDatumStorage,
                    dataChangeEventCenter, sessionLeaseManager);
                syncer.syncSession(slot.getId(), sessionIp, sessionNodeExchanger, slotTableEpoch,
                    new SyncContinues() {
                        @Override
                        public boolean continues() {
                            // if not leader, the syncing need to break
                            return isLeader(slot.getId());
                        }
                    });
            } catch (Throwable e) {
                LOGGER.error("sync session failed: {}, slot={}", sessionIp, slot.getId(), e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return "SyncSessionTask{" + "slotTableEpoch=" + slotTableEpoch + ", sessioIp='"
                   + sessionIp + '\'' + ", slot=" + slot + '}';
        }
    }

    private final class SyncLeaderTask implements Runnable {
        final long slotTableEpoch;
        final Slot slot;

        SyncLeaderTask(long slotTableEpoch, Slot slot) {
            this.slotTableEpoch = slotTableEpoch;
            this.slot = slot;
        }

        @Override
        public void run() {
            try {
                //sync leader no need to notify event
                SlotDiffSyncer syncer = new SlotDiffSyncer(dataServerConfig, localDatumStorage,
                    null, sessionLeaseManager);
                syncer.syncSlotLeader(slot.getId(), slot.getLeader(), dataNodeExchanger,
                    slotTableEpoch, new SyncContinues() {
                        @Override
                        public boolean continues() {
                            return isFollower(slot.getId());
                        }
                    });
            } catch (Throwable e) {
                LOGGER.error("sync leader failed: {}, slot={}", slot.getLeader(), slot.getId(), e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return "SyncLeaderTask{" + "slotTableEpoch=" + slotTableEpoch + ", slot=" + slot + '}';
        }
    }

    @Override
    public void triggerUpdateSlotTable(long expectEpoch) {
        // TODO
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
}
