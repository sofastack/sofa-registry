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

import com.alipay.sofa.registry.common.model.CollectionSdks;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerQueryRequest;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerResp;
import com.alipay.sofa.registry.common.model.sessionserver.ClientOffRequest;
import com.alipay.sofa.registry.common.model.sessionserver.ClientOnRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.mapper.ConnectionMapper;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The type Clients open resource.
 *
 * @author kezhu.wukz
 * @version $Id : ClientsResource.java, v 0.1 2018-11-22 19:04 kezhu.wukz Exp $$
 */
@Path("api/clientManager")
@Produces(MediaType.APPLICATION_JSON)
public class ClientManagerResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientManagerResource.class);

  @Autowired private SessionRegistry sessionRegistry;

  @Autowired private SessionServerConfig sessionServerConfig;
  @Autowired private MetaServerService metaServerService;
  @Autowired private ConnectionsService connectionsService;

  @Autowired private ConnectionMapper connectionMapper;

  @Autowired private NodeExchanger sessionConsoleExchanger;

  @Resource private FetchClientOffAddressService fetchClientOffAddressService;

  @Autowired protected ExecutorManager executorManager;

  /**
   * Client off
   *
   * @param ips ips
   * @return CommonResponse
   */
  @POST
  @Path("/clientOff")
  public CommonResponse clientOff(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    final Set<String> ipSet = CollectionSdks.toIpSet(ips);
    List<ConnectId> conIds = connectionsService.getIpConnects(ipSet);
    sessionRegistry.clientOff(conIds);
    LOGGER.info("clientOff ips={}, conIds={}", ips, conIds);
    return CommonResponse.buildSuccessResponse();
  }

  /**
   * Client on
   *
   * @param ips ips
   * @return CommonResponse
   */
  @POST
  @Path("/clientOpen")
  public CommonResponse clientOn(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    final List<String> ipList = CollectionSdks.toIpList(ips);
    List<String> conIds = connectionsService.closeIpConnects(ipList);
    LOGGER.info("clientOn ips={}, conIds={}", ips, conIds);

    return CommonResponse.buildSuccessResponse();
  }

  /**
   * Client off
   *
   * @param ips ips
   * @return CommonResponse
   */
  @POST
  @Path("/zone/clientOff")
  public CommonResponse clientOffInZone(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    CommonResponse resp = clientOff(ips);
    if (!resp.isSuccess()) {
      return resp;
    }

    final List<String> ipList = CollectionSdks.toIpList(ips);
    List<URL> servers = getOtherConsoleServersCurrentZone();
    LOGGER.info("clientOffInZone, others={}", servers);
    if (servers.size() > 0) {
      Map<URL, CommonResponse> map =
          Sdks.concurrentSdkSend(
              executorManager.getZoneSdkExecutor(),
              servers,
              (URL url) -> {
                final ClientOffRequest req = new ClientOffRequest(ipList);
                return (CommonResponse)
                    sessionConsoleExchanger.request(new SimpleRequest(req, url)).getResult();
              },
              3000);
      return Sdks.getFailedResponseIfAbsent(map.values());
    }
    return CommonResponse.buildSuccessResponse();
  }

  /**
   * Client on
   *
   * @param ips ips
   * @return CommonResponse
   */
  @POST
  @Path("/zone/clientOpen")
  public CommonResponse clientOnInZone(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    CommonResponse resp = clientOn(ips);
    if (!resp.isSuccess()) {
      return resp;
    }
    final List<String> ipList = CollectionSdks.toIpList(ips);
    List<URL> servers = getOtherConsoleServersCurrentZone();
    LOGGER.info("clientOnInZone, others={}", servers);
    if (servers.size() > 0) {
      Map<URL, CommonResponse> map =
          Sdks.concurrentSdkSend(
              executorManager.getZoneSdkExecutor(),
              servers,
              (URL url) -> {
                final ClientOnRequest req = new ClientOnRequest(ipList);
                return (CommonResponse)
                    sessionConsoleExchanger.request(new SimpleRequest(req, url)).getResult();
              },
              3000);
      return Sdks.getFailedResponseIfAbsent(map.values());
    }
    return CommonResponse.buildSuccessResponse();
  }

  /**
   * Client on
   *
   * @return GenericResponse
   */
  @POST
  @Path("/zone/queryClientOff")
  public GenericResponse<Map<String, ClientManagerResp>> queryClientOff() {
    Set<String> clientOffAddress = fetchClientOffAddressService.getClientOffAddress();
    List<URL> servers = getOtherConsoleServersCurrentZone();

    Map<String, ClientManagerResp> resp = Maps.newHashMapWithExpectedSize(servers.size() + 1);

    resp.put(ServerEnv.IP, new ClientManagerResp(true, clientOffAddress));
    if (servers.size() > 0) {
      Map<URL, CommonResponse> map =
          Sdks.concurrentSdkSend(
              executorManager.getZoneSdkExecutor(),
              servers,
              (URL url) -> {
                final ClientManagerQueryRequest req = new ClientManagerQueryRequest();
                return (CommonResponse)
                    sessionConsoleExchanger.request(new SimpleRequest(req, url)).getResult();
              },
              3000);

      for (Entry<URL, CommonResponse> entry : map.entrySet()) {
        if (entry.getValue() instanceof GenericResponse) {
          GenericResponse response = (GenericResponse) entry.getValue();
          if (response.isSuccess()) {
            resp.put(entry.getKey().getIpAddress(), (ClientManagerResp) response.getData());
            continue;
          }
        }
        LOGGER.error(
            "url={} queryClientOff fail, msg:{}.", entry.getKey().getIpAddress(), entry.getValue());
        resp.put(entry.getKey().getIpAddress(), new ClientManagerResp(false));
      }
    }
    return new GenericResponse().fillSucceed(resp);
  }

  @GET
  @Path("/connectionMapper.json")
  public Map<String, String> connectionMapper() {
    return connectionMapper.get();
  }

  public List<URL> getOtherConsoleServersCurrentZone() {
    return Sdks.getOtherConsoleServers(null, sessionServerConfig, metaServerService);
  }
}
