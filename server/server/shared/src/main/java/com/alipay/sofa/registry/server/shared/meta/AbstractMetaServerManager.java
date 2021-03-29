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

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.MetaLeaderQueryException;
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
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.shared.comparator.ComparatorVisitor;
import com.alipay.sofa.registry.server.shared.comparator.NodeComparator;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.util.JsonUtils;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;

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

  protected volatile List<String> defaultMetaServers;

  protected volatile String metaLeader;

  protected volatile long metaLeaderEpoch = INIT_META_LEADER_EPOCH;

  protected static final String metaLeaderQueryUrl = "http://%s:9615/meta/leader/query";

  private static final String leaderKey = "leader";
  private static final String epochKey = "epoch";

  private final Retryer<Boolean> retryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfException()
          .retryIfResult(
              new Predicate<Boolean>() {
                @Override
                public boolean apply(Boolean input) {
                  return !input;
                }
              })
          .withWaitStrategy(WaitStrategies.exponentialWait(1000, 5000, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(5))
          .build();

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

  @Override
  public String getMetaServerLeader() {

    if (StringUtils.isEmpty(metaLeader) || metaLeaderEpoch == INIT_META_LEADER_EPOCH) {
      LeaderInfo leaderInfo = resetLeaderFromRestServer();
      if (leaderInfo == null || StringUtil.isEmpty(leaderInfo.getLeader())) {
        throw new MetaLeaderQueryException("query meta leader fail.");
      }
    }
    return getLeader();
  }

  @Override
  public void refresh(BaseHeartBeatResponse heartBeatResp) {
    String futureLeader = heartBeatResp.getMetaLeader();
    long futureEpoch = heartBeatResp.getMetaLeaderEpoch();

    List<String> metaServers = new ArrayList<>();
    metaServers.add(heartBeatResp.getMetaLeader());
    doRefreshConnections(metaServers);

    // set leader
    setLeader(futureLeader, futureEpoch);
  }

  @VisibleForTesting
  protected void doRefreshConnections(List<String> metaServers) {
    Set<String> future = Sets.newHashSet(metaServers);
    NodeComparator diff = new NodeComparator(this.serverIps, future);
    diff.accept(
        new ComparatorVisitor<String>() {
          @Override
          public void visitAdded(String added) {
            try {
              LOGGER.info("[visitAdded] connect new meta-server: {}", added);
              URL url = new URL(added, getServerPort());
              connect(url);
            } catch (Throwable th) {
              LOGGER.error("[visitAdded]", th);
            }
          }

          @Override
          public void visitModified(Tuple<String, String> modified) {
            // do nothing
          }

          @Override
          public void visitRemoved(String removed) {
            try {
              LOGGER.info("[visitRemoved] close meta-server connection: {}", removed);
              boltExchange
                  .getClient(serverType)
                  .getChannel(new URL(removed, getServerPort()))
                  .close();
            } catch (Throwable th) {
              LOGGER.error("[visitRemoved]", th);
            }
          }

          @Override
          public void visitRemaining(String remain) {
            // do nothing
          }
        });

    setServerIps(metaServers);
  }

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
            return new URL(getMetaServerLeader(), getServerPort());
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
      URL url = new URL(getMetaServerLeader(), getServerPort());
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

  protected void setLeader(String leader, long epoch) {
    try {
      lock.writeLock().lock();
      if (StringUtils.isNotEmpty(leader) && epoch >= metaLeaderEpoch) {
        metaLeader = leader;
        metaLeaderEpoch = epoch;
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  private String getLeader() {

    try {
      lock.readLock().lock();
      return metaLeader;
    } finally {
      lock.readLock().unlock();
    }
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

  @Override
  public LeaderInfo resetLeaderFromRestServer() {
    LeaderInfo leaderInfo = new LeaderInfo();

    try {
      retryer.call(
          () -> {
            Collection<String> metaDomains = getConfiguredMetaServerDomains();
            for (String metaDomain : metaDomains) {

              String url = String.format(metaLeaderQueryUrl, metaDomain);
              javax.ws.rs.core.Response resp =
                  JerseyClient.getInstance().getClient().target(url).request().buildGet().invoke();

              if (resp.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                if (LOGGER.isInfoEnabled()) {
                  LOGGER.info(
                      "[resetLeaderFromRestServer] query from url: {}, resp status: {}",
                      url,
                      resp.getStatus());
                }
                continue;
              }
              GenericResponse genericResponse = new GenericResponse<>();
              genericResponse = resp.readEntity(genericResponse.getClass());

              if (!genericResponse.isSuccess() || genericResponse.getData() == null) {
                if (LOGGER.isInfoEnabled()) {
                  LOGGER.info(
                      "[resetLeaderFromRestServer] query from url: {}, resp: {}",
                      url,
                      JsonUtils.writeValueAsString(genericResponse));
                }
                continue;
              }
              LinkedHashMap data = (LinkedHashMap) genericResponse.getData();
              Long epoch = (Long) data.get(epochKey);
              String leader = (String) data.get(leaderKey);
              leaderInfo.setEpoch(epoch);
              leaderInfo.setLeader(leader);
              if (LOGGER.isInfoEnabled()) {
                LOGGER.info(
                    "[resetLeaderFromRestServer] query from url: {}, meta leader:{}",
                    url,
                    leaderInfo);
              }
              return StringUtil.isNotEmpty(leader);
            }
            return false;
          });
    } catch (Exception e) {
      LOGGER.error("query meta leader error.");
    }

    // connect to meta leader
    connect(new URL(leaderInfo.getLeader(), getServerPort()));
    setLeader(leaderInfo.getLeader(), leaderInfo.getEpoch());
    return leaderInfo;
  }

  protected abstract Collection<String> getConfiguredMetaServerDomains();
}
