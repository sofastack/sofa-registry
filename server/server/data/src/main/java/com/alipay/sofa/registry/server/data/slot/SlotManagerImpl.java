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
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor.KeyedTask;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
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

    private final List<SlotChangeListener>   slotChangeListeners = new ArrayList<>();

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
        this.syncSessionExecutor = new KeyedThreadPoolExecutor("Slot-Executor-SyncSession",
            dataServerConfig.getSlotLeaderSyncSessionExecutorThreadSize(),
            dataServerConfig.getSlotLeaderSyncSessionExecutorQueueSize());

        this.syncLeaderExecutor = new KeyedThreadPoolExecutor("Slot-Executor-SyncLeader",
            dataServerConfig.getSlotFollowerSyncLeaderExecutorThreadSize(),
            dataServerConfig.getSlotFollowerSyncLeaderExecutorQueueSize());

        SlotChangeListener l = localDatumStorage.getSlotChanngeListener();
        if (l != null) {
            this.slotChangeListeners.add(l);
        }
        watchDog = new SyncingWatchDog();
        ConcurrentUtils.createDaemonThread("SyncingWatchDog", watchDog).start();
    }

    @Override
    public SlotAccess checkSlotAccess(String dataInfoId, long srcSlotEpoch) {
        final long currentEpoch = slotTableStates.table.getEpoch();
        if (currentEpoch < srcSlotEpoch) {
            triggerUpdateSlotTable(srcSlotEpoch);
        }

        final int slotId = slotFunction.slotOf(dataInfoId);
        final SlotState state = slotTableStates.slotStates.get(slotId);
        if (state == null || !isLeader(state.slot)) {
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
        return state != null && isLeader(state.slot);
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

    private void updateSlotState(SlotTable updating) {
        updating.getSlots().forEach(s -> {
            SlotState state = slotTableStates.slotStates.get(s.getId());
            listenAdd(s);
            if (state != null) {
                state.update(s);
            } else {
                slotTableStates.slotStates.put(s.getId(), new SlotState(s));
            }
        });

        final Iterator<Map.Entry<Integer, SlotState>> it = slotTableStates.slotStates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, SlotState> e = it.next();
            if (updating.getSlot(e.getKey()) == null) {
                it.remove();
                listenRemove(e.getValue().slot);
            }
        }
        slotTableStates.table = updating;
    }

    private static final class SlotTableStates {
        volatile SlotTable            table      = SlotTable.INIT;
        final Map<Integer, SlotState> slotStates = Maps.newConcurrentMap();
    }

    private final class SyncingWatchDog extends LoopRunnable {
        private final Object bell = new Object();

        void wakeup() {
            synchronized (bell) {
                bell.notify();
            }
        }

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
                    if (isLeader(slot)) {
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
                                    task = slotState.commitSyncSessionTask(slotTableEpoch, sessionIp);
                                    slotState.syncSessionTasks.put(sessionIp, task);
                                }
                            }
                        } else {
                            for (String sessionIp : sessions) {
                                MigratingTask mtask = slotState.migratingTasks.get(sessionIp);
                                if (mtask == null || mtask.task.isFailed()) {
                                    KeyedTask<SyncSessionTask> ktask = slotState
                                            .commitSyncSessionTask(slotTableEpoch, sessionIp);
                                    if (mtask == null) {
                                        mtask = new MigratingTask(ktask);
                                        slotState.migratingTasks.put(sessionIp, mtask);
                                    } else {
                                        mtask.task = ktask;
                                    }
                                    // TODO add max trycount, avoid the unhealth session block the migrating
                                    mtask.tryCount++;
                                }
                            }
                            // check all migrating task
                            if (slotState.migratingTasks.isEmpty()) {
                                LOGGER.warn("sessionNodes is empty when migrating, {}", slot);
                                continue;
                            }
                            if (slotState.migratingTasks.values().stream().allMatch(m -> m.task.isSuccess())) {
                                slotState.migrated = true;
                                LOGGER.info("slot migrating finish {}, sessions={}", slot,
                                        slotState.migratingTasks.keySet());
                                slotState.migratingTasks.clear();
                            }
                        }
                    } else {
                        // sync leader
                        if (syncLeaderTask == null ||
                                syncLeaderTask.isOverAfter(dataServerConfig.getSlotFollowerSyncLeaderIntervalMs())) {
                            SyncLeaderTask task = new SyncLeaderTask(slotTableEpoch, slot);
                            slotState.syncLeaderTask = syncLeaderExecutor.execute(slot.getId(), task);
                        } else {
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
        public void waitingUnthrowable() {
            synchronized (bell) {
                if (updatingSlotTable.get() == null) {
                    ConcurrentUtils.objectWaitUninterruptibly(bell, 1000);
                }
            }
        }
    }

    private final class SlotState {
        volatile Slot                                 slot;
        volatile boolean                              migrated;
        final Map<String, MigratingTask>              migratingTasks   = Maps.newHashMap();
        final Map<String, KeyedTask<SyncSessionTask>> syncSessionTasks = Maps.newHashMap();
        KeyedTask<SyncLeaderTask>                     syncLeaderTask;

        SlotState(Slot slot) {
            this.slot = slot;
        }

        synchronized void update(Slot s) {
            if (slot.getLeaderEpoch() != s.getLeaderEpoch()) {
                this.migrated = false;
                this.syncSessionTasks.clear();
                this.migratingTasks.clear();
            }
            this.slot = s;
        }

        KeyedTask<SyncSessionTask> commitSyncSessionTask(long slotTableEpoch, String sessionIp) {
            SyncSessionTask task = new SyncSessionTask(slotTableEpoch, slot, sessionIp);
            return syncSessionExecutor.execute(new Tuple(slot.getId(), sessionIp), task);
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
        final String sessioIp;

        SyncSessionTask(long slotTableEpoch, Slot slot, String sessioIp) {
            this.slotTableEpoch = slotTableEpoch;
            this.slot = slot;
            this.sessioIp = sessioIp;
        }

        public void run() {
            try {
                SlotDiffSyncer syncer = new SlotDiffSyncer(dataServerConfig, localDatumStorage,
                    dataChangeEventCenter, sessionLeaseManager);
                syncer.syncSession(slot.getId(), sessioIp, sessionNodeExchanger, slotTableEpoch);
            } catch (Throwable e) {
                LOGGER.error("sync session failed: {}, slot={}", sessioIp, slot.getId(), e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return "SyncSessionTask{" + "slotTableEpoch=" + slotTableEpoch + ", sessioIp='"
                   + sessioIp + '\'' + ", slot=" + slot + '}';
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
                    slotTableEpoch);
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
        return isLeader(s) ? Slot.Role.Leader : Slot.Role.Follower;
    }

    private void listenAdd(Slot s) {
        slotChangeListeners.forEach(listener -> listener.onSlotAdd(s.getId(), getRole(s)));
    }

    private void listenRemove(Slot s) {
        slotChangeListeners.forEach(listener -> listener.onSlotRemove(s.getId(), getRole(s)));
    }

    private static boolean isLeader(Slot slot) {
        return ServerEnv.isLocalServer(slot.getLeader());
    }
}
