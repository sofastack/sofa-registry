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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.remoting.ListenServerChannelHandler;
import com.alipay.sofa.registry.util.AtomicSet;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

/**
 * @author shangyu.wh
 * @version $Id: ServerConnectionLisener.java, v 0.1 2017-11-30 15:04 shangyu.wh Exp $
 */
public class ClientNodeConnectionHandler extends ListenServerChannelHandler
    implements ApplicationListener<ContextRefreshedEvent> {
  private final Logger LOG = LoggerFactory.getLogger("SRV-CONNECT");

  @Autowired Registry sessionRegistry;

  @Autowired ExecutorManager executorManager;

  private final AtomicSet<ConnectId> pendingClientOff = new AtomicSet<>();
  private final ClientOffWorker worker = new ClientOffWorker();

  private volatile boolean stopped = false;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    start();
  }

  public void start() {
    ConcurrentUtils.createDaemonThread("ClientOff-Worker", worker).start();
  }

  @Override
  public void disconnected(Channel channel) {
    if (stopped) {
      return;
    }
    super.disconnected(channel);
    fireCancelClient(channel);
  }

  public void stop() {
    this.stopped = true;
  }

  @Override
  protected Node.NodeType getConnectNodeType() {
    return Node.NodeType.CLIENT;
  }

  void fireCancelClient(Channel channel) {
    pendingClientOff.add(ConnectId.of(channel.getRemoteAddress(), channel.getLocalAddress()));
    worker.wakeup();
  }

  private class ClientOffWorker extends WakeUpLoopRunnable {
    @Override
    public void runUnthrowable() {
      Set<ConnectId> connectIds = pendingClientOff.getAndReset();
      if (!CollectionUtils.isEmpty(connectIds)) {
        long start = System.currentTimeMillis();
        sessionRegistry.clean(Lists.newArrayList(connectIds));
        long span = System.currentTimeMillis() - start;
        LOG.info("disconnect size={},span={}", connectIds.size(), span);
      }
    }

    @Override
    public int getWaitingMillis() {
      return 5000;
    }
  }
}
