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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.metaserver.DataOperation;
import com.alipay.sofa.registry.common.model.metaserver.FetchProvideDataRequest;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyRequest;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.blacklist.RegistryForbiddenServerRequest;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.GetSlotTableStatusRequest;
import com.alipay.sofa.registry.common.model.slot.SlotTableStatusResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:21 yuzhi.lyz Exp $
 */
public abstract class AbstractMetaServerService<T extends BaseHeartBeatResponse>
    implements MetaServerService {
  protected final Logger RENEWER_LOGGER = LoggerFactory.getLogger("META-RENEW");

  @Autowired private MetaLeaderExchanger metaLeaderExchanger;

  @Autowired protected CommonConfig commonConfig;

  protected volatile State state = State.NULL;

  final Renewer renewer = new Renewer();
  private Thread renewerThread;
  final AtomicInteger renewFailCounter = new AtomicInteger(0);
  static final int MAX_RENEW_FAIL_COUNT = 3;

  @Override
  public synchronized void startRenewer() {
    if (renewerThread == null) {
      renewerThread = ConcurrentUtils.createDaemonThread("meta-renewer", this.renewer);
      renewerThread.start();
    }
  }

  @Override
  public void suspendRenewer() {
    renewer.suspend();
    RENEWER_LOGGER.info("suspend the renewer");
  }

  @Override
  public void resumeRenewer() {
    renewer.resume();
    RENEWER_LOGGER.info("resume the renewer");
  }

  public abstract int getRenewIntervalSecs();

  @Override
  public boolean handleSlotTableChange(SlotTableChangeEvent event) {
    long epoch = event.getSlotTableEpoch();
    long currentEpoch = getCurrentSlotTableEpoch();
    if (currentEpoch >= epoch) {
      RENEWER_LOGGER.warn(
          "[handleSlotTableChange] slot-table change event epoch: [{}], current epoch: [{}], "
              + "won't retrieve again",
          epoch,
          currentEpoch);
      return false;
    }
    RENEWER_LOGGER.info(
        "[handleSlotTableChange] slot table is changed, run heart-beat to retrieve new version");
    renewer.wakeup();
    return true;
  }

  @Override
  public void addSelfToMetaBlacklist() {
    metaLeaderExchanger.sendRequest(
        commonConfig.getLocalDataCenter(),
        new RegistryForbiddenServerRequest(DataOperation.ADD, nodeType(), ServerEnv.IP, cell()));
  }

  @Override
  public void removeSelfFromMetaBlacklist() {
    metaLeaderExchanger.sendRequest(
        commonConfig.getLocalDataCenter(),
        new RegistryForbiddenServerRequest(DataOperation.REMOVE, nodeType(), ServerEnv.IP, cell()));
  }

  private final class Renewer extends WakeUpLoopRunnable {
    @Override
    public void runUnthrowable() {
      try {
        checkRenewFailCounter();
        renewNode();
      } catch (Throwable e) {
        RENEWER_LOGGER.error("failed to renewNode", e);
      }
    }

    @Override
    public int getWaitingMillis() {
      return getRenewIntervalSecs() * 1000;
    }
  }

  @Override
  public boolean renewNode() {
    final String leaderIp = getMetaServerLeader();
    final long startTimestamp = System.currentTimeMillis();
    boolean success = true;
    try {
      HeartbeatRequest heartbeatRequest = createRequest();
      GenericResponse<T> resp =
          (GenericResponse<T>)
              metaLeaderExchanger
                  .sendRequest(commonConfig.getLocalDataCenter(), heartbeatRequest)
                  .getResult();
      handleHeartbeatResponse(resp);

      success = true;
    } catch (Throwable e) {
      success = false;
      handleHeartbeatFailed(leaderIp, e);
    } finally {
      RENEWER_LOGGER.info(
          "[renewMetaLeader]{},leader={},span={}",
          success ? 'Y' : 'N',
          leaderIp,
          System.currentTimeMillis() - startTimestamp);
    }
    return success;
  }

  boolean checkRenewFailCounter() {
    if (renewFailCounter.get() >= MAX_RENEW_FAIL_COUNT) {
      RENEWER_LOGGER.error(
          "renewNode failed [{}] times, prepare to reset leader from rest api.",
          renewFailCounter.get());
      metaLeaderExchanger.resetLeader(commonConfig.getLocalDataCenter());
      renewFailCounter.set(0);
      return true;
    }
    return false;
  }

  void handleHeartbeatResponse(GenericResponse<T> resp) {
    if (resp == null) {
      throw new RuntimeException("renew node to metaServer error : resp is null");
    }
    String localDataCenter = commonConfig.getLocalDataCenter();
    if (resp.isSuccess()) {
      updateState(resp.getData());
      BaseHeartBeatResponse data = resp.getData();
      metaLeaderExchanger.learn(
          localDataCenter, new LeaderInfo(data.getMetaLeaderEpoch(), data.getMetaLeader()));
      handleRenewResult(resp.getData());
      renewFailCounter.set(0);
    } else {
      T data = resp.getData();
      if (data == null) {
        // could no get data, trigger the counter inc
        throw new RuntimeException(
            "renew node to metaServer error, resp.data is null, msg:" + resp.getMessage());
      }
      // heartbeat on follow, refresh leader;
      // it will renewNode on leader next time;
      if (!data.isHeartbeatOnLeader()) {
        metaLeaderExchanger.learn(
            localDataCenter, new LeaderInfo(data.getMetaLeaderEpoch(), data.getMetaLeader()));
        // refresh the leader from follower, but the info maybe is incorrect
        // throw the exception to trigger the counter inc
        // if the info is correct, the counter would be reset after heartbeat
        throw new RuntimeException(
            "renew node to metaServer.follower, leader is "
                + new LeaderInfo(data.getMetaLeaderEpoch(), data.getMetaLeader()));
      } else {
        throw new RuntimeException(
            StringFormatter.format(
                "renew node to metaServer.leader error, msg={}, data={}", resp.getMessage(), data));
      }
    }
  }

  void handleHeartbeatFailed(String leaderIp, Throwable e) {
    renewFailCounter.incrementAndGet();
    RENEWER_LOGGER.error(
        "[RenewNodeTask] renew node to metaServer error, fail count:{}, leader: {}",
        renewFailCounter.get(),
        leaderIp,
        e);
    throw new RuntimeException("renew node error!", e);
  }

  private void updateState(T response) {
    Map<String, Set<String>> map = response.getRemoteDataServers();
    if (!CollectionUtils.isEmpty(state.remoteDataServers)) {
      for (Entry<String, Set<String>> entry : state.remoteDataServers.entrySet()) {
        map.putIfAbsent(entry.getKey(), entry.getValue());
      }
    }
    State s =
        new State(
            response.getDataCentersFromMetaNodes(),
            response.getSessionNodesMap(),
            response.getSlotTable().getDataServers(),
            response.getSessionServerEpoch(),
            response.getMetaLeader(),
            response.getMetaLeaderEpoch(),
            map);
    this.state = s;
    RENEWER_LOGGER.info(
        "update MetaStat, sessions={}/{}, datas={}, metaLeader: {}, metaLeaderEpoch: {}",
        s.sessionServerEpoch,
        s.sessionNodes.keySet(),
        s.dataServers,
        s.metaLeader,
        s.metaLeaderEpoch);
  }

  @Override
  public ProvideData fetchData(String dataInfoId) {
    final String leaderIp = getMetaServerLeader();
    try {
      Response response =
          metaLeaderExchanger.sendRequest(
              commonConfig.getLocalDataCenter(), new FetchProvideDataRequest(dataInfoId));

      Object result = response.getResult();
      if (result instanceof ProvideData) {
        return (ProvideData) result;
      } else {
        RENEWER_LOGGER.error("fetch null provider data from {}", leaderIp);
        throw new RuntimeException("metaServerService fetch null provider data!");
      }
    } catch (Throwable e) {
      RENEWER_LOGGER.error("fetch provider data error from {}", leaderIp, e);
      throw new RuntimeException("fetch provider data error! " + e.getMessage(), e);
    }
  }

  @Override
  public Map<String, ProvideData> fetchData(Map<String, Long> dataInfoIdsWithVersion) {
    // TODO unsupported this now
    throw new UnsupportedOperationException();
  }

  @Override
  public FetchSystemPropertyResult fetchSystemProperty(String dataInfoId, long version) {
    final String leaderIp = getMetaServerLeader();

    try {
      Response response =
          metaLeaderExchanger.sendRequest(
              commonConfig.getLocalDataCenter(),
              new FetchSystemPropertyRequest(dataInfoId, version));

      FetchSystemPropertyResult result = (FetchSystemPropertyResult) response.getResult();
      return result;
    } catch (Throwable e) {
      RENEWER_LOGGER.error(
          "fetch system property data:{}, version:{}, from {} is null",
          dataInfoId,
          version,
          leaderIp,
          e);
      throw new RuntimeException("fetch system property data error! " + e.getMessage(), e);
    }
  }

  @Override
  public SlotTableStatusResponse getSlotTableStatus() {
    final String leaderIp = getMetaServerLeader();

    try {
      Response response =
          metaLeaderExchanger.sendRequest(
              commonConfig.getLocalDataCenter(), new GetSlotTableStatusRequest());
      SlotTableStatusResponse result = (SlotTableStatusResponse) response.getResult();
      return result;
    } catch (Throwable e) {
      RENEWER_LOGGER.error("fetch slot table status from={} error.", leaderIp, e);
      return null;
    }
  }

  @Override
  public Set<ProcessId> getSessionProcessIds() {
    Set<ProcessId> processIds = Sets.newHashSetWithExpectedSize(state.sessionNodes.size());
    for (SessionNode session : state.sessionNodes.values()) {
      processIds.add(session.getProcessId());
    }
    return processIds;
  }

  public Map<String, SessionNode> getSessionNodes() {
    return state.sessionNodes;
  }

  public Set<String> getSessionServerList() {
    return state.sessionNodes.keySet();
  }

  public Set<String> getDataServerList() {
    return state.dataServers;
  }

  public Map<String, Set<String>> getRemoteDataServers() {
    return state.remoteDataServers;
  }

  public String getMetaServerLeader() {
    String localDataCenter = commonConfig.getLocalDataCenter();
    LeaderInfo leader = metaLeaderExchanger.getLeader(localDataCenter);
    if (leader == null) {
      throw new RuntimeException("localDataCenter meta leader is null.");
    }
    return leader.getLeader();
  }

  public List<String> getSessionServerList(String zonename) {
    List<String> serverList = new ArrayList<>();
    for (SessionNode sessionNode : getSessionNodes().values()) {
      if (StringUtils.isBlank(zonename) || zonename.equals(sessionNode.getRegionId())) {
        URL url = sessionNode.getNodeUrl();
        if (url != null) {
          serverList.add(url.getIpAddress());
        }
      }
    }
    return serverList;
  }

  @Override
  public long getSessionServerEpoch() {
    return state.sessionServerEpoch;
  }

  @Override
  public Set<String> getDataCenters() {
    return state.dataCenters;
  }

  protected abstract void handleRenewResult(T result);

  protected abstract HeartbeatRequest createRequest();

  protected abstract NodeType nodeType();

  protected abstract String cell();

  protected abstract long getCurrentSlotTableEpoch();

  private static final class State {
    static final State NULL =
        new State(
            Collections.emptySet(),
            Collections.emptyMap(),
            Collections.emptySet(),
            0,
            null,
            -1L,
            Collections.emptyMap());
    protected final long sessionServerEpoch;
    protected final Map<String, SessionNode> sessionNodes;
    protected final Set<String> dataServers;
    protected final String metaLeader;
    protected final long metaLeaderEpoch;
    protected final Set<String> dataCenters;
    protected final Map<String, Set<String>> remoteDataServers;

    State(
        Set<String> dataCenters,
        Map<String, SessionNode> sessionNodes,
        Set<String> dataServers,
        long sessionServerEpoch,
        String metaLeader,
        long metaLeaderEpoch,
        Map<String, Set<String>> remoteDataServers) {
      this.sessionServerEpoch = sessionServerEpoch;
      this.dataCenters = Collections.unmodifiableSet(new TreeSet<>(dataCenters));
      this.sessionNodes = Collections.unmodifiableMap(sessionNodes);
      this.dataServers = Collections.unmodifiableSet(dataServers);
      this.metaLeader = metaLeader;
      this.metaLeaderEpoch = metaLeaderEpoch;
      this.remoteDataServers = remoteDataServers;
    }
  }

  /**
   * Setter method for property <tt>metaLeaderExchanger</tt>.
   *
   * @param metaLeaderExchanger value to be assigned to property metaLeaderExchanger
   */
  public AbstractMetaServerService setMetaLeaderExchanger(MetaLeaderExchanger metaLeaderExchanger) {
    this.metaLeaderExchanger = metaLeaderExchanger;
    return this;
  }

  /**
   * Setter method for property <tt>commonConfig</tt>.
   *
   * @param commonConfig value to be assigned to property commonConfig
   */
  public AbstractMetaServerService setCommonConfig(CommonConfig commonConfig) {
    this.commonConfig = commonConfig;
    return this;
  }
}
