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
package com.alipay.sofa.registry.server.data.multi.cluster.slot;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.console.MultiSegmentSyncSwitch;
import com.alipay.sofa.registry.common.model.constants.MultiValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.Slot.Role;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.filter.MultiSyncDataAcceptorManager;
import com.alipay.sofa.registry.common.model.slot.filter.SyncAcceptorRequest;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptor;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotDataInfoIdAcceptor;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.exception.UnSupportOperationException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.multi.cluster.exchanger.RemoteDataNodeExchanger;
import com.alipay.sofa.registry.server.data.multi.cluster.executor.MultiClusterExecutorManager;
import com.alipay.sofa.registry.server.data.multi.cluster.loggers.Loggers;
import com.alipay.sofa.registry.server.data.multi.cluster.sync.info.FetchMultiSyncService;
import com.alipay.sofa.registry.server.data.slot.SlotChangeListenerManager;
import com.alipay.sofa.registry.server.data.slot.SlotDiffSyncer;
import com.alipay.sofa.registry.server.data.slot.SlotManagerImpl;
import com.alipay.sofa.registry.server.data.slot.SlotManagerImpl.ISlotState;
import com.alipay.sofa.registry.server.data.slot.SyncContinues;
import com.alipay.sofa.registry.server.data.slot.SyncLeaderTask;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.task.KeyedTask;
import com.alipay.sofa.registry.util.AtomicSet;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotManagerImpl.java, v 0.1 2022年05月05日 21:23 xiaojian.xj Exp $
 */
public class MultiClusterSlotManagerImpl implements MultiClusterSlotManager {

  private static final Logger MULTI_CLUSTER_SLOT_TABLE = Loggers.MULTI_CLUSTER_SLOT_TABLE;

  private static final Logger MULTI_CLUSTER_CLIENT_LOGGER = Loggers.MULTI_CLUSTER_CLIENT_LOGGER;

  private static final Logger MULTI_CLUSTER_SYNC_DIGEST_LOGGER =
      Loggers.MULTI_CLUSTER_SYNC_DIGEST_LOGGER;

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private MultiClusterDataServerConfig multiClusterDataServerConfig;

  @Autowired private DataChangeEventCenter dataChangeEventCenter;

  @Resource private DatumStorageDelegate datumStorageDelegate;

  @Autowired private SlotChangeListenerManager slotChangeListenerManager;

  @Autowired private RemoteDataNodeExchanger remoteDataNodeExchanger;

  @Autowired private MultiClusterExecutorManager multiClusterExecutorManager;

  @Autowired private MultiSyncDataAcceptorManager multiSyncDataAcceptorManager;

  @Autowired private FetchMultiSyncService fetchMultiSyncService;

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  private static final Map<String, RemoteSlotTableStorage> slotTableStorageMap =
      Maps.newConcurrentMap();

  private final SlotFunction slotFunction = SlotFunctionRegistry.getFunc();

  private final RemoteSyncingWatchDog watchDog = new RemoteSyncingWatchDog();

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("RemoteSyncingWatchDog", watchDog).start();
  }

  @Override
  public int slotOf(String dataInfoId) {
    return slotFunction.slotOf(dataInfoId);
  }

  @Override
  public Slot getSlot(String dataCenter, int slotId) {
    RemoteSlotTableStorage remoteSlotTableStorage = slotTableStorageMap.get(dataCenter);
    if (remoteSlotTableStorage == null) {
      return null;
    }

    final RemoteSlotTableStates states = remoteSlotTableStorage.slotTableStates;
    if (states == null) {
      return null;
    }
    RemoteSlotStates state = states.slotStates.get(slotId);
    return state == null ? null : state.slot;
  }

  @Override
  public SlotAccess checkSlotAccess(
      String dataCenter, int slotId, long srcSlotEpoch, long srcLeaderEpoch) {
    RemoteSlotTableStorage remoteSlotTableStorage = slotTableStorageMap.get(dataCenter);
    if (remoteSlotTableStorage == null) {
      return null;
    }

    final RemoteSlotTableStates states = remoteSlotTableStorage.slotTableStates;
    if (states == null) {
      return new SlotAccess(slotId, SlotTable.INIT.getEpoch(), SlotAccess.Status.UnSupport, -1);
    }
    Tuple<SlotTable, RemoteSlotStates> tuple = states.get(slotId);

    return SlotManagerImpl.checkSlotAccess(slotId, tuple.o1.getEpoch(), tuple.o2, srcLeaderEpoch);
  }

  private static boolean localIsLeader(Slot slot) {
    return ServerEnv.isLocalServer(slot.getLeader());
  }

  @Override
  public boolean isLeader(String dataCenter, int slotId) {
    RemoteSlotTableStorage remoteSlotTableStorage = slotTableStorageMap.get(dataCenter);
    if (remoteSlotTableStorage == null) {
      return false;
    }

    final RemoteSlotTableStates states = remoteSlotTableStorage.slotTableStates;
    if (states == null) {
      return false;
    }
    RemoteSlotStates state = states.get(slotId).o2;
    return state != null && localIsLeader(state.slot);
  }

  @Override
  public boolean isFollower(String dataCenter, int slotId) {
    throw new UnSupportOperationException(
        StringFormatter.format("MultiClusterSlotManagerImpl.isFollower {}/{}", dataCenter, slotId));
  }

  private final class RemoteSlotTableStorage {
    private final RemoteSlotTableStates slotTableStates;

    private final AtomicReference<SlotTable> updating;

    public RemoteSlotTableStorage(
        RemoteSlotTableStates slotTableStates, AtomicReference<SlotTable> updating) {
      this.slotTableStates = slotTableStates;
      this.updating = updating;
    }
  }

  private final class RemoteSlotTableStates {
    private final ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = updateLock.writeLock();
    private final ReentrantReadWriteLock.ReadLock readLock = updateLock.readLock();

    final String dataCenter;
    // save only slot belong to us
    volatile SlotTable slotTable = SlotTable.INIT;

    // map<slotId, RemoteSlotStates>
    final Map<Integer, RemoteSlotStates> slotStates = Maps.newConcurrentMap();

    public RemoteSlotTableStates(String dataCenter) {
      this.dataCenter = dataCenter;
    }

    boolean updateSlotState(SlotTable update) {
      writeLock.lock();
      try {
        for (Slot slot : update.getSlots()) {
          listenAdd(dataCenter, slot);
          RemoteSlotStates state =
              slotStates.computeIfAbsent(
                  slot.getId(),
                  k -> {
                    MULTI_CLUSTER_SLOT_TABLE.info("[updateSlotState]add slot={}", dataCenter, slot);
                    return new RemoteSlotStates(dataCenter, slot);
                  });
          state.update(slot);
        }

        final Iterator<Entry<Integer, RemoteSlotStates>> it = slotStates.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<Integer, RemoteSlotStates> e = it.next();
          if (update.getSlot(e.getKey()) == null) {
            final Slot slot = e.getValue().slot;
            it.remove();
            // first remove the slot for GetData Access check, then clean the data
            listenRemove(dataCenter, slot);
            MultiClusterSlotMetrics.observeRemoteLeaderSyncingFinish(dataCenter, slot.getId());
            MULTI_CLUSTER_SLOT_TABLE.info("dataCenter={} remove slot, slot={}", dataCenter, slot);
          }
        }
        this.slotTable = update;
        MultiClusterSlotMetrics.observeRemoteLeaderAssignGauge(
            dataCenter, this.slotTable.getLeaderNum(ServerEnv.IP));
      } catch (Throwable t) {
        MULTI_CLUSTER_SLOT_TABLE.error("[updateSlotTable]update slot table:{} error.", update, t);
        return false;
      } finally {
        writeLock.unlock();
      }
      return true;
    }

    RemoteSlotStates getSlotStates(int slotId) {
      readLock.lock();
      try {
        return slotStates.get(slotId);
      } finally {
        readLock.unlock();
      }
    }

    Tuple<SlotTable, RemoteSlotStates> get(int slotId) {
      SlotTable table;
      RemoteSlotStates state;
      readLock.lock();
      try {
        table = slotTable;
        state = slotStates.get(slotId);
      } finally {
        readLock.unlock();
      }
      return new Tuple<>(table, state);
    }

    private void listenAdd(String dataCenter, Slot s) {
      slotChangeListenerManager
          .remoteListeners()
          .forEach(listener -> listener.onSlotAdd(dataCenter, s.getId(), Role.Leader));
    }

    private void listenRemove(String dataCenter, Slot s) {
      slotChangeListenerManager
          .remoteListeners()
          .forEach(listener -> listener.onSlotRemove(dataCenter, s.getId(), Role.Leader));
    }
  }

  private final class SyncDataIdTask extends SyncLeaderTask {

    private final Set<String> syncing;

    public SyncDataIdTask(
        String localDataCenter,
        String syncDataCenter,
        long slotTableEpoch,
        Slot slot,
        SlotDiffSyncer syncer,
        Set<String> syncing,
        ClientSideExchanger clientSideExchanger,
        SyncContinues continues,
        Logger syncDigestLogger,
        Logger syncErrorLogger) {
      super(
          localDataCenter,
          syncDataCenter,
          slotTableEpoch,
          slot,
          syncer,
          clientSideExchanger,
          continues,
          syncDigestLogger,
          syncErrorLogger);
      this.syncing = syncing;
    }

    /**
     * Getter method for property <tt>syncing</tt>.
     *
     * @return property value of syncing
     */
    public Set<String> getSyncing() {
      return syncing;
    }
  }

  private static final class RemoteSlotStates implements ISlotState {
    final String remoteDataCenter;
    final int slotId;
    volatile Slot slot;
    volatile boolean synced;
    volatile long lastSuccessSyncRemoteTime = -1L;
    volatile KeyedTask<SyncLeaderTask> syncRemoteTask;

    AtomicSet<String> pendingDataInfoIds = new AtomicSet<>();
    volatile long lastSuccessDataInfoIdTime = -1L;
    volatile KeyedTask<SyncDataIdTask> syncDataIdTask;

    RemoteSlotStates(String remoteDataCenter, Slot slot) {
      this.remoteDataCenter = remoteDataCenter;
      this.slotId = slot.getId();
      this.slot = slot;
    }

    void update(Slot update) {
      ParaCheckUtil.checkEquals(slotId, update.getId(), "slot.id");
      ParaCheckUtil.assertTrue(
          ServerEnv.isLocalServer(slot.getLeader()),
          StringFormatter.format("{} is not equal leader={}", ServerEnv.IP, slot.getLeader()));
      if (slot.getLeaderEpoch() != update.getLeaderEpoch()) {
        this.synced = false;
        this.lastSuccessSyncRemoteTime = -1L;
        this.syncRemoteTask = null;

        // clear task when slot update;
        // the SyncLeaderTask will sync all;
        this.pendingDataInfoIds.getAndReset();
        this.lastSuccessDataInfoIdTime = -1L;
        this.syncDataIdTask = null;
      }
      this.slot = update;
      MULTI_CLUSTER_SLOT_TABLE.info(
          "remoteDataCenter={}, update slot={}", remoteDataCenter, update);
    }

    void completeSyncRemoteLeaderTask() {
      if (syncRemoteTask != null && syncRemoteTask.isSuccess()) {
        this.lastSuccessSyncRemoteTime = syncRemoteTask.getEndTime();
        this.synced = true;
      }
    }

    void completeSyncRemoteDataIdTask() {
      if (syncDataIdTask == null || !syncRemoteTask.isFinished()) {
        return;
      }

      if (syncRemoteTask.isSuccess()) {
        this.lastSuccessDataInfoIdTime = syncDataIdTask.getEndTime();
      } else {
        // sync dataIds fail, pending to sync in next task
        addPending(syncDataIdTask.getRunnable().getSyncing());
      }
    }

    public synchronized void addPending(String dataInfoId) {
      pendingDataInfoIds.add(dataInfoId);
    }

    public synchronized void addPending(Set<String> dataInfoIds) {
      pendingDataInfoIds.addAll(dataInfoIds);
    }

    public synchronized boolean hasPending() {
      return pendingDataInfoIds.size() > 0;
    }

    @Override
    public boolean isMigrated() {
      return this.synced;
    }

    @Override
    public Slot getSlot() {
      return this.slot;
    }
  }

  /**
   * get remote cluster slotTable epoch
   *
   * @return map<cluster, slotTableEpoch>
   */
  @Override
  public Map<String, Long> getSlotTableEpoch() {
    Map<String, Long> slotTableEpochMap =
        Maps.newHashMapWithExpectedSize(slotTableStorageMap.size());
    for (Entry<String, RemoteSlotTableStorage> entry : slotTableStorageMap.entrySet()) {

      long epoch;
      if (entry.getValue() == null || entry.getValue().slotTableStates == null) {
        epoch = SlotTable.INIT.getEpoch();
      } else {
        epoch = entry.getValue().slotTableStates.slotTable.getEpoch();
      }
      slotTableEpochMap.put(entry.getKey(), epoch);
    }
    return slotTableEpochMap;
  }

  /**
   * 1.add new dataCenter slot table to remoteSlotTableStates 2.update exist dataCenter slot table
   * 3.important: remove slot table which not exist in meta
   *
   * @param remoteSlotTableStatus
   */
  @Override
  public void updateSlotTable(Map<String, RemoteSlotTableStatus> remoteSlotTableStatus) {

    Set<String> tobeRemove =
        Sets.difference(slotTableStorageMap.keySet(), remoteSlotTableStatus.keySet());

    boolean wakeup = false;
    for (Entry<String, RemoteSlotTableStatus> statusEntry : remoteSlotTableStatus.entrySet()) {
      RemoteSlotTableStatus slotTableStatus = statusEntry.getValue();
      if (slotTableStatus.isSlotTableEpochConflict()) {
        // local.slotTableEpoch > meta.slotTableEpoch
        // it should noe happen, print error log and restart data server;
        MULTI_CLUSTER_SLOT_TABLE.error(
            "[updateSlotTable]meta remote slot table status conflict: {}", slotTableStatus);
        continue;
      }

      String dataCenter = statusEntry.getKey();

      RemoteSlotTableStorage storage =
          slotTableStorageMap.computeIfAbsent(
              dataCenter,
              k ->
                  new RemoteSlotTableStorage(
                      new RemoteSlotTableStates(dataCenter), new AtomicReference<>()));
      SlotTable curSlotTable = storage.slotTableStates.slotTable;
      // upgrade=false, slot table not change
      // upgrade=true, but data had accept a bigger version than return value
      if (!slotTableStatus.isSlotTableUpgrade()
          || curSlotTable.getEpoch() >= slotTableStatus.getSlotTableEpoch()) {
        continue;
      }

      // check updating slot table
      AtomicReference<SlotTable> updating = storage.updating;
      SlotTable updatingSlotTable = updating.get();
      if (updatingSlotTable != null
          && updatingSlotTable.getEpoch() >= slotTableStatus.getSlotTableEpoch()) {
        continue;
      }
      // filter slot belong to me
      SlotTable toBeUpdate = slotTableStatus.getSlotTable().filter(ServerEnv.IP);

      if (!checkSlot(curSlotTable, updatingSlotTable, toBeUpdate)) {
        continue;
      }

      if (updating.compareAndSet(updatingSlotTable, toBeUpdate)) {
        wakeup = true;
        MULTI_CLUSTER_SLOT_TABLE.info(
            "updating slot table, dataCenter={}, new={}, current={}",
            dataCenter,
            toBeUpdate,
            curSlotTable);
      }
    }
    if (wakeup) {
      watchDog.wakeup();
    }

    processRemove(tobeRemove);
  }

  private void processRemove(Set<String> tobeRemove) {
    if (CollectionUtils.isEmpty(tobeRemove)) {
      return;
    }
    Set<MultiClusterSyncInfo> syncInfos = multiClusterSyncRepository.queryLocalSyncInfos();
    Set<String> syncing =
        syncInfos.stream()
            .map(MultiClusterSyncInfo::getRemoteDataCenter)
            .collect(Collectors.toSet());
    for (String remove : tobeRemove) {
      if (syncing.contains(remove)) {
        MULTI_CLUSTER_SLOT_TABLE.error("dataCenter:{} remove is forbidden.", remove);
        continue;
      }

      MULTI_CLUSTER_SLOT_TABLE.info("start to remove dataCenter:{} datum and slotTable.", remove);
      boolean removeSuccess = datumStorageDelegate.removeStorage(remove);
      if (!removeSuccess) {
        MULTI_CLUSTER_SLOT_TABLE.error("dataCenter:{} remove storage fail.", remove);
        continue;
      }
      slotTableStorageMap.remove(remove);
      MULTI_CLUSTER_SLOT_TABLE.info("remove dataCenter:{} datum and slotTable success.", remove);
    }
  }

  @Override
  public void dataChangeNotify(String dataCenter, Set<String> dataInfoIds) {
    if (!fetchMultiSyncService.multiSync(dataCenter)) {
      MULTI_CLUSTER_CLIENT_LOGGER.info(
          "[syncDisable]dataCenter: {} data change:{} notify", dataCenter, dataInfoIds);
      return;
    }

    Map<Integer, Set<String>> slotDataInfoIds = Maps.newHashMap();
    for (String dataInfoId : dataInfoIds) {
      SyncSlotAcceptorManager syncSlotAcceptorManager =
          multiSyncDataAcceptorManager.getSyncSlotAcceptorManager(dataCenter);
      if (syncSlotAcceptorManager == null
          || syncSlotAcceptorManager.accept(SyncAcceptorRequest.buildRequest(dataInfoId))) {
        MULTI_CLUSTER_CLIENT_LOGGER.info(
            "[NotAccept]dataCenter: {} data change:{} notify", dataCenter, dataInfoId);
        continue;
      }

      int slotId = slotOf(dataInfoId);
      Set<String> set = slotDataInfoIds.computeIfAbsent(slotId, k -> Sets.newHashSet());
      set.add(dataInfoId);
    }

    RemoteSlotTableStorage storage = slotTableStorageMap.get(dataCenter);
    if (storage == null) {
      MULTI_CLUSTER_CLIENT_LOGGER.info(
          "[skip]dataCenter: {} data change:{} notify", dataCenter, dataInfoIds);
      return;
    }

    for (Entry<Integer, Set<String>> entry : slotDataInfoIds.entrySet()) {
      RemoteSlotStates states = storage.slotTableStates.getSlotStates(entry.getKey());
      if (states == null) {
        MULTI_CLUSTER_CLIENT_LOGGER.info(
            "[skip]dataCenter: {}, slotId:{},  data change:{} notify",
            dataCenter,
            entry.getKey(),
            dataInfoIds);
        continue;
      }
      states.addPending(entry.getValue());
      MULTI_CLUSTER_CLIENT_LOGGER.info(
          "dataCenter: {}, slotId:{},  data change:{} add to pending.",
          dataCenter,
          entry.getKey(),
          dataInfoIds);
    }
  }

  private boolean checkSlot(SlotTable cur, SlotTable updating, SlotTable update) {
    try {
      cur.assertSlotLessThan(update);
      if (updating != null) {
        update.assertSlotLessThan(update);
      }
      return true;
    } catch (RuntimeException e) {
      MULTI_CLUSTER_SLOT_TABLE.error(
          "[checkSlot]assert slot fail, cur: {}, updating: {}, update: {}",
          cur,
          updating,
          update,
          e);
      return false;
    }
  }

  private final class RemoteSyncingWatchDog extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      try {
        doUpdating();
        doSyncRemoteLeader();

      } catch (Throwable t) {
        MULTI_CLUSTER_CLIENT_LOGGER.error("[remoteSyncWatch]failed to do sync watching.", t);
      }
    }

    @Override
    public int getWaitingMillis() {
      return 200;
    }
  }

  /**
   * update remote state
   *
   * @return
   */
  boolean doUpdating() {
    for (Entry<String, RemoteSlotTableStorage> entry : slotTableStorageMap.entrySet()) {
      String dataCenter = entry.getKey();
      SlotTable update = entry.getValue().updating.getAndSet(null);
      if (update == null) {
        continue;
      }
      RemoteSlotTableStates remoteStates = entry.getValue().slotTableStates;
      SlotTable current = remoteStates.slotTable;
      if (update.getEpoch() <= current.getEpoch()) {
        MULTI_CLUSTER_SLOT_TABLE.warn(
            "skip remoteDataCenter={}, updating={}, current={}",
            dataCenter,
            update.getEpoch(),
            current.getEpoch());
        continue;
      }
      remoteStates.updateSlotState(update);
    }
    return true;
  }

  void doSyncRemoteLeader() {
    final int remoteSyncLeaderMs =
        multiClusterDataServerConfig.getSyncRemoteSlotLeaderIntervalSecs() * 1000;

    final int remoteSyncDataIdMs = multiClusterDataServerConfig.getSyncRemoteDataIdIntervalMs();
    for (Entry<String, RemoteSlotTableStorage> entry : slotTableStorageMap.entrySet()) {
      String remoteDataCenter = entry.getKey();
      if (!fetchMultiSyncService.multiSync(remoteDataCenter)) {
        continue;
      }

      RemoteSlotTableStates states = entry.getValue().slotTableStates;
      for (RemoteSlotStates state : states.slotStates.values()) {
        try {
          syncRemoteDataIds(
              remoteDataCenter, state, remoteSyncDataIdMs, states.slotTable.getEpoch());
          syncRemote(remoteDataCenter, state, remoteSyncLeaderMs, states.slotTable.getEpoch());
        } catch (Throwable t) {
          MULTI_CLUSTER_CLIENT_LOGGER.error(
              "[syncRemoteLeader]remoteDataCenter={}, slotId={} sync error.",
              entry.getKey(),
              state.slotId,
              t);
        }
      }
    }
  }

  private Set<String> getTobeSyncs(
      RemoteSlotStates state, int remoteSyncDataIdMs, KeyedTask<SyncDataIdTask> syncDataIdTask) {
    if (syncDataIdTask != null
        && syncDataIdTask.isOverAfter(remoteSyncDataIdMs)
        && !syncDataIdTask.isFinished()) {
      // task timeout, pending to retry
      state.addPending(syncDataIdTask.getRunnable().syncing);
    }
    return state.pendingDataInfoIds.getAndReset();
  }

  private Set<SyncSlotAcceptor> acceptors(String remoteDataCenter, Set<String> syncing) {
    HashSet<SyncSlotAcceptor> acceptors =
        Sets.newHashSet(MultiValueConstants.DATUM_SYNCER_SOURCE_FILTER);
    MultiSegmentSyncSwitch multiSyncSwitch =
        fetchMultiSyncService.getMultiSyncSwitch(remoteDataCenter);

    acceptors.add(new SyncSlotDataInfoIdAcceptor(syncing, multiSyncSwitch.getIgnoreDataInfoIds()));

    return acceptors;
  }

  private void syncRemoteDataIds(
      String remoteDataCenter,
      RemoteSlotStates state,
      int remoteSyncDataIdMs,
      long slotTableEpoch) {
    final Slot slot = state.slot;
    final KeyedTask<SyncDataIdTask> syncDataIdTask = state.syncDataIdTask;

    // 1.syncDataIdTask == null, state.pending is not empty, execute new task;
    // 2.syncDataIdTask != null
    //  2.1 syncDataIdTask.isFinished(), state.pending is not empty, execute new task(200ms to
    // execute a new task);
    //  2.2 syncDataIdTask.isNotFinished(), but task is timeout(over remoteSyncDataIdMs),
    //     repending syncDataIdTask.syncing to execute a new task
    if (syncDataIdTask == null
        || syncDataIdTask.isFinished()
        || syncDataIdTask.isOverAfter(remoteSyncDataIdMs)) {

      Set<String> tobeSyncs = getTobeSyncs(state, remoteSyncDataIdMs, syncDataIdTask);
      if (CollectionUtils.isEmpty(tobeSyncs)) {
        return;
      }
      Set<String> syncing = state.pendingDataInfoIds.getAndReset();
      SlotDiffSyncer syncer =
          new SlotDiffSyncer(
              dataServerConfig,
              datumStorageDelegate,
              dataChangeEventCenter,
              null,
              multiSyncDataAcceptorManager
              .new RemoteSyncDataAcceptorManager(acceptors(remoteDataCenter, syncing)),
              MULTI_CLUSTER_CLIENT_LOGGER);
      SyncContinues continues = () -> isLeader(slot.getLeader());
      SyncDataIdTask task =
          new SyncDataIdTask(
              dataServerConfig.getLocalDataCenter(),
              remoteDataCenter,
              slotTableEpoch,
              slot,
              syncer,
              syncing,
              remoteDataNodeExchanger,
              continues,
              MULTI_CLUSTER_SYNC_DIGEST_LOGGER,
              MULTI_CLUSTER_SYNC_DIGEST_LOGGER);
      state.syncDataIdTask =
          multiClusterExecutorManager.getRemoteSyncDataIdExecutor().execute(slot.getId(), task);
      return;
    }

    if (syncDataIdTask.isFinished()) {
      state.completeSyncRemoteDataIdTask();
    } else {
      if (System.currentTimeMillis() - syncDataIdTask.getCreateTime() > 1500) {
        // the sync leader is running more than 1500ms, print
        MULTI_CLUSTER_CLIENT_LOGGER.info(
            "remoteDataCenter={}, slotId={}, dataIds={}, sync-dataid running, {}",
            remoteDataCenter,
            slot.getId(),
            syncDataIdTask.getRunnable().syncing,
            syncDataIdTask);
      }
    }
  }

  void syncRemote(
      String remoteDataCenter,
      RemoteSlotStates state,
      int remoteSyncLeaderMs,
      long slotTableEpoch) {
    final Slot slot = state.slot;
    final KeyedTask<SyncLeaderTask> syncRemoteTask = state.syncRemoteTask;

    if (syncRemoteTask == null || syncRemoteTask.isOverAfter(remoteSyncLeaderMs)) {
      SlotDiffSyncer syncer =
          new SlotDiffSyncer(
              dataServerConfig,
              datumStorageDelegate,
              dataChangeEventCenter,
              null,
              multiSyncDataAcceptorManager.getSyncSlotAcceptorManager(remoteDataCenter),
              MULTI_CLUSTER_CLIENT_LOGGER);
      SyncContinues continues = () -> isLeader(slot.getLeader());
      SyncLeaderTask task =
          new SyncLeaderTask(
              dataServerConfig.getLocalDataCenter(),
              remoteDataCenter,
              slotTableEpoch,
              slot,
              syncer,
              remoteDataNodeExchanger,
              continues,
              MULTI_CLUSTER_SYNC_DIGEST_LOGGER,
              MULTI_CLUSTER_SYNC_DIGEST_LOGGER);
      state.syncRemoteTask =
          multiClusterExecutorManager.getRemoteSyncLeaderExecutor().execute(slot.getId(), task);
      return;
    }

    if (syncRemoteTask.isFinished()) {
      state.completeSyncRemoteLeaderTask();
    } else {
      if (System.currentTimeMillis() - syncRemoteTask.getCreateTime() > 5000) {
        // the sync leader is running more than 5secs, print
        MULTI_CLUSTER_CLIENT_LOGGER.info(
            "remoteDataCenter={}, slotId={}, sync-leader running, {}",
            remoteDataCenter,
            slot.getId(),
            syncRemoteTask);
      }
    }
  }

  private boolean isLeader(String leader) {
    return ServerEnv.isLocalServer(leader);
  }
}
