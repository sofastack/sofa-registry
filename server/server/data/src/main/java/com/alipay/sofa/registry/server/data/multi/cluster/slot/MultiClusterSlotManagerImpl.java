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
import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
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
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotMetrics.SyncType;
import com.alipay.sofa.registry.server.data.multi.cluster.sync.info.FetchMultiSyncService;
import com.alipay.sofa.registry.server.data.slot.SlotChangeListenerManager;
import com.alipay.sofa.registry.server.data.slot.SlotDiffSyncer;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.data.slot.SlotManagerImpl.ISlotState;
import com.alipay.sofa.registry.server.data.slot.SyncContinues;
import com.alipay.sofa.registry.server.data.slot.SyncLeaderTask;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.task.KeyedTask;
import com.alipay.sofa.registry.util.AtomicSet;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

  private static final int MAX_DATAINFOID_SYNCING_FAIL_RETRY_SIZE = 1000;
  private static final Logger MULTI_CLUSTER_SYNC_DELTA_LOGGER =
      Loggers.MULTI_CLUSTER_SYNC_DELTA_LOGGER;

  private static final Logger MULTI_CLUSTER_SYNC_ALL_LOGGER = Loggers.MULTI_CLUSTER_SYNC_ALL_LOGGER;

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

  @Autowired private SlotManager slotManager;

  private final Map<String, RemoteSlotTableStorage> slotTableStorageMap = Maps.newConcurrentMap();

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

    return checkSlotAccess(slotId, tuple.o1.getEpoch(), tuple.o2, srcLeaderEpoch);
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

  private boolean localIsLeader(Slot slot) {
    return slotManager.isLeader(dataServerConfig.getLocalDataCenter(), slot.getId());
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

  @Override
  public Tuple<Long, List<BaseSlotStatus>> getSlotTableEpochAndStatuses(String dataCenter) {
    RemoteSlotTableStorage storage = slotTableStorageMap.get(dataCenter);
    if (storage == null) {
      return new Tuple<>(SlotTable.INIT.getEpoch(), Collections.emptyList());
    }

    long slotTableEpoch = storage.slotTableStates.getSlotTableEpoch();
    List<BaseSlotStatus> slotStatuses = storage.slotTableStates.getSlotStatuses();
    return new Tuple<>(slotTableEpoch, slotStatuses);
  }

  final class RemoteSlotTableStorage {
    private final RemoteSlotTableStates slotTableStates;

    private final AtomicReference<SlotTable> updating;

    public RemoteSlotTableStorage(
        RemoteSlotTableStates slotTableStates, AtomicReference<SlotTable> updating) {
      this.slotTableStates = slotTableStates;
      this.updating = updating;
    }

    /**
     * Getter method for property <tt>slotTableStates</tt>.
     *
     * @return property value of slotTableStates
     */
    @VisibleForTesting
    public RemoteSlotTableStates getSlotTableStates() {
      return slotTableStates;
    }
  }

  final class RemoteSlotTableStates {
    private final ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = updateLock.writeLock();
    private final ReentrantReadWriteLock.ReadLock readLock = updateLock.readLock();

    final String dataCenter;
    // save all slot
    volatile SlotTable slotTable = SlotTable.INIT;

    // map<slotId, RemoteSlotStates>, slot belong to me
    final Map<Integer, RemoteSlotStates> slotStates = Maps.newConcurrentMap();

    public RemoteSlotTableStates(String dataCenter) {
      this.dataCenter = dataCenter;
    }

    boolean updateSlotState(SlotTable update) {

      // filter slot belong to me
      SlotTable mySlots = update.filter(slotManager.leaderSlotIds());
      writeLock.lock();
      try {
        for (Slot slot : mySlots.getSlots()) {
          listenAdd(dataCenter, slot.getId());
          RemoteSlotStates state =
              slotStates.computeIfAbsent(
                  slot.getId(),
                  k -> {
                    MULTI_CLUSTER_SLOT_TABLE.info(
                        "[updateSlotState]add dataCenter={}, slot={}", dataCenter, slot);
                    return new RemoteSlotStates(dataCenter, slot);
                  });
          state.update(slot);
        }

        this.slotTable = update;
        MultiClusterSlotMetrics.observeRemoteLeaderAssignGauge(
            dataCenter, this.slotTable.getSlotNum());
      } catch (Throwable t) {
        MULTI_CLUSTER_SLOT_TABLE.error("[updateSlotState]update slot table:{} error.", update, t);
        return false;
      } finally {
        writeLock.unlock();
      }
      return true;
    }

    Set<Integer> slotIds() {
      readLock.lock();
      try {
        return slotStates.keySet();
      } finally {
        readLock.unlock();
      }
    }

    boolean addSlotState(Set<Integer> slotIds) {
      if (CollectionUtils.isEmpty(slotIds)) {
        return true;
      }
      writeLock.lock();
      try {
        for (Integer slotId : slotIds) {
          Slot slot = slotTable.getSlot(slotId);
          if (slot == null) {
            MULTI_CLUSTER_SLOT_TABLE.error(
                "[addSlotState]add dataCenter={}, slotId={} fail, "
                    + "for slotId not exist in slotTable={}",
                dataCenter,
                slotId,
                slotTable);
            continue;
          }
          listenAdd(dataCenter, slotId);
          slotStates.computeIfAbsent(
              slotId,
              k -> {
                MULTI_CLUSTER_SLOT_TABLE.info(
                    "[addSlotState]add dataCenter={}, slot={}", dataCenter, slot);
                return new RemoteSlotStates(dataCenter, slot);
              });
        }
      } catch (Throwable t) {
        MULTI_CLUSTER_SLOT_TABLE.error(
            "[addSlotState]add dataCenter={}, slotIds={} error.", dataCenter, slotIds, t);
        return false;
      } finally {
        writeLock.unlock();
      }
      return true;
    }

    boolean removeSlotState(Set<Integer> slotIds) {
      if (CollectionUtils.isEmpty(slotIds)) {
        return true;
      }
      writeLock.lock();
      try {
        for (Integer slotId : slotIds) {
          slotStates.remove(slotId);
          // first remove the slot for GetData Access check, then clean the data
          listenRemove(dataCenter, slotId);
          MultiClusterSlotMetrics.observeRemoteLeaderSyncingFinish(dataCenter, slotId);
          MULTI_CLUSTER_SLOT_TABLE.info(
              "[removeSlotState]dataCenter={} remove slot, slotId={}", dataCenter, slotId);
        }
      } catch (Throwable t) {
        MULTI_CLUSTER_SLOT_TABLE.error(
            "[removeSlotState]remove dataCenter={}, slotIds={} error.", dataCenter, slotIds, t);
        return false;
      } finally {
        writeLock.unlock();
      }
      return true;
    }

    long getSlotTableEpoch() {
      readLock.lock();
      try {
        return slotTable.getEpoch();
      } finally {
        readLock.unlock();
      }
    }

    List<BaseSlotStatus> getSlotStatuses() {
      List<BaseSlotStatus> slotStatuses = Lists.newArrayListWithCapacity(slotStates.size());
      updateLock.readLock().lock();
      try {
        for (Map.Entry<Integer, RemoteSlotStates> entry : slotStates.entrySet()) {
          int slotId = entry.getKey();
          RemoteSlotStates slotState = entry.getValue();
          LeaderSlotStatus status =
              new LeaderSlotStatus(
                  slotId,
                  slotState.slot.getLeaderEpoch(),
                  slotState.slot.getLeader(),
                  slotState.synced
                      ? BaseSlotStatus.LeaderStatus.HEALTHY
                      : BaseSlotStatus.LeaderStatus.UNHEALTHY);
          slotStatuses.add(status);
        }

        Collections.sort(slotStatuses, Comparator.comparingInt(s -> s.getSlotId()));
        return slotStatuses;
      } finally {
        updateLock.readLock().unlock();
      }
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

    private void listenAdd(String dataCenter, int slotId) {
      slotChangeListenerManager
          .remoteListeners()
          .forEach(listener -> listener.onSlotAdd(dataCenter, slotId, Role.Leader));
    }

    private void listenRemove(String dataCenter, int slotId) {
      slotChangeListenerManager
          .remoteListeners()
          .forEach(listener -> listener.onSlotRemove(dataCenter, slotId, Role.Leader));
    }
  }

  final class SyncDataIdTask extends SyncLeaderTask {

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

  static final class RemoteSlotStates implements ISlotState {
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
      if (syncDataIdTask == null || !syncDataIdTask.isFinished()) {
        return;
      }

      if (syncDataIdTask.isSuccess()) {
        this.lastSuccessDataInfoIdTime = syncDataIdTask.getEndTime();
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
   * @return map
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
   * @param remoteSlotTableStatus remoteSlotTableStatus
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
        // it should not happen, print error log and restart data server;
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
      SlotTable toBeUpdate = slotTableStatus.getSlotTable();

      if (!checkSlot(curSlotTable, updatingSlotTable, toBeUpdate)) {
        continue;
      }

      if (updating.compareAndSet(updatingSlotTable, toBeUpdate)) {
        wakeup = true;
        MULTI_CLUSTER_SLOT_TABLE.info(
            "[updateSlotTable]updating remote slot table, dataCenter={}, prev={}, new={}",
            dataCenter,
            curSlotTable,
            toBeUpdate);
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
      MULTI_CLUSTER_SYNC_DELTA_LOGGER.info(
          "[syncDisable]dataCenter: {} data change:{} notify", dataCenter, dataInfoIds);
      return;
    }

    Map<Integer, Set<String>> slotDataInfoIds = Maps.newHashMap();
    for (String dataInfoId : dataInfoIds) {
      SyncSlotAcceptorManager syncSlotAcceptorManager =
          multiSyncDataAcceptorManager.getSyncSlotAcceptorManager(dataCenter);
      if (syncSlotAcceptorManager == null
          || !syncSlotAcceptorManager.accept(SyncAcceptorRequest.buildRequest(dataInfoId))) {
        MULTI_CLUSTER_SYNC_DELTA_LOGGER.info(
            "[NotAccept]dataCenter: {} data change:{} notify", dataCenter, dataInfoId);
        continue;
      }

      int slotId = slotOf(dataInfoId);
      Set<String> set = slotDataInfoIds.computeIfAbsent(slotId, k -> Sets.newHashSet());
      set.add(dataInfoId);
    }

    RemoteSlotTableStorage storage = slotTableStorageMap.get(dataCenter);
    if (storage == null) {
      MULTI_CLUSTER_SYNC_DELTA_LOGGER.info(
          "[skip]dataCenter: {} data change:{} notify", dataCenter, dataInfoIds);
      return;
    }

    for (Entry<Integer, Set<String>> entry : slotDataInfoIds.entrySet()) {
      RemoteSlotStates states = storage.slotTableStates.getSlotStates(entry.getKey());
      if (states == null) {
        MULTI_CLUSTER_SYNC_DELTA_LOGGER.info(
            "[skip]dataCenter: {}, slotId:{},  data change:{} notify",
            dataCenter,
            entry.getKey(),
            dataInfoIds);
        continue;
      }
      states.addPending(entry.getValue());
      MULTI_CLUSTER_SYNC_DELTA_LOGGER.info(
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

  final class RemoteSyncingWatchDog extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      try {
        doUpdating();
        watchLocalSegSlot();
        doSyncRemoteLeader();

      } catch (Throwable t) {
        MULTI_CLUSTER_SYNC_ALL_LOGGER.error("[remoteSyncWatch]failed to do sync watching.", t);
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

  // compare with slot manager leader slotId
  private void watchLocalSegSlot() {
    Set<Integer> leaderIds = slotManager.leaderSlotIds();
    for (Entry<String, RemoteSlotTableStorage> entry : slotTableStorageMap.entrySet()) {
      RemoteSlotTableStates states = entry.getValue().slotTableStates;
      Set<Integer> currents = states.slotIds();
      Set<Integer> adds = Sets.newHashSet(Sets.difference(leaderIds, currents));
      Set<Integer> removes = Sets.newHashSet(Sets.difference(currents, leaderIds));

      if (!CollectionUtils.isEmpty(adds) || !CollectionUtils.isEmpty(removes)) {
        states.addSlotState(adds);
        states.removeSlotState(removes);
        MULTI_CLUSTER_SLOT_TABLE.info(
            "[doSlotStatesUpdate]dataCenter={}, adds={}, removes={}",
            entry.getKey(),
            adds,
            removes);
      }
    }
  }

  void doSyncRemoteLeader() {
    final int remoteSyncLeaderMs =
        multiClusterDataServerConfig.getSyncRemoteSlotLeaderIntervalSecs() * 1000;

    for (Entry<String, RemoteSlotTableStorage> entry : slotTableStorageMap.entrySet()) {
      String remoteDataCenter = entry.getKey();
      if (!fetchMultiSyncService.multiSync(remoteDataCenter)) {
        continue;
      }

      RemoteSlotTableStates states = entry.getValue().slotTableStates;
      for (RemoteSlotStates state : states.slotStates.values()) {
        try {
          syncRemoteDataIds(remoteDataCenter, state, states.slotTable.getEpoch());
          syncRemote(remoteDataCenter, state, remoteSyncLeaderMs, states.slotTable.getEpoch());
        } catch (Throwable t) {
          MULTI_CLUSTER_SYNC_ALL_LOGGER.error(
              "[syncRemoteLeader]remoteDataCenter={}, slotId={} sync error.",
              entry.getKey(),
              state.slotId,
              t);
        }
      }
    }
  }

  private Set<String> getTobeSyncs(
      RemoteSlotStates state, KeyedTask<SyncDataIdTask> syncDataIdTask) {
    if (syncDataIdTask != null && syncDataIdTask.isFailed()) {

      Set<String> syncedFail = syncDataIdTask.getRunnable().syncing;
      if (syncedFail.size() < MAX_DATAINFOID_SYNCING_FAIL_RETRY_SIZE) {
        MULTI_CLUSTER_SYNC_DELTA_LOGGER.error(
            "getTobeSyncs repending syncDataIdTask:{} to retry", syncedFail);
        state.addPending(syncedFail);
      } else {
        MULTI_CLUSTER_SYNC_DELTA_LOGGER.error(
            "getTobeSyncs syncDataIdTask.size={} > {}",
            syncedFail.size(),
            MAX_DATAINFOID_SYNCING_FAIL_RETRY_SIZE);
      }
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
      String remoteDataCenter, RemoteSlotStates state, long slotTableEpoch) {
    final Slot slot = state.slot;
    final KeyedTask<SyncDataIdTask> syncDataIdTask = state.syncDataIdTask;

    if (syncDataIdTask != null && syncDataIdTask.isFinished()) {
      state.completeSyncRemoteDataIdTask();
    }
    // 1.syncDataIdTask == null, state.pending is not empty, execute new task;
    // 2.syncDataIdTask.isFinished(), state.pending is not empty, execute new task(200ms to
    //   execute a new task);
    // 3.syncDataIdTask.isNotFinished(), waiting task to finish
    if (syncDataIdTask == null || syncDataIdTask.isFinished()) {

      Set<String> tobeSyncs = getTobeSyncs(state, syncDataIdTask);
      if (CollectionUtils.isEmpty(tobeSyncs)) {
        return;
      }
      MULTI_CLUSTER_SYNC_DELTA_LOGGER.info("tobeSyncs dataInfoIds:{}", tobeSyncs);
      SlotDiffSyncer syncer =
          new SlotDiffSyncer(
              dataServerConfig,
              datumStorageDelegate,
              dataChangeEventCenter,
              null,
              multiSyncDataAcceptorManager
              .new RemoteSyncDataAcceptorManager(acceptors(remoteDataCenter, tobeSyncs)),
              MULTI_CLUSTER_SYNC_DELTA_LOGGER);
      SyncContinues continues = () -> localIsLeader(slot);
      SyncDataIdTask task =
          new SyncDataIdTask(
              dataServerConfig.getLocalDataCenter(),
              remoteDataCenter,
              slotTableEpoch,
              slot,
              syncer,
              tobeSyncs,
              remoteDataNodeExchanger,
              continues,
              MULTI_CLUSTER_SYNC_DIGEST_LOGGER,
              MULTI_CLUSTER_SYNC_DIGEST_LOGGER);
      state.syncDataIdTask =
          multiClusterExecutorManager.getRemoteSyncDataIdExecutor().execute(slot.getId(), task);
      MultiClusterSlotMetrics.syncAccess(remoteDataCenter, SyncType.SYNC_DELTA);
      return;
    }

    if (System.currentTimeMillis() - syncDataIdTask.getCreateTime() > 1500) {
      // the sync leader is running more than 1500ms, print
      MULTI_CLUSTER_SYNC_DELTA_LOGGER.info(
          "remoteDataCenter={}, slotId={}, dataIds={}, sync-dataid running, {}",
          remoteDataCenter,
          slot.getId(),
          syncDataIdTask.getRunnable().syncing,
          syncDataIdTask);
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
              MULTI_CLUSTER_SYNC_ALL_LOGGER);
      SyncContinues continues = () -> localIsLeader(slot);
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
      MultiClusterSlotMetrics.syncAccess(remoteDataCenter, SyncType.SYNC_ALL);
      return;
    }

    if (syncRemoteTask.isFinished()) {
      state.completeSyncRemoteLeaderTask();
    } else {
      if (System.currentTimeMillis() - syncRemoteTask.getCreateTime() > 5000) {
        // the sync leader is running more than 5secs, print
        MULTI_CLUSTER_SYNC_ALL_LOGGER.info(
            "remoteDataCenter={}, slotId={}, sync-leader running, {}",
            remoteDataCenter,
            slot.getId(),
            syncRemoteTask);
      }
    }
  }

  /**
   * Setter method for property <tt>dataServerConfig</tt>.
   *
   * @param dataServerConfig value to be assigned to property dataServerConfig
   */
  @VisibleForTesting
  void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }

  /**
   * Setter method for property <tt>multiClusterDataServerConfig</tt>.
   *
   * @param multiClusterDataServerConfig value to be assigned to property
   *     multiClusterDataServerConfig
   */
  @VisibleForTesting
  void setMultiClusterDataServerConfig(MultiClusterDataServerConfig multiClusterDataServerConfig) {
    this.multiClusterDataServerConfig = multiClusterDataServerConfig;
  }

  /**
   * Setter method for property <tt>dataChangeEventCenter</tt>.
   *
   * @param dataChangeEventCenter value to be assigned to property dataChangeEventCenter
   */
  @VisibleForTesting
  void setDataChangeEventCenter(DataChangeEventCenter dataChangeEventCenter) {
    this.dataChangeEventCenter = dataChangeEventCenter;
  }

  /**
   * Setter method for property <tt>datumStorageDelegate</tt>.
   *
   * @param datumStorageDelegate value to be assigned to property datumStorageDelegate
   */
  @VisibleForTesting
  void setDatumStorageDelegate(DatumStorageDelegate datumStorageDelegate) {
    this.datumStorageDelegate = datumStorageDelegate;
  }

  /**
   * Setter method for property <tt>slotChangeListenerManager</tt>.
   *
   * @param slotChangeListenerManager value to be assigned to property slotChangeListenerManager
   */
  @VisibleForTesting
  void setSlotChangeListenerManager(SlotChangeListenerManager slotChangeListenerManager) {
    this.slotChangeListenerManager = slotChangeListenerManager;
  }

  /**
   * Setter method for property <tt>remoteDataNodeExchanger</tt>.
   *
   * @param remoteDataNodeExchanger value to be assigned to property remoteDataNodeExchanger
   */
  @VisibleForTesting
  void setRemoteDataNodeExchanger(RemoteDataNodeExchanger remoteDataNodeExchanger) {
    this.remoteDataNodeExchanger = remoteDataNodeExchanger;
  }

  /**
   * Setter method for property <tt>multiClusterExecutorManager</tt>.
   *
   * @param multiClusterExecutorManager value to be assigned to property multiClusterExecutorManager
   */
  @VisibleForTesting
  void setMultiClusterExecutorManager(MultiClusterExecutorManager multiClusterExecutorManager) {
    this.multiClusterExecutorManager = multiClusterExecutorManager;
  }

  /**
   * Setter method for property <tt>multiSyncDataAcceptorManager</tt>.
   *
   * @param multiSyncDataAcceptorManager value to be assigned to property
   *     multiSyncDataAcceptorManager
   */
  @VisibleForTesting
  void setMultiSyncDataAcceptorManager(MultiSyncDataAcceptorManager multiSyncDataAcceptorManager) {
    this.multiSyncDataAcceptorManager = multiSyncDataAcceptorManager;
  }

  /**
   * Setter method for property <tt>fetchMultiSyncService</tt>.
   *
   * @param fetchMultiSyncService value to be assigned to property fetchMultiSyncService
   */
  @VisibleForTesting
  void setFetchMultiSyncService(FetchMultiSyncService fetchMultiSyncService) {
    this.fetchMultiSyncService = fetchMultiSyncService;
  }

  /**
   * Setter method for property <tt>multiClusterSyncRepository</tt>.
   *
   * @param multiClusterSyncRepository value to be assigned to property multiClusterSyncRepository
   */
  @VisibleForTesting
  void setMultiClusterSyncRepository(MultiClusterSyncRepository multiClusterSyncRepository) {
    this.multiClusterSyncRepository = multiClusterSyncRepository;
  }

  /**
   * Setter method for property <tt>slotManager</tt>.
   *
   * @param slotManager value to be assigned to property slotManager
   */
  @VisibleForTesting
  void setSlotManager(SlotManager slotManager) {
    this.slotManager = slotManager;
  }

  /**
   * Getter method for property <tt>slotTableStorageMap</tt>.
   *
   * @param dataCenter dataCenter
   * @return property value of slotTableStorageMap
   */
  @VisibleForTesting
  public RemoteSlotTableStorage getSlotTableStorage(String dataCenter) {
    return slotTableStorageMap.get(dataCenter);
  }

  /**
   * Getter method for property <tt>watchDog</tt>.
   *
   * @return property value of watchDog
   */
  @VisibleForTesting
  public RemoteSyncingWatchDog getWatchDog() {
    return watchDog;
  }
}
