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
package com.alipay.sofa.registry.server.meta.remoting.notifier;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Feb 23, 2021
 */
public abstract class AbstractNotifier<T extends Node> implements Notifier {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired private MetaLeaderService metaLeaderService;

  private Executor executors =
      DefaultExecutorFactory.createCachedThreadPoolFactory(
              getClass().getSimpleName(),
              Math.min(4, OsUtils.getCpuCount()),
              60 * 1000,
              TimeUnit.MILLISECONDS)
          .create();

  @Override
  public void notifySlotTableChange(SlotTable slotTable) {
    if (metaLeaderService.amIStableAsLeader()) {
      new NotifyTemplate<SlotTableChangeEvent>()
          .broadcast(new SlotTableChangeEvent(slotTable.getEpoch()));
    }
  }

  @Override
  public void notifyProvideDataChange(ProvideDataChangeEvent event) {
    new NotifyTemplate<ProvideDataChangeEvent>().broadcast(event);
  }

  @VisibleForTesting
  public AbstractNotifier<T> setExecutors(Executor executors) {
    this.executors = executors;
    return this;
  }

  protected abstract NodeExchanger getNodeExchanger();

  protected abstract List<T> getNodes();

  protected abstract NodeConnectManager getNodeConnectManager();

  public final class NotifyTemplate<E> {

    public void broadcast(E event) {
      NodeConnectManager nodeConnectManager = getNodeConnectManager();
      Collection<InetSocketAddress> connections = nodeConnectManager.getConnections(null);

      if (connections == null || connections.isEmpty()) {
        logger.error("Push Node list error! No node connected!");
        return;
      }

      List<T> nodes = getNodes();

      if (nodes == null || nodes.isEmpty()) {
        logger.error("Node list error! No node registered!");
        return;
      }
      Set<String> ipAddresses = Sets.newHashSet();
      nodes.forEach(node -> ipAddresses.add(node.getNodeUrl().getIpAddress()));
      new ConcurrentUtils.SafeParaLoop<InetSocketAddress>(executors, connections) {
        @Override
        protected void doRun0(InetSocketAddress connection) throws Exception {
          if (!ipAddresses.contains(connection.getAddress().getHostAddress())) {
            return;
          }
          getNodeExchanger().request(new SimpleRequest<E>(event, connection, executors));
        }
      }.run();
    }
  }

  private static final class SimpleRequest<E> implements Request<E> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRequest.class);

    private final E event;

    private final InetSocketAddress connection;

    private final Executor executors;

    public SimpleRequest(E event, InetSocketAddress connection, Executor executors) {
      this.event = event;
      this.connection = connection;
      this.executors = executors;
    }

    @Override
    public E getRequestBody() {
      return event;
    }

    @Override
    public URL getRequestUrl() {
      return new URL(connection);
    }

    @Override
    public CallbackHandler getCallBackHandler() {
      return new CallbackHandler() {
        @Override
        public void onCallback(Channel channel, Object message) {
          logger.info(
              "[onCallback] notify slot-change succeed, ({}): [{}]",
              channel != null ? channel.getRemoteAddress() : "unknown channel",
              message);
        }

        @Override
        public void onException(Channel channel, Throwable exception) {
          logger.error(
              "[onException] notify slot-change failed, ({})",
              channel != null ? channel.getRemoteAddress() : "unknown channel",
              exception);
        }

        @Override
        public Executor getExecutor() {
          return executors;
        }
      };
    }

    @Override
    public AtomicInteger getRetryTimes() {
      return new AtomicInteger(3);
    }
  }

  @VisibleForTesting
  AbstractNotifier<T> setMetaLeaderService(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
    return this;
  }
}
