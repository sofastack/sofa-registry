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
package com.alipay.sofa.registry.server.meta.remoting.meta;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: MetaServerRenewService.java, v 0.1 2021年03月26日 20:29 xiaojian.xj Exp $
 */
public class MetaServerRenewService {

  protected final Logger LOGGER = LoggerFactory.getLogger("META-RENEW");

  @Autowired private MetaLeaderService metaLeaderService;

  @Autowired private NodeConfig nodeConfig;

  @Autowired protected LocalMetaExchanger localMetaExchanger;

  @Autowired protected MetaServerConfig metaServerConfig;

  private Renewer renewer;

  public synchronized void startRenewer(int intervalMs) {

    if (renewer != null) {
      throw new IllegalStateException("has started renewer");
    }
    this.renewer = new Renewer(intervalMs);
    ConcurrentUtils.createDaemonThread("metaNode-renewer", this.renewer).start();
  }

  private final class Renewer extends WakeUpLoopRunnable {
    final int intervalMs;

    Renewer(int intervalMs) {
      this.intervalMs = intervalMs;
    }

    @Override
    public void runUnthrowable() {
      try {

        // heartbeat on leader
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

  public void renewNode() {
    final String leaderIp = metaLeaderService.getLeader();
    HeartbeatRequest heartbeatRequest =
        new HeartbeatRequest<>(
            createNode(),
            -1L,
            nodeConfig.getLocalDataCenter(),
            System.currentTimeMillis(),
            null,
            null);

    boolean success = true;
    final long startTimestamp = System.currentTimeMillis();
    try {
      GenericResponse resp =
          (GenericResponse)
              localMetaExchanger
                  .sendRequest(metaServerConfig.getLocalDataCenter(), heartbeatRequest)
                  .getResult();

      if (resp == null || !resp.isSuccess()) {
        success = false;
        LOGGER.error(
            "[RenewNodeTask] renew meta node to metaLeader error, leader: {}, resp: {}",
            leaderIp,
            resp);
      }
    } catch (Throwable t) {
      success = false;
      LOGGER.error("[RenewNodeTask] renew node to metaLeader error, leader: {}", leaderIp, t);
    } finally {
      LOGGER.info(
          "[renewMetaLeader]{},leader={},span={}",
          success ? 'Y' : 'N',
          leaderIp,
          System.currentTimeMillis() - startTimestamp);
    }
  }

  private MetaNode createNode() {
    return new MetaNode(new URL(ServerEnv.IP), nodeConfig.getLocalDataCenter());
  }
}
