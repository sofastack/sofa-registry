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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.sessionserver.StopPushRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.resource.AuthChecker;
import com.alipay.sofa.registry.server.shared.util.PersistenceDataParser;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : StopPushResource.java, v 0.1 2021年10月25日 10:24 xiaojian.xj Exp $
 */
@Path("api/emergency")
@Produces(MediaType.APPLICATION_JSON)
public class EmergencyApiResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyApiResource.class);

  @Autowired protected ProvideDataRepository provideDataRepository;

  @Autowired protected MetaServerService metaServerService;

  @Autowired protected SessionServerConfig sessionServerConfig;

  @Autowired protected ExecutorManager executorManager;

  @Autowired private NodeExchanger sessionConsoleExchanger;

  @Resource private FetchStopPushService fetchStopPushService;

  protected static final String META_LEADER_HEALTHY_URL = "http://%s:9615/health/check";

  private Client client;

  @PostConstruct
  public void init() {
    client = JerseyClient.getInstance().getClient();
  }

  /**
   * close push by repository when meta leader is down
   *
   * @return CommonResponse
   */
  @POST
  @Path("/pushSwitch/stop")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse closePushByRepository() {
    boolean leaderHealthy = leaderHealthy();
    boolean renewSuccess = metaServerService.renewNode();
    LOGGER.info(
        "[closePushByRepository]leaderHealthy: {}, renewSuccess: {}", leaderHealthy, renewSuccess);
    if (leaderHealthy || renewSuccess) {
      LOGGER.error("leader is healthy, should not stop server by session.");
      return CommonResponse.buildFailedResponse(
          "leader is healthy, should not close push by session.");
    }

    PersistenceData update =
        PersistenceDataBuilder.createPersistenceDataForBool(
            ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, true);
    provideDataRepository.put(update);
    return CommonResponse.buildSuccessResponse();
  }

  /**
   * stop server by repository when meta leader is down
   *
   * @param token token
   * @return CommonResponse
   */
  @POST
  @Path("/shutdown")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse shutdownByRepository(@FormParam("token") String token) {
    if (!AuthChecker.authCheck(token)) {
      LOGGER.error("update shutdownSwitch, stop=true auth check={} fail!", token);
      return GenericResponse.buildFailedResponse("auth check fail");
    }

    if (!isStopPush()) {
      LOGGER.error("push switch is open");
      return GenericResponse.buildFailedResponse("push switch is open");
    }

    boolean leaderHealthy = leaderHealthy();
    boolean renewSuccess = metaServerService.renewNode();
    LOGGER.info(
        "[shutdownByRepository]leaderHealthy: {}, renewSuccess: {}", leaderHealthy, renewSuccess);
    if (leaderHealthy || renewSuccess) {
      LOGGER.error("leader is healthy, should not stop server by session.");
      return CommonResponse.buildFailedResponse(
          "leader is healthy, should not stop server by session.");
    }

    PersistenceData update =
        PersistenceDataBuilder.createPersistenceDataForBool(
            ValueConstants.SHUTDOWN_SWITCH_DATA_ID, true);
    provideDataRepository.put(update);
    return CommonResponse.buildSuccessResponse();
  }

  /**
   * close push broadcast on zone when meta leader is down
   *
   * @return CommonResponse
   */
  @GET
  @Path("/pushSwitch/zone/stop")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse zoneClosePush() {
    fetchStopPushService.setStopPushSwitch(System.currentTimeMillis(), true);
    List<URL> servers = Sdks.getOtherConsoleServers(null, sessionServerConfig, metaServerService);
    if (servers.size() == 0) {
      return CommonResponse.buildSuccessResponse();
    }

    Map<URL, CommonResponse> map =
        Sdks.concurrentSdkSend(
            executorManager.getZoneSdkExecutor(),
            servers,
            (URL url) -> {
              final StopPushRequest req = new StopPushRequest(true);
              return (CommonResponse)
                  sessionConsoleExchanger.request(new SimpleRequest(req, url)).getResult();
            },
            3000);
    return Sdks.getFailedResponseIfAbsent(map.values());
  }

  private boolean leaderHealthy() {
    String leader = metaServerService.getMetaServerLeader();
    String url = String.format(META_LEADER_HEALTHY_URL, leader);
    try {
      Response resp = client.target(url).request().buildGet().invoke();
      if (resp.getStatus() != Response.Status.OK.getStatusCode()) {
        LOGGER.error(
            "[leaderHealthy] failed to query leader status from url: {}, resp status: {}",
            url,
            resp.getStatus());
        return false;
      }
      CommonResponse commonResponse = new CommonResponse();
      commonResponse = resp.readEntity(commonResponse.getClass());
      LOGGER.info("[leaderHealthy]url: {}, resp: {}", url, commonResponse);
      return commonResponse.isSuccess();
    } catch (Throwable t) {
      LOGGER.error("[leaderHealthy] failed to query leader status from url: {}", url, t);
    }
    return false;
  }

  private boolean isStopPush() {
    PersistenceData stopPushResp =
        provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    return PersistenceDataParser.parse2BoolIgnoreCase(stopPushResp, false);
  }

  /**
   * Setter method for property <tt>metaNodeService</tt>.
   *
   * @param metaServerService value to be assigned to property metaNodeService
   * @return EmergencyApiResource
   */
  @VisibleForTesting
  public EmergencyApiResource setMetaNodeService(MetaServerService metaServerService) {
    this.metaServerService = metaServerService;
    return this;
  }
}
