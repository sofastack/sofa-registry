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
import com.alipay.remoting.util.ConcurrentHashSet;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataSlotMigrateRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataSlotMigrateResult;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.notify.SessionServerNotifier;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.MetaNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.task.Task;
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
    private DataServerConfig                     dataServerConfig;

    private SlotManager.SlotDatumStorageProvider slotDatumStorageProvider;
    private final Lock                           lock            = new ReentrantLock();
    private volatile SlotState                   slotState       = SlotState.INIT;
    private final List<SlotChangeListener>       changeListeners = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService             migratingScheduler;

    private final Map<Integer, MigratingTask>    migratingTasks  = new TreeMap<>();

    @PostConstruct
    public void init() {
        this.migratingScheduler = createMigratingScheduler(dataServerConfig
            .getSlotMigratingExecutorThreadSize());
    }

    private ScheduledExecutorService createMigratingScheduler(int corePoolSize) {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        threadFactoryBuilder.setUncaughtExceptionHandler((t, err) -> {
            LOGGER.error("unexpect exception migrating task in thread {}, err={}", t.getName(), err);
        });
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactoryBuilder
                .setNameFormat("Registry-SlotManager-ExecutorForMigrating").build());
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
                LOGGER.info("epoch has updated, expect={}, current={}", slotTable.getEpoch(), curEpoch);
                return false;
            }

            final SlotState newState = new SlotState(slotTable, curState.migrating);
            final Map<Integer, Slot> added = SlotTable.getSlotsAdded(curState.table, slotTable);
            // mark the new leader is migrating
            newState.migrating.addAll(added.keySet());

            final Map<Integer, Slot> deleted = SlotTable.getSlotsDeleted(curState.table, slotTable);
            newState.migrating.removeAll(deleted.keySet());

            final Map<Integer, Slot> updated = SlotTable.getSlotUpdated(curState.table, slotTable);
            updated.forEach((slotId, slot) -> {
                final Slot curSlot = curState.table.getSlot(slotId);
                if (!isLeader(slot)) {
                    // not leader, clean migrating if has set
                    newState.migrating.remove(slotId);
                    // as same as added.follower
                    added.put(slotId, slot);
                } else {
                    // is leader, check prev state
                    // new leader
                    if (curSlot.getLeaderEpoch() != slot.getLeaderEpoch()) {
                        newState.migrating.add(slotId);
                        // as same as added.leader
                        added.put(slotId, slot);
                    } else {
                        // the follower changes, ignore that, let the follower to pull data
                    }
                }
            });
            // update the current slotstate
            this.slotState = newState;
            handleSlotsAdded(slotState, added);
            handleSlotsDeleted(slotState, deleted);
            return true;
        } finally {
            lock.unlock();
        }
    }

    // is lock by caller
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
            task.cancel = true;
        }

        task = new MigratingTask(slot);
        migratingTasks.put(slot.getId(), task);
        migratingScheduler.schedule(task, 0, TimeUnit.SECONDS);
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

    private final class MigratingTaskCallback implements CallbackHandler {
        final String        sessionIp;
        final MigratingTask task;

        MigratingTaskCallback(MigratingTask task, String sessionIp) {
            this.sessionIp = sessionIp;
            this.task = task;
        }

        @Override
        public void onCallback(Channel channel, Object message) {
            try {
                GenericResponse<DataSlotMigrateResult> resp = (GenericResponse<DataSlotMigrateResult>) message;
                if (resp == null || !resp.isSuccess()) {
                    task.sessionsSyncs.remove(sessionIp);
                    LOGGER.error(
                        "response not success when migrating sessionServer({}), slot={}, resp={}",
                        sessionIp, task.slot, resp);
                    return;
                }
                DataSlotMigrateResult result = resp.getData();
                if (result != null) {
                    slotDatumStorageProvider.merge(task.slot.getId(),
                        dataServerConfig.getLocalDataCenter(), result.getUpdatedPublishers(),
                        result.getRemovedPublishers());
                    LOGGER
                        .info(
                            "migratingTask merge publishers from sessionServer({}), slot={}, update={}, removed={}",
                            sessionIp, task.slot, result.getUpdatedPublishers().size(), result
                                .getRemovedPublishers().size());
                }
                task.sessionsSyncs.put(sessionIp, TaskStatus.DONE);
                LOGGER.info("migratingTask finished from sessionServer({}), slot={}", sessionIp,
                    task.slot);
            } catch (Throwable e) {
                task.sessionsSyncs.remove(sessionIp);
                LOGGER.error(
                    "response callback failed when migrating sessionServer({}), slot={}, resp={}",
                    channel, task.slot);
            }
        }

        @Override
        public void onException(Channel channel, Throwable exception) {
            final String remoteIp = channel.getRemoteAddress().getAddress().getHostAddress();
            task.sessionsSyncs.remove(remoteIp);
            LOGGER.error("migrating failed: sessionServer({}), slot={}", remoteIp, task.slot,
                exception);
        }

        @Override
        public Executor getExecutor() {
            return ExecutorFactory.MIGRATING_SESSION_CALLBACK_EXECUTOR;
        }
    }

    private final class MigratingTask implements Runnable {
        final Slot                    slot;
        final Map<String, TaskStatus> sessionsSyncs = new ConcurrentHashMap<>();
        volatile boolean              cancel;

        MigratingTask(Slot slot) {
            this.slot = slot;
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
                    Map<String, DatumSummary> summarys = slotDatumStorageProvider
                            .getDatumSummary(slot.getId(), dataServerConfig.getLocalDataCenter(), sessionIp);
                    DataSlotMigrateRequest request = new DataSlotMigrateRequest(slot.getLeaderEpoch(), slot.getId(),
                            summarys);
                    Server sessionServer = boltExchange.getServer(dataServerConfig.getPort());
                    sessionsSyncs.put(sessionIp, TaskStatus.DOING);
                    sessionServer.sendCallback(sessionServer.getChannel(conn.getRemoteAddress()), request,
                            new MigratingTaskCallback(MigratingTask.this, sessionIp), dataServerConfig.getRpcTimeout());
                } catch (Throwable e) {
                    if (sessionIp != null) {
                        sessionsSyncs.remove(sessionIp);
                    }
                    LOGGER.error("migrating failed: sessionServer({}), slot={}",
                            conn.getRemoteAddress(), slot.getId(), e);
                }
            }
            // check request finish?
            Map<String, SessionNode> sessions = sessionServerCache.getServerMap(dataServerConfig.getLocalDataCenter());
            if (sessions == null || sessions.isEmpty()) {
                LOGGER.warn("sessions is empty when migrating, {}", slot);
            }else {
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
                            } else {
                                // the slot leader has modified, ignore the task, another task is own the flag
                                LOGGER.info("the slotLeaderEpoch has modified in migrating, task={}, now={}", slot,
                                        now);
                            }
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

    private static final class SlotState {
        static final SlotState INIT      = new SlotState(new SlotTable(-1, Collections.emptyMap()),
                                             Collections.emptySet());
        final SlotTable        table;
        final Set<Integer>     migrating = new ConcurrentHashSet<>();

        SlotState(SlotTable table, Set<Integer> migratingSlots) {
            this.table = table;
            this.migrating.addAll(migratingSlots);
        }

    }

    private void triggerUpdateSlotTable(long expectEpoch) {
        // TODO
    }

    public void setSlotDatumStorageProvider(SlotDatumStorageProvider slotDatumStorageProvider) {
        this.slotDatumStorageProvider = slotDatumStorageProvider;
    }

    @Override
    public void addSlotChangeListener(SlotChangeListener listener) {
        this.changeListeners.add(listener);
    }
}