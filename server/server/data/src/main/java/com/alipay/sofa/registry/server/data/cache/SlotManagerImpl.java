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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-02 15:25 yuzhi.lyz Exp $
 */
public final class SlotManagerImpl implements SlotManager {
    private static final Logger                  LOGGER          = LoggerFactory
                                                                     .getLogger(SlotManagerImpl.class);

    private final SlotFunction                   slotFunction    = new MD5SlotFunction();

    @Autowired
    private SessionServerCache                   sessionServerCache;

    @Autowired
    private SessionServerConnectionFactory       sessionServerConnectionFactory;

    @Autowired
    private Exchange                             boltExchange;

    @Autowired
    private DataNodeExchanger                    dataNodeExchanger;

    @Autowired
    private DataServerConfig                     dataServerConfig;

    private SlotManager.SlotDatumStorageProvider slotDatumStorageProvider;
    private final Lock                           lock            = new ReentrantLock();
    private volatile SlotState                   slotState       = SlotState.INIT;
    private final List<SlotChangeListener>       changeListeners = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService             migratingScheduler;
    private ExecutorService                      syncSessionExecutor;
    private ExecutorService                      syncLeaderExecutor;

    private final Map<Integer, MigratingTask>    migratingTasks  = new TreeMap<>();
    /**
     * the sync and migrating may happen parallelly when slot role has modified.
     * make sure the datum merging is idempotent
     */
    private final Map<Integer, SyncState>        syncStates      = new ConcurrentHashMap<>();
    private SyncingWatchDog                      watchDog;

    @PostConstruct
    public void init() {
        this.migratingScheduler = createScheduler("Migrating",
            dataServerConfig.getSlotMigratingExecutorThreadSize());

        this.syncSessionExecutor = createExecutor("SyncSession",
            dataServerConfig.getSlotLeaderSyncSessionExecutorThreadSize(),
            dataServerConfig.getSlotLeaderSyncSessionExecutorQueueSize());

        this.syncLeaderExecutor = createExecutor("SyncLeader",
            dataServerConfig.getSlotFollowerSyncLeaderExecutorThreadSize(),
            dataServerConfig.getSlotFollowerSyncLeaderExecutorQueueSize());

        watchDog = new SyncingWatchDog();
        Thread t = new Thread(watchDog, "SyncingWatchDog");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public SlotAccess checkSlotAccess(String dataInfoId, long srcSlotEpoch) {
        final SlotState curState = slotState;
        final long currentEpoch = curState.table.getEpoch();
        // if srcSlotEpoch<0, ignore the epoch check
        if (srcSlotEpoch >= 0 && currentEpoch < srcSlotEpoch) {
            triggerUpdateSlotTable(srcSlotEpoch);
        }

        final int slotId = slotFunction.slotOf(dataInfoId);
        final Slot targetSlot = curState.table.getSlot(slotId);
        if (targetSlot == null || !DataServerConfig.IP.equals(targetSlot.getLeader())) {
            return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Moved);
        }
        if (curState.migrating.contains(slotId)) {
            return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Migrating);
        }
        return new SlotAccess(slotId, currentEpoch, SlotAccess.Status.Accept);
    }

    @Override
    public int slotOf(String dataInfoId) {
        return slotFunction.slotOf(dataInfoId);
    }

    private boolean isLeader(Slot slot) {
        return DataServerConfig.IP.equals(slot.getLeader());
    }

    @Override
    public boolean updateSlotTable(SlotTable slotTable) {
        // avoid the update concurrent
        lock.lock();
        try {
            final SlotState curState = slotState;
            final long curEpoch = curState.table.getEpoch();
            if (curEpoch >= slotTable.getEpoch()) {
                return false;
            }

            LOGGER.info("updating slot table, expect={}, current={}, migrating={}", slotTable.getEpoch(), curEpoch,
                    curState.migrating);

            final SlotState newState = new SlotState(slotTable, curState.migrating);

            final Map<Integer, Slot> added = SlotTable.getSlotsAdded(curState.table, slotTable);
            // mark the new leader is migrating
            newState.migrating.addAll(added.keySet());
            LOGGER.info("add slots, {}", added.keySet());

            final Map<Integer, Slot> deleted = SlotTable.getSlotsDeleted(curState.table, slotTable);
            newState.migrating.removeAll(deleted.keySet());
            LOGGER.info("remove slots, {}", deleted.keySet());

            final Map<Integer, Slot> updated = SlotTable.getSlotUpdated(curState.table, slotTable);
            LOGGER.info("update slots, {}", updated.keySet());

            updated.forEach((slotId, slot) -> {
                final Slot curSlot = curState.table.getSlot(slotId);
                if (!isLeader(slot)) {
                    // not leader, try clean migrating if has set
                    newState.migrating.remove(slotId);
                    // as same as added.follower
                    added.put(slotId, slot);
                } else {
                    // is leader, check prev state
                    if (curSlot.getLeaderEpoch() != slot.getLeaderEpoch()) {
                        // new leader, as same as added.leader
                        newState.migrating.add(slotId);
                        added.put(slotId, slot);
                        LOGGER.info("slot updated with same leader, expect={}, current {}", slot, curSlot);
                    } else {
                        // the follower changes, ignore that, let the follower to pull data
                    }
                }
            });
            // update the current slotstate
            this.slotState = newState;
            LOGGER.info("new migrating={}", this.slotState.migrating);
            handleSlotsAdded(slotState, added);
            handleSlotsDeleted(slotState, deleted);
            return true;
        } finally {
            lock.unlock();
        }
    }

    // is locked by caller
    private void handleNewLeader(Slot slot) {
        MigratingTask task = migratingTasks.get(slot.getId());
        if (task != null) {
            // the slot has modified with same leader, may be A->B->, miss the B
            // do the migrating again
            if (slot.getLeaderEpoch() <= task.slot.getLeaderEpoch()) {
                LOGGER.info("dups migratingTask, existing={}, newly={}", task.slot, slot);
                return;
            }
            LOGGER.info("higher migratingTask, cancel existing={}, newly={}", task.slot, slot);
            // cancel the existing
            cancelMigratingTask(slot);
        }

        task = new MigratingTask(slot);
        migratingTasks.put(slot.getId(), task);
        migratingScheduler.schedule(task, 0, TimeUnit.SECONDS);
        watchDog.wakeup();
        LOGGER.info("new migratingTask, slot={}", slot);
    }

    private void cancelMigratingTask(Slot slot) {
        MigratingTask t = migratingTasks.remove(slot.getId());
        if (t != null) {
            t.cancel = true;
        }
    }

    private void handleNewFollower(Slot slot) {
        // if leader -> follower, cancel the migrating task
        cancelMigratingTask(slot);
        // TODO sync leader slot
    }

    private void handleSlotsDeleted(SlotState state, Map<Integer, Slot> slots) {
        slots.forEach((slotId, slot) -> {
            cancelMigratingTask(slot);
            changeListeners.forEach(listener -> {
                listener.onSlotRemove(slotId, isLeader(slot) ? Slot.Role.Leader : Slot.Role.Follower);
            });
        });
    }

    private void handleSlotsAdded(SlotState state, Map<Integer, Slot> slots) {
        slots.forEach((slotId, slot) -> {
            if (isLeader(slot)) {
                changeListeners.forEach(listener -> {
                    listener.onSlotAdd(slotId, Slot.Role.Leader);
                });
                handleNewLeader(slot);
            } else {
                changeListeners.forEach(listener -> {
                    listener.onSlotAdd(slotId, Slot.Role.Follower);
                });
                handleNewFollower(slot);
            }
        });
    }

    private enum TaskStatus {
        DOING, DONE,
    }

    private final class MigratingTask implements Runnable {
        final Slot                    slot;
        final Map<String, TaskStatus> sessionsSyncs = new ConcurrentHashMap<>();
        volatile boolean              cancel;
        final SlotDiffSyncer          syncer;

        MigratingTask(Slot slot) {
            this.slot = slot;
            this.syncer = new SlotDiffSyncer(boltExchange, dataNodeExchanger, dataServerConfig,
                slotDatumStorageProvider);
        }

        @Override
        public void run() {
            // trigger to fetch data from all session
            List<Connection> connections = sessionServerConnectionFactory.getSessionConnections();
            for (Connection conn : connections) {
                if (cancel) {
                    return;
                }
                String sessionIp = null;
                try {
                    //check connection active
                    if (!conn.isFine()) {
                        LOGGER.info("connection from sessionServer({}) is not fine when migrating，slot={}",
                                conn.getRemoteAddress(), slot.getId());
                        continue;
                    }
                    sessionIp = conn.getRemoteAddress().getAddress().getHostAddress();
                    if (sessionsSyncs.containsKey(sessionIp)) {
                        // the task in doing or done
                        continue;
                    }
                    MigratingSyncTask syncTask = new MigratingSyncTask(this, sessionIp, conn);
                    sessionsSyncs.put(sessionIp, TaskStatus.DOING);
                    migratingScheduler.submit(syncTask);
                } catch (Throwable e) {
                    if (sessionIp != null) {
                        sessionsSyncs.remove(sessionIp);
                    }
                    LOGGER.error("migrating failed: sessionServer({}), slot={}", conn.getRemoteAddress(), slot.getId(),
                            e);
                }
            }
            // check request finish?
            Map<String, SessionNode> sessions = sessionServerCache.getServerMap(dataServerConfig.getLocalDataCenter());
            if (sessions == null || sessions.isEmpty()) {
                LOGGER.warn("sessions is empty when migrating, {}", slot);
            } else {
                if (new ArrayList<>(sessionsSyncs.values()).stream().anyMatch(s -> s != TaskStatus.DOING)) {
                    // all done, now check all the session, may be some session is newly
                    if (new HashSet<>(sessionsSyncs.keySet()).containsAll(sessions.keySet())) {
                        // all finish, check the slotstate
                        lock.lock();
                        try {
                            final SlotState curState = slotState;
                            final Slot now = curState.table.getSlot(slot.getId());
                            // if now not contains the slot, it must be removed
                            if (now == null) {
                                LOGGER.info("slot remove when migrating finish, {}", slot);
                                return;
                            }
                            if (now.getLeaderEpoch() == slot.getLeaderEpoch()) {
                                migratingTasks.remove(slot.getId());
                                // clean migrating.flag at last step
                                curState.migrating.remove(slot.getId());
                                LOGGER.info("slot migrating finish, {}", slot);
                            } else {
                                // the slot leader has modified, ignore the task, another task is own the flag
                                LOGGER.info("the slotLeaderEpoch has modified in migrating, task={}, now={}", slot,
                                        now);
                            }
                            watchDog.wakeup();
                            return;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }
            LOGGER.info("migratingTask {}, sessions={}, status={}", slot, sessions.keySet(),
                    sessionsSyncs.keySet());
            // wait next sched
            if (!cancel) {
                migratingScheduler.schedule(this, 1, TimeUnit.SECONDS);
            }
        }
    }

    private final class MigratingSyncTask implements Runnable {
        final Connection    conn;
        final MigratingTask task;
        final String        sessionIp;

        MigratingSyncTask(MigratingTask task, String sessionIp, Connection conn) {
            this.task = task;
            this.conn = conn;
            this.sessionIp = sessionIp;
        }

        @Override
        public void run() {
            try {
                boolean success = task.syncer.syncSession(task.slot.getId(), conn,
                    slotState.table.getEpoch());
                if (success) {
                    task.sessionsSyncs.put(sessionIp, TaskStatus.DONE);
                    LOGGER.info("migratingTask finished from sessionServer({}), slot={}",
                        sessionIp, task.slot);
                } else {
                    task.sessionsSyncs.remove(sessionIp);
                    LOGGER.error("migratingTask failed from sessionServer({}), slot={}", sessionIp,
                        task.slot);
                }
            } catch (Throwable e) {
                task.sessionsSyncs.remove(sessionIp);
                LOGGER.error("migratingTask failed from sessionServer({}), slot={}", sessionIp,
                    task.slot, e);
            }
        }
    }

    private final class SyncState {
        final int                          slodId;
        // sessionIp -> task
        final Map<String, SyncSessionTask> syncSessionTasks = new HashMap<>();
        SyncLeaderTask                     syncLeaderTask;

        SyncState(int slotId) {
            this.slodId = slotId;
        }
    }

    abstract class SyncTask implements Runnable {
        final Slot    slot;
        final long    createTime = System.currentTimeMillis();
        volatile long startTime;
        volatile long endTime;

        SyncTask(Slot slot) {
            this.slot = slot;
        }

        abstract void run0();

        @Override
        public final void run() {
            startTime = System.currentTimeMillis();
            try {
                run0();
            } catch (Throwable e) {
                LOGGER.error("failed to run sync task", e);
            } finally {
                endTime = System.currentTimeMillis();
            }
        }

        boolean isScheduleable(int intervalMs) {
            if (endTime <= 0) {
                return false;
            }
            return System.currentTimeMillis() - endTime >= intervalMs;
        }

        boolean isFinished() {
            return endTime > 0;
        }
    }

    private final class SyncSessionTask extends SyncTask {
        final String     sessioIp;
        final Connection conn;

        SyncSessionTask(Slot slot, String sessioIp, Connection conn) {
            super(slot);
            this.sessioIp = sessioIp;
            this.conn = conn;
        }

        void run0() {
            try {
                SlotDiffSyncer syncer = new SlotDiffSyncer(boltExchange, dataNodeExchanger,
                    dataServerConfig, slotDatumStorageProvider);
                syncer.syncSession(slot.getId(), conn, slotState.table.getEpoch());
            } catch (Throwable e) {
                LOGGER.error("sync session failed: sessionServer({}), slot={}",
                    conn.getRemoteAddress(), slot.getId(), e);
            }
        }
    }

    private final class SyncLeaderTask extends SyncTask {
        SyncLeaderTask(Slot slot) {
            super(slot);
        }

        @Override
        void run0() {
            SlotDiffSyncer syncer = new SlotDiffSyncer(boltExchange, dataNodeExchanger,
                dataServerConfig, slotDatumStorageProvider);
            syncer.syncSlotLeader(slot.getId(), slot.getLeader(), slotState.table.getEpoch());
        }
    }

    private final class SyncingWatchDog implements Runnable {
        synchronized void wakeup() {
            this.notify();
        }

        @Override
        public void run() {
            for (; ; ) {
                try {
                    final SlotState curstate = slotState;
                    for (Slot slot : curstate.table.getSlots()) {
                        final Integer slotId = slot.getId();
                        if (curstate.migrating.contains(slotId)) {
                            // migrating, no need to sync
                            continue;
                        }
                        final SyncState state = syncStates.computeIfAbsent(slotId, k -> new SyncState(k));
                        final Map<String, Connection> sessions = sessionServerConnectionFactory
                                .getSessonConnectionMap();
                        if (isLeader(slot)) {
                            for (Map.Entry<String, Connection> session : sessions.entrySet()) {
                                SyncSessionTask task = state.syncSessionTasks.get(session.getKey());
                                if (task != null &&
                                        !task.isScheduleable(dataServerConfig.getSlotLeaderSyncSessionIntervalMs())) {
                                    continue;
                                } else {
                                    task = new SyncSessionTask(slot, session.getKey(), session.getValue());
                                    syncSessionExecutor.submit(task);
                                    state.syncSessionTasks.put(session.getKey(), task);
                                }
                            }
                        } else {
                            if (state.syncLeaderTask != null && !state.syncLeaderTask
                                    .isScheduleable(dataServerConfig.getSlotFollowerSyncLeaderIntervalMs())) {
                                // the sync leader is running or waiting, check next round
                                continue;
                            } else {
                                SyncLeaderTask task = new SyncLeaderTask(slot);
                                syncLeaderExecutor.submit(task);
                                state.syncLeaderTask = task;
                            }
                        }
                        // check the syncSessionTask, maybe the session is offline
                        Iterator<Map.Entry<String, SyncSessionTask>> iter = state.syncSessionTasks.entrySet()
                                .iterator();
                        while (iter.hasNext()) {
                            Map.Entry<String, SyncSessionTask> e = iter.next();
                            // make sure the task is finished
                            if (e.getValue().isFinished()) {
                                iter.remove();
                            }
                        }
                    }
                    // TODO check the long running task
                    synchronized (this) {
                        this.wait(1000);
                    }
                } catch (Throwable e) {
                    LOGGER.error("failed to do sync watching", e);
                }

            }
        }
    }

    private static final class SlotState {
        static final SlotState INIT      = new SlotState(new SlotTable(-1, Collections.emptyMap()),
                                             Collections.emptySet());
        final SlotTable        table;
        final Set<Integer>     migrating = new ConcurrentSkipListSet<>();

        SlotState(SlotTable table, Set<Integer> migratingSlots) {
            this.table = table;
            this.migrating.addAll(migratingSlots);
        }

    }

    @Override
    public void triggerUpdateSlotTable(long expectEpoch) {
        // TODO
    }

    public void setSlotDatumStorageProvider(SlotDatumStorageProvider slotDatumStorageProvider) {
        this.slotDatumStorageProvider = slotDatumStorageProvider;
    }

    @Override
    public void addSlotChangeListener(SlotChangeListener listener) {
        this.changeListeners.add(listener);
    }

    @Override
    public long getSlotTableEpoch() {
        return slotState.table.getEpoch();
    }

    private ScheduledExecutorService createScheduler(String name, int corePoolSize) {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        threadFactoryBuilder.setUncaughtExceptionHandler((t, err) -> {
            LOGGER.error("unexpect exception task in thread {}, err={}", t.getName(), err);
        });
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactoryBuilder
                .setNameFormat("Registry-SlotManager-Sched-" + name).build());
    }

    private ExecutorService createExecutor(String name, int corePoolSize, int queueSize) {
        return new ThreadPoolExecutor(corePoolSize, corePoolSize, 300, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(queueSize), new NamedThreadFactory(
                "Registry-SlotManager-Executor-" + name, true));
    }
}