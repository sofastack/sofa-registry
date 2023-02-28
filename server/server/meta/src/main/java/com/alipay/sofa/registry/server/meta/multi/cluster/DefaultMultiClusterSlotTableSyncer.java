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
package com.alipay.sofa.registry.server.meta.multi.cluster;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.MetaLeaderNotWarmupException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MultiClusterMetaServerConfig;
import com.alipay.sofa.registry.server.meta.multi.cluster.remote.RemoteClusterMetaExchanger;
import com.alipay.sofa.registry.server.meta.multi.cluster.remote.RemoteClusterSlotSyncRequest;
import com.alipay.sofa.registry.server.meta.multi.cluster.remote.RemoteClusterSlotSyncResponse;
import com.alipay.sofa.registry.task.KeyedTask;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotTableSyncer.java, v 0.1 2022年04月15日 16:46 xiaojian.xj Exp $
 */
public class DefaultMultiClusterSlotTableSyncer implements MultiClusterSlotTableSyncer {

  private static final Logger LOGGER =
      LoggerFactory.getLogger("MULTI-CLUSTER-CLIENT", "[SlotTableSyncer]");

  private Map<String, RemoteClusterSlotState> slotStateMap = Maps.newConcurrentMap();

  private SlotTableWatcher watcher = new SlotTableWatcher();

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private MultiClusterMetaServerConfig multiClusterMetaServerConfig;

  @Autowired private RemoteClusterMetaExchanger remoteClusterMetaExchanger;

  @Autowired private ExecutorManager executorManager;

  private static volatile long LAST_REFRESH_CONFIG_TS = 0;

  static final int MAX_SYNC_FAIL_COUNT = 3;

  @PostConstruct
  public void init() {
    remoteClusterMetaExchanger.refreshClusterInfos();
    ConcurrentUtils.createDaemonThread("multi_cluster_slot_table", watcher).start();
    metaLeaderService.registerListener(this);
  }

  @Override
  public void becomeLeader() {
    watcher.wakeup();
  }

  @Override
  public void loseLeader() {}

  @Override
  public Map<String, RemoteClusterSlotState> getMultiClusterSlotTable() {
    if (!metaLeaderService.amIStableAsLeader()) {
      throw new MetaLeaderNotWarmupException(
          metaLeaderService.getLeader(), metaLeaderService.getLeaderEpoch());
    }

    Map<String, RemoteClusterSlotState> result =
        Maps.newHashMapWithExpectedSize(slotStateMap.size());
    for (Entry<String, RemoteClusterSlotState> entry : slotStateMap.entrySet()) {
      RemoteClusterSlotState state = entry.getValue();
      DataCenterMetadata metadata = state.getDataCenterMetadata();
      if (metadata == null) {
        LOGGER.error("[getMultiClusterSlotTable]dataCenter: {} metadata is null.", entry.getKey());
        continue;
      }
      result.put(
          metadata.getDataCenter(), new RemoteClusterSlotState(state.getSlotTable(), metadata));
    }
    return result;
  }

  private final class SlotTableWatcher extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      try {
        // if need reload sync info from db
        if (needReloadConfig()) {
          executorManager
              .getMultiClusterConfigReloadExecutor()
              .execute(() -> remoteClusterMetaExchanger.refreshClusterInfos());
          LAST_REFRESH_CONFIG_TS = System.currentTimeMillis();
        }
      } catch (Throwable t) {
        LOGGER.error("refresh multi cluster config error.", t);
      }

      if (!metaLeaderService.amILeader()) {
        return;
      }

      // remoteDataCenters is save in MultiClusterSyncInfo.remoteDataCenter, just use to print log,
      // not clusterId
      Set<String> remoteDataCenters = remoteClusterMetaExchanger.getAllRemoteClusters();
      SetView<String> removes = Sets.difference(slotStateMap.keySet(), remoteDataCenters);
      for (String remove : removes) {
        RemoteClusterSlotState state = slotStateMap.remove(remove);
        LOGGER.info("remove dataCenter: {} sync info: {}.", remove, state);
      }

      for (String dataCenter : remoteDataCenters) {
        RemoteClusterSlotState slotState =
            slotStateMap.computeIfAbsent(dataCenter, k -> new RemoteClusterSlotState());

        // check if exceed max fail count
        if (checkSyncFailCount(slotState.getFailCount())) {
          executorManager
              .getRemoteSlotSyncerExecutor()
              .execute(
                  dataCenter,
                  () -> {
                    slotState.initFailCount();
                    remoteClusterMetaExchanger.resetLeader(dataCenter);
                  });
          continue;
        }

        // check if need to do sync
        if (needSync(slotState.task)) {
          SlotSyncTask syncTask = new SlotSyncTask(dataCenter, slotState.slotTable.getEpoch());
          slotState.task =
              executorManager.getRemoteSlotSyncerExecutor().execute(dataCenter, syncTask);
          LOGGER.info("commit sync task:{}", syncTask);
        }
      }
    }

    private boolean checkSyncFailCount(long failCount) {
      if (failCount >= MAX_SYNC_FAIL_COUNT) {
        LOGGER.error("sync failed [{}] times, prepare to reset leader from rest api.", failCount);
        return true;
      }
      return false;
    }

    @Override
    public int getWaitingMillis() {
      return 200;
    }
  }

  /** @return */
  private boolean needReloadConfig() {
    return System.currentTimeMillis() - LAST_REFRESH_CONFIG_TS
        > multiClusterMetaServerConfig.getMultiClusterConfigReloadMillis();
  }

  /**
   * need sync slot table from remote cluster
   *
   * @param task
   * @return
   */
  private boolean needSync(KeyedTask<SlotSyncTask> task) {
    return task == null
        || task.isOverAfter(multiClusterMetaServerConfig.getRemoteSlotSyncerMillis());
  }

  public static final class RemoteClusterSlotState {
    volatile SlotTable slotTable;

    volatile DataCenterMetadata dataCenterMetadata;

    volatile KeyedTask<SlotSyncTask> task;

    final AtomicLong failCount = new AtomicLong(0);

    public RemoteClusterSlotState() {
      this.slotTable = SlotTable.INIT;
    }

    public RemoteClusterSlotState(SlotTable slotTable, DataCenterMetadata dataCenterMetadata) {
      this.slotTable = slotTable;
      this.dataCenterMetadata = dataCenterMetadata;
    }

    public synchronized void updateSlotTable(SlotTable update) {

      SlotTable prev = slotTable;
      if (slotTable.getEpoch() < update.getEpoch()) {
        this.slotTable = update;
        LOGGER.info(
            "slotTable update from {} to {}, data: {}", prev.getEpoch(), update.getEpoch(), update);
      }
    }

    public synchronized void updateMetadata(DataCenterMetadata metadata) {
      if (metadata != null && !metadata.equals(this.dataCenterMetadata)) {
        LOGGER.info("dataCenterMetadata update from {} to {}", this.dataCenterMetadata, metadata);
        this.dataCenterMetadata = metadata;
      }
    }

    public long incrementAndGetFailCount() {
      return failCount.incrementAndGet();
    }

    public void initFailCount() {
      failCount.set(0);
    }

    public long getFailCount() {
      return failCount.get();
    }

    /**
     * Getter method for property <tt>dataCenterMetadata</tt>.
     *
     * @return property value of dataCenterMetadata
     */
    public synchronized DataCenterMetadata getDataCenterMetadata() {
      return dataCenterMetadata;
    }

    public synchronized SlotTable getSlotTable() {
      return this.slotTable;
    }

    @Override
    public String toString() {
      return "RemoteClusterSlotState{"
          + "slotTable="
          + slotTable
          + ", dataCenterMetadata="
          + dataCenterMetadata
          + ", task="
          + task
          + ", failCount="
          + failCount
          + '}';
    }
  }

  private final class SlotSyncTask implements Runnable {
    final long startTimestamp = System.currentTimeMillis();

    final String dataCenter;

    final long slotTableEpoch;

    public SlotSyncTask(String dataCenter, long slotTableEpoch) {
      this.dataCenter = dataCenter;
      this.slotTableEpoch = slotTableEpoch;
    }

    @Override
    public void run() {
      boolean success = false;
      RemoteClusterSlotSyncRequest request =
          new RemoteClusterSlotSyncRequest(dataCenter, slotTableEpoch);
      try {
        Response response = remoteClusterMetaExchanger.sendRequest(dataCenter, request);

        // learn latest meta leader and slot table
        handleSyncResponse(request, response.getResult());
        success = true;
      } catch (Throwable t) {
        handleSyncFail(request, t);
      } finally {
        LOGGER.info(
            "{},{},{},span={}",
            success ? 'Y' : 'N',
            dataCenter,
            slotTableEpoch,
            System.currentTimeMillis() - startTimestamp);
      }
    }

    @Override
    public String toString() {
      return "SlotSyncTask{"
          + "startTimestamp="
          + startTimestamp
          + ", dataCenter='"
          + dataCenter
          + '\''
          + ", slotTableEpoch="
          + slotTableEpoch
          + '}';
    }
  }

  private void handleSyncFail(RemoteClusterSlotSyncRequest request, Throwable t) {
    RemoteClusterSlotState state = slotStateMap.get(request.getDataCenter());
    state.incrementAndGetFailCount();
    LOGGER.error("[syncRemoteMeta]sync request: {} error.", request, t);
  }

  private void handleSyncResponse(RemoteClusterSlotSyncRequest request, Object response) {
    RemoteClusterSlotState state = slotStateMap.get(request.getDataCenter());
    if (!(response instanceof GenericResponse)) {
      throw new RuntimeException(
          StringFormatter.format("sync request: {} fail, resp: {}", request, response));
    }
    GenericResponse<RemoteClusterSlotSyncResponse> syncRest =
        (GenericResponse<RemoteClusterSlotSyncResponse>) response;
    RemoteClusterSlotSyncResponse data = syncRest.getData();

    if (syncRest.isSuccess()) {
      boolean learn =
          remoteClusterMetaExchanger.learn(
              request.getDataCenter(),
              new LeaderInfo(data.getMetaLeaderEpoch(), data.getMetaLeader()));
      if (learn) {
        handleSyncResult(state, data);
        state.initFailCount();
      }
      return;
    }
    handleFailResponse(request, syncRest);
  }

  private void handleFailResponse(
      RemoteClusterSlotSyncRequest request,
      GenericResponse<RemoteClusterSlotSyncResponse> syncRest) {

    RemoteClusterSlotSyncResponse data = syncRest.getData();

    if (data == null) {
      throw new RuntimeException(
          StringFormatter.format(
              "sync request: {} fail, resp.data is null, msg: {}", request, syncRest.getMessage()));
    }
    // heartbeat on follow, refresh leader;
    // it will sync on leader next time;
    if (!data.isSyncOnLeader()) {
      remoteClusterMetaExchanger.learn(
          request.getDataCenter(), new LeaderInfo(data.getMetaLeaderEpoch(), data.getMetaLeader()));
      // refresh the leader from follower, but the info maybe incorrect
      // throw the exception to trigger the counter inc
      // if the info is correct, the counter would be reset
      throw new RuntimeException(
          StringFormatter.format(
              "sync dataCenter: {} on metaServer.follower, leader is: {} ",
              request.getDataCenter(),
              new LeaderInfo(data.getMetaLeaderEpoch(), data.getMetaLeader())));
    } else if (!data.isLeaderWarmuped()) {
      // remote leader not warmuped, just print log, not throw exception.
      LOGGER.info("sync dataCenter: {}, remote leader:{} not warmuped.", request.getDataCenter());
    } else {
      throw new RuntimeException(
          StringFormatter.format(
              "sync dataCenter: {} on metaServer.leader error, msg={}, data={}",
              syncRest.getMessage(),
              data));
    }
  }

  private void handleSyncResult(RemoteClusterSlotState state, RemoteClusterSlotSyncResponse data) {

    if (data.isSlotTableUpgrade()) {
      LOGGER.info("slotTable data upgrade:{}", data);
      state.updateSlotTable(data.getSlotTable());
    }

    state.updateMetadata(data.getDataCenterMetadata());
  }

  @VisibleForTesting
  public Map<String, RemoteClusterSlotState> getRemoteClusterSlotState() {
    return slotStateMap;
  }
}
