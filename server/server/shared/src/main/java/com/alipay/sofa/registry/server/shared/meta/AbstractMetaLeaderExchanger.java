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
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.elector.DistributeLockInfo;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.MetaLeaderQueryException;
import com.alipay.sofa.registry.jdbc.elector.MetaJdbcLeaderElector;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.shared.constant.MetaLeaderLearnModeEnum;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.elector.DistributeLockRepository;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : AbstractMetaLeaderExchanger.java, v 0.1 2022年04月16日 16:34 xiaojian.xj Exp $
 */
public abstract class AbstractMetaLeaderExchanger extends ClientSideExchanger
    implements MetaLeaderExchanger {

  private final Logger LOGGER;

  private final Map<String, LeaderInfo> leaderInfo = Maps.newConcurrentMap();

  protected final Retryer<LeaderInfo> retryer =
      RetryerBuilder.<LeaderInfo>newBuilder()
          .retryIfException()
          .retryIfResult(Objects::isNull)
          .withWaitStrategy(WaitStrategies.exponentialWait(1000, 3000, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(5))
          .build();

  @Autowired protected DefaultCommonConfig defaultCommonConfig;

  @Autowired private DistributeLockRepository distributeLockRepository;

  private javax.ws.rs.client.Client rsClient;

  private static final String LEADER_KEY = "leader";
  private static final String EPOCH_KEY = "epoch";

  protected AbstractMetaLeaderExchanger(String serverType, Logger logger) {
    super(serverType);
    this.LOGGER = logger;
  }

  @PostConstruct
  public void init() {
    super.init();
    rsClient = JerseyClient.getInstance().getClient();
  }

  /**
   * send request to remote cluster meta leader
   *
   * @param dataCenter
   * @param requestBody
   * @return
   * @throws RequestException
   */
  @Override
  public Response sendRequest(String dataCenter, Object requestBody) throws RequestException {
    Request request =
        new Request() {
          @Override
          public Object getRequestBody() {
            return requestBody;
          }

          @Override
          public URL getRequestUrl() {
            return new URL(getLeader(dataCenter).getLeader(), getServerPort());
          }
        };
    LOGGER.info(
        "[request] MetaNode Exchanger dataCenter={},request={},url={},callbackHandler={}",
        dataCenter,
        request.getRequestBody(),
        request.getRequestUrl(),
        request.getCallBackHandler());

    try {
      return super.request(request);
    } catch (Throwable e) {
      // retry
      resetLeader(dataCenter);
      URL url = new URL(getLeader(dataCenter).getLeader(), getServerPort());
      LOGGER.warn(
          "[request] MetaNode Exchanger request send error!It will be retry once!Request url:{}",
          url);
      return super.request(request);
    }
  }

  /**
   * learn leader from remote resp
   *
   * @param dataCenter
   * @param update
   */
  @Override
  public synchronized boolean learn(String dataCenter, LeaderInfo update) {

    final LeaderInfo exist = this.leaderInfo.get(dataCenter);
    if (exist == null) {
      leaderInfo.put(dataCenter, update);
      setServerIps(Collections.singleton(update.getLeader()));
      return true;
    }
    if (update.getEpoch() < exist.getEpoch()) {
      LOGGER.warn(
          "[setLeaderConflict]dataCenter={},exist={}/{},input={}/{}",
          dataCenter,
          exist.getEpoch(),
          exist.getLeader(),
          update.getEpoch(),
          update.getLeader());
      return false;
    } else if (exist.getEpoch() < update.getEpoch()) {
      leaderInfo.put(dataCenter, update);
      setServerIps(Collections.singleton(update.getLeader()));
    }
    return true;
  }

  protected abstract MetaLeaderLearnModeEnum getMode();

  /**
   * reset leader from remoteMetaDomain
   *
   * @param dataCenter
   */
  @Override
  public LeaderInfo resetLeader(String dataCenter) {
    LeaderInfo leader = null;
    MetaLeaderLearnModeEnum mode = getMode();
    if (mode == MetaLeaderLearnModeEnum.JDBC) {
      leader = queryLeaderFromDb();
    } else if (mode == MetaLeaderLearnModeEnum.LOADBALANCER) {
      leader = queryLeaderFromRest(dataCenter);
    }

    // connect to meta leader
    connect(new URL(leader.getLeader(), getServerPort()));
    // learn leader from resp
    learn(dataCenter, leader);
    return leaderInfo.get(dataCenter);
  }

  /**
   * get leader info
   *
   * @param dataCenter
   * @return
   */
  @Override
  public LeaderInfo getLeader(String dataCenter) {
    LeaderInfo leader = leaderInfo.get(dataCenter);
    if (leader != null) {
      return new LeaderInfo(leader.getEpoch(), leader.getLeader());
    }
    return resetLeader(dataCenter);
  }

  /**
   * remove leader
   *
   * @param dataCenter
   */
  @Override
  public void removeLeader(String dataCenter) {
    leaderInfo.remove(dataCenter);
  }

  protected LeaderInfo queryLeaderFromDb() {
    try {
      return retryer.call(
          () -> {
            DistributeLockInfo lock =
                distributeLockRepository.queryDistLock(MetaJdbcLeaderElector.lockName);
            if (!validateLockLeader(lock)) {
              return null;
            }
            String leader = lock.getOwner();
            long epoch = lock.getGmtModifiedUnixMillis();
            return new LeaderInfo(epoch, leader);
          });
    } catch (Throwable e) {
      throw new MetaLeaderQueryException(
          StringFormatter.format("query meta leader error from db failed"), e);
    }
  }

  private boolean validateLockLeader(DistributeLockInfo lock) {
    if (lock == null) {
      LOGGER.error("[resetLeaderFromDb] failed to query leader from db: lock null");
      return false;
    }
    long expireTimestamp = lock.getGmtModifiedUnixMillis() + lock.getDuration() / 2;
    long now = System.currentTimeMillis();
    if (expireTimestamp < now) {
      LOGGER.error("[resetLeaderFromDb] failed to query leader from db: lock expired {}", lock);
      return false;
    }
    return true;
  }

  private LeaderInfo queryLeaderFromRest(String dataCenter) {
    Collection<String> metaDomains = getMetaServerDomains(dataCenter);

    try {
      return retryer.call(() -> queryLeaderInfo(dataCenter, metaDomains, rsClient));
    } catch (Throwable e) {
      throw new MetaLeaderQueryException(
          StringFormatter.format("query meta leader from {} failed", metaDomains), e);
    }
  }

  private LeaderInfo queryLeaderInfo(
      String dataCenter, Collection<String> metaDomains, javax.ws.rs.client.Client client) {
    for (String metaDomain : metaDomains) {
      String url = String.format(ValueConstants.META_LEADER_QUERY_URL, metaDomain);
      try {
        javax.ws.rs.core.Response resp = client.target(url).request().buildGet().invoke();
        LeaderInfo ret = handleResp(dataCenter, url, resp);
        if (ret != null) {
          return ret;
        }
      } catch (Throwable e) {
        LOGGER.error(
            "[resetLeaderFromRestServer] dataCenter:{} failed to query from url: {}",
            dataCenter,
            url,
            e);
      }
    }
    return null;
  }

  protected LeaderInfo handleResp(String dataCenter, String url, javax.ws.rs.core.Response resp) {
    if (resp.getStatus() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
      LOGGER.error(
          "[resetLeaderFromRestServer] dataCenter:{} failed to query from url: {}, resp status: {}",
          dataCenter,
          url,
          resp.getStatus());
      return null;
    }
    GenericResponse genericResponse = new GenericResponse<>();
    genericResponse = resp.readEntity(genericResponse.getClass());

    if (!genericResponse.isSuccess() || genericResponse.getData() == null) {
      LOGGER.error(
          "[resetLeaderFromRestServer] dataCenter:{} failed to query from url: {}, resp: {}",
          dataCenter,
          url,
          JsonUtils.writeValueAsString(genericResponse));
      return null;
    }
    Map data = (Map) genericResponse.getData();
    Long epoch = (Long) data.get(EPOCH_KEY);
    String leader = (String) data.get(LEADER_KEY);
    if (StringUtils.isBlank(leader)) {
      return null;
    }
    LeaderInfo query = new LeaderInfo(epoch, leader);
    LOGGER.info(
        "[resetLeaderFromRestServer] dataCenter:{} query from url: {}, meta leader:{}",
        dataCenter,
        url,
        query);
    return query;
  }

  protected abstract Collection<String> getMetaServerDomains(String dataCenter);
}
