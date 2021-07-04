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
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.MetaLeaderQueryException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.util.JsonUtils;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.client.Client;
import org.apache.commons.lang.StringUtils;

/**
 * @author chen.zhu
 *     <p>Mar 15, 2021
 */
public abstract class AbstractMetaServerManager extends ClientSideExchanger
    implements MetaServerManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetaServerManager.class);

  @Resource(name = "metaClientHandlers")
  private Collection<ChannelHandler> metaClientHandlers;

  protected volatile LeaderInfo metaLeaderInfo;

  protected static final String META_LEADER_QUERY_URL = "http://%s:9615/meta/leader/query";

  static final String LEADER_KEY = "leader";
  static final String EPOCH_KEY = "epoch";

  private final Retryer<LeaderInfo> retryer =
      RetryerBuilder.<LeaderInfo>newBuilder()
          .retryIfException()
          .retryIfResult(
              new Predicate<LeaderInfo>() {
                @Override
                public boolean apply(LeaderInfo input) {
                  return input == null;
                }
              })
          .withWaitStrategy(WaitStrategies.exponentialWait(1000, 3000, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(5))
          .build();

  private javax.ws.rs.client.Client rsClient;

  protected AbstractMetaServerManager() {
    super(Exchange.META_SERVER_TYPE);
  }

  @PostConstruct
  public void init() {
    super.init();
    rsClient = JerseyClient.getInstance().getClient();
  }

  @Override
  public String getMetaServerLeader() {
    if (metaLeaderInfo == null) {
      resetLeaderFromRestServer();
    }
    return metaLeaderInfo.getLeader();
  }

  @Override
  public void refresh(BaseHeartBeatResponse heartBeatResp) {
    String futureLeader = heartBeatResp.getMetaLeader();
    long futureEpoch = heartBeatResp.getMetaLeaderEpoch();
    if (StringUtils.isBlank(futureLeader)) {
      LOGGER.warn("heartbeat response not contains leader");
      return;
    }
    // set leader
    setLeader(new LeaderInfo(futureEpoch, futureLeader));
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
      resetLeaderFromRestServer();
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

  private void setLeader(LeaderInfo leader) {
    if (trySetLeader(leader)) {
      LOGGER.info("update leader {}", leader);
    }
  }

  private boolean trySetLeader(LeaderInfo leader) {
    synchronized (this) {
      if (metaLeaderInfo == null || metaLeaderInfo.getEpoch() < leader.getEpoch()) {
        this.metaLeaderInfo = leader;
        // do not need to refresh the connect, the daemon connector will to this
        // TODO now not close the conn which is not leader, the num of conn is small
        this.setServerIps(Collections.singleton(leader.getLeader()));
        return true;
      }
      return false;
    }
  }

  @Override
  public LeaderInfo resetLeaderFromRestServer() {
    LeaderInfo leaderInfo = null;
    Collection<String> metaDomains = getConfiguredMetaServerDomains();
    try {
      leaderInfo = retryer.call(() -> queryLeaderInfo(metaDomains, rsClient));
    } catch (Throwable e) {
      throw new MetaLeaderQueryException("query meta leader error from " + metaDomains, e);
    }
    // connect to meta leader
    connect(new URL(leaderInfo.getLeader(), getServerPort()));
    setLeader(leaderInfo);
    return this.metaLeaderInfo;
  }

  protected abstract Collection<String> getConfiguredMetaServerDomains();

  static LeaderInfo queryLeaderInfo(
      Collection<String> metaDomains, javax.ws.rs.client.Client client) {
    for (String metaDomain : metaDomains) {
      String url = String.format(META_LEADER_QUERY_URL, metaDomain);
      try {
        javax.ws.rs.core.Response resp = client.target(url).request().buildGet().invoke();
        if (resp.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
          LOGGER.error(
              "[resetLeaderFromRestServer] failed to query from url: {}, resp status: {}",
              url,
              resp.getStatus());
          continue;
        }
        GenericResponse genericResponse = new GenericResponse<>();
        genericResponse = resp.readEntity(genericResponse.getClass());

        if (!genericResponse.isSuccess() || genericResponse.getData() == null) {
          LOGGER.error(
              "[resetLeaderFromRestServer] failed to query from url: {}, resp: {}",
              url,
              JsonUtils.writeValueAsString(genericResponse));
          continue;
        }
        Map data = (Map) genericResponse.getData();
        Long epoch = (Long) data.get(EPOCH_KEY);
        String leader = (String) data.get(LEADER_KEY);
        if (StringUtils.isBlank(leader)) {
          continue;
        }
        LeaderInfo query = new LeaderInfo(epoch, leader);
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("[resetLeaderFromRestServer] query from url: {}, meta leader:{}", url, query);
        }
        return query;
      } catch (Throwable e) {
        LOGGER.error("[resetLeaderFromRestServer] failed to query from url: {}", url, e);
        continue;
      }
    }
    return null;
  }

  @VisibleForTesting
  void setRsClient(Client rsClient) {
    this.rsClient = rsClient;
  }
}
