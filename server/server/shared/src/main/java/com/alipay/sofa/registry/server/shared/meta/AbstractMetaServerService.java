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
import com.alipay.sofa.registry.common.model.metaserver.DataOperation;
import com.alipay.sofa.registry.common.model.metaserver.FetchProvideDataRequest;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.blacklist.RegistryBlacklistRequest;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:21 yuzhi.lyz Exp $
 */
public abstract class AbstractMetaServerService<T extends BaseHeartBeatResponse>
    implements MetaServerService {
  protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

  @Autowired protected MetaServerManager metaServerManager;

  protected volatile State state = State.NULL;

  private Renewer renewer;

  private AtomicInteger renewFailCounter = new AtomicInteger(0);
  private static final Integer maxRenewFailCount = 3;

  @PreDestroy
  public void dispose() {
    if (renewer != null) {
      renewer.close();
    }
  }

  @Override
  public synchronized void startRenewer(int intervalMs) {
    if (renewer != null) {
      throw new IllegalStateException("has started renewer");
    }
    this.renewer = new Renewer(intervalMs);
    ConcurrentUtils.createDaemonThread("meta-renewer", this.renewer).start();
  }

  @Override
  public void stopRenewer() {
    if (renewer != null) {
      renewer.close();
    }
  }

  @Override
  public boolean handleSlotTableChange(SlotTableChangeEvent event) {
    long epoch = event.getSlotTableEpoch();
    long currentEpoch = getCurrentSlotTableEpoch();
    if (currentEpoch >= epoch) {
      LOGGER.warn(
          "[handleSlotTableChange] slot-table change event epoch: [{}], current epoch: [{}], "
              + "won't retrieve again",
          epoch,
          currentEpoch);
      return false;
    }
    LOGGER.info(
        "[handleSlotTableChange] slot table is changed, run heart-beat to retrieve new version");
    if (renewer != null) {
      renewer.wakeup();
    }
    return true;
  }

  @Override
  public void addSelfToMetaBlacklist() {
    metaServerManager.sendRequest(new RegistryBlacklistRequest(DataOperation.ADD, ServerEnv.IP));
  }

  @Override
  public void removeSelfFromMetaBlacklist() {
    metaServerManager.sendRequest(new RegistryBlacklistRequest(DataOperation.REMOVE, ServerEnv.IP));
  }

  private final class Renewer extends WakeUpLoopRunnable {
    final int intervalMs;

    Renewer(int intervalMs) {
      this.intervalMs = intervalMs;
    }

    @Override
    public void runUnthrowable() {
      try {
        if (renewFailCounter.get() >= maxRenewFailCount) {
          metaServerManager.resetLeaderFromRestServer();
          renewFailCounter.set(0);
        }

        renewNode();
      } catch (Throwable e) {
        LOGGER.error("failed to renewNode", e);
      }
    }

    @Override
    public int getWaitingMillis() {
      return intervalMs;
    }
  }

  @Override
  public void renewNode() {
    final String leaderIp = metaServerManager.getMetaServerLeader();
    try {
      HeartbeatRequest heartbeatRequest = createRequest();
      GenericResponse<T> resp =
          (GenericResponse<T>) metaServerManager.sendRequest(heartbeatRequest).getResult();
      if (resp != null && resp.isSuccess()) {
        updateState(resp.getData());
        metaServerManager.refresh(resp.getData());
        handleRenewResult(resp.getData());

        renewFailCounter.set(0);
      } else if (resp != null && !resp.isSuccess()) {
        // heartbeat on follow, refresh leader;
        // it will renewNode on leader next time;
        if (!resp.getData().isHeartbeatOnLeader()) {
          metaServerManager.refresh(resp.getData());

          renewFailCounter.set(0);
        }
      } else {
        renewFailCounter.incrementAndGet();

        LOGGER.error(
            "[RenewNodeTask] renew data node to metaServer error : {}, {}", leaderIp, resp);
        throw new RuntimeException(
            "[RenewNodeTask] renew data node to metaServer error : " + leaderIp);
      }
    } catch (Throwable e) {
      renewFailCounter.incrementAndGet();

      LOGGER.error("renew node error from {}", leaderIp, e);
      throw new RuntimeException("renew node error! " + e.getMessage(), e);
    }
  }

  private void updateState(T response) {
    State s =
        new State(
            response.getDataCentersFromMetaNodes(),
            response.getSessionNodesMap(),
            response.getSlotTable().getDataServers(),
            response.getSessionServerEpoch(),
            response.getMetaLeader(),
            response.getMetaLeaderEpoch());
    this.state = s;
    LOGGER.info(
        "update MetaStat, sessions={}/{}, datas={}, metaLeader: {}, metaLeaderEpoch: {}",
        s.sessionServerEpoch,
        s.sessionNodes.keySet(),
        s.dataServers,
        s.metaLeader,
        s.metaLeaderEpoch);
  }

  @Override
  public ProvideData fetchData(String dataInfoId) {
    final String leaderIp = metaServerManager.getMetaServerLeader();
    try {
      Response response = metaServerManager.sendRequest(new FetchProvideDataRequest(dataInfoId));

      Object result = response.getResult();
      if (result instanceof ProvideData) {
        return (ProvideData) result;
      } else {
        LOGGER.error("fetch null provider data from {}", leaderIp);
        throw new RuntimeException("metaServerService fetch null provider data!");
      }
    } catch (Exception e) {
      LOGGER.error("fetch provider data error from {}", leaderIp, e);
      throw new RuntimeException("fetch provider data error! " + e.getMessage(), e);
    }
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

  public String getMetaServerLeader() {
    return metaServerManager.getMetaServerLeader();
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

  protected abstract long getCurrentSlotTableEpoch();

  private static final class State {
    static final State NULL =
        new State(
            Collections.emptySet(), Collections.emptyMap(), Collections.emptySet(), 0, null, -1L);
    protected final long sessionServerEpoch;
    protected final Map<String, SessionNode> sessionNodes;
    protected final Set<String> dataServers;
    protected final String metaLeader;
    protected final long metaLeaderEpoch;
    protected final Set<String> dataCenters;

    State(
        Set<String> dataCenters,
        Map<String, SessionNode> sessionNodes,
        Set<String> dataServers,
        long sessionServerEpoch,
        String metaLeader,
        long metaLeaderEpoch) {
      this.sessionServerEpoch = sessionServerEpoch;
      this.dataCenters = Collections.unmodifiableSet(new TreeSet<>(dataCenters));
      this.sessionNodes = Collections.unmodifiableMap(sessionNodes);
      this.dataServers = Collections.unmodifiableSet(dataServers);
      this.metaLeader = metaLeader;
      this.metaLeaderEpoch = metaLeaderEpoch;
    }
  }
}
