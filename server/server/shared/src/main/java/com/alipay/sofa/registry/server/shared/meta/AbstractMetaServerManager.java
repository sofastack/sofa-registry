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

import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.lifecycle.Initializable;
import com.alipay.sofa.registry.lifecycle.Startable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author chen.zhu
 *     <p>Mar 15, 2021
 */
public abstract class AbstractMetaServerManager extends ClientSideExchanger
    implements MetaServerManager, Initializable, Startable {

  private static final long INIT_META_LEADER_EPOCH = -1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetaServerManager.class);

  @Resource(name = "metaClientHandlers")
  private Collection<ChannelHandler> metaClientHandlers;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final AtomicInteger metaServerIndex = new AtomicInteger(0);

  protected volatile List<String> defaultMetaServers;

  // protected volatile List<String> runtimeMetaServers;

  protected volatile String metaLeader;

  protected volatile long metaLeaderEpoch = INIT_META_LEADER_EPOCH;

  protected AbstractMetaServerManager() {
    super(Exchange.META_SERVER_TYPE);
  }

  @PostConstruct
  public void postConstruct() {
    initialize();
    start();
  }

  @Override
  public void initialize() {
    defaultMetaServers = getConfiguredMetaIp();
    // this.runtimeMetaServers = Lists.newArrayList(defaultMetaServers);
  }

  @Override
  public void start() {
    // this.serverIps = Sets.newHashSet(runtimeMetaServers);
    connectServer();
  }

  @Override
  public List<String> getDefaultMetaServerList() {
    try {
      lock.readLock().lock();
      return Lists.newArrayList(defaultMetaServers);
    } finally {
      lock.readLock().unlock();
    }
  }

  // @Override
  // public List<String> getRuntimeMetaServerList() {
  //  try {
  //    lock.readLock().lock();
  //    return Lists.newArrayList(runtimeMetaServers);
  //  } finally {
  //    lock.readLock().unlock();
  //  }
  // }

  @Override
  public String getMetaServerLeader() {
    try {
      lock.readLock().lock();
      return getLeader();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void refresh(BaseHeartBeatResponse heartBeatResp) {
    String futureLeader = heartBeatResp.getMetaLeader();
    long futureEpoch = heartBeatResp.getMetaLeaderEpoch();
    if (futureEpoch >= metaLeaderEpoch && !StringUtils.isEmpty(futureLeader)) {
      lock.writeLock().lock();
      try {
        metaLeaderEpoch = futureEpoch;
        metaLeader = futureLeader;
      } finally {
        lock.writeLock().unlock();
      }
    }

    // heartbeat on follow, does not return metaNodes;
    if (CollectionUtils.isEmpty(heartBeatResp.getMetaNodes())) {
      return;
    }
    // List<String> metaServers = NodeUtils.transferNodeToIpList(heartBeatResp.getMetaNodes());
    // lock.writeLock().lock();
    // try {
    //  this.runtimeMetaServers = metaServers;
    // } finally {
    //  lock.writeLock().unlock();
    // }
    // doRefreshConnections();
  }

  // @VisibleForTesting
  // protected void doRefreshConnections() {
  //  Set<String> future = Sets.newHashSet(this.runtimeMetaServers);
  //  NodeComparator diff = new NodeComparator(this.serverIps, future);
  //  diff.accept(
  //      new ComparatorVisitor<String>() {
  //        @Override
  //        public void visitAdded(String added) {
  //          try {
  //            LOGGER.info("[visitAdded] connect new meta-server: {}", added);
  //            URL url = new URL(added, getServerPort());
  //            connect(url);
  //          } catch (Throwable th) {
  //            LOGGER.error("[visitAdded]", th);
  //          }
  //        }
  //
  //        @Override
  //        public void visitModified(Tuple<String, String> modified) {
  //          // do nothing
  //        }
  //
  //        @Override
  //        public void visitRemoved(String removed) {
  //          try {
  //            LOGGER.info("[visitRemoved] close meta-server connection: {}", removed);
  //            boltExchange
  //                .getClient(serverType)
  //                .getChannel(new URL(removed, getServerPort()))
  //                .close();
  //          } catch (Throwable th) {
  //            LOGGER.error("[visitRemoved]", th);
  //          }
  //        }
  //
  //        @Override
  //        public void visitRemaining(String remain) {
  //          // do nothing
  //        }
  //      });
  // }

  @Override
  public Response sendRequest(Object requestBody) throws RequestException {
    Request request =
        new Request() {
          @Override
          public Object getRequestBody() {
            return requestBody;
          }

          @Override
          public URL getRequestUrl() {
            return new URL(getLeader(), getServerPort());
          }
        };
    return request(request);
  }

  @Override
  public Response request(Request request) throws RequestException {
    LOGGER.info(
        "[request] MetaNode Exchanger request={},url={},callbackHandler={}",
        request.getRequestBody(),
        request.getRequestUrl(),
        request.getCallBackHandler());
    try {
      return super.request(request);
    } catch (Throwable e) {
      // retry
      URL url = new URL(getLeader(), getServerPort());
      LOGGER.warn(
          "[request] MetaNode Exchanger request send error!It will be retry once!Request url:{}",
          url);
      return super.request(request);
    }
  }

  @Override
  protected Collection<ChannelHandler> getClientHandlers() {
    return metaClientHandlers;
  }

  private String getLeader() {
    if (StringUtils.isEmpty(metaLeader) || metaLeaderEpoch == INIT_META_LEADER_EPOCH) {
      int index = metaServerIndex.incrementAndGet() % defaultMetaServers.size();
      metaServerIndex.set(index);
      return defaultMetaServers.get(index);
    }
    return metaLeader;
  }

  private List<String> getConfiguredMetaIp() {
    Set<String> set = Sets.newHashSet();
    Collection<String> metaDomains = getConfiguredMetaServerDomains();
    metaDomains.forEach(
        domain -> {
          String ip = NetUtil.getIPAddressFromDomain(domain);
          if (ip == null) {
            throw new SofaRegistryRuntimeException(
                "Node config convert domain {" + domain + "} error!");
          }
          set.add(ip);
        });
    return Lists.newArrayList(set);
  }

  protected abstract Collection<String> getConfiguredMetaServerDomains();
}
