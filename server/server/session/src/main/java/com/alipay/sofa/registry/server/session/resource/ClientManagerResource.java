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

  private volatile boolean enableTrafficOperate = true;

  /**
   * Turns off (disconnects or disables traffic for) client connections for the given IPs.
   *
   * Accepts a single string of one or more IP addresses (e.g., comma- or whitespace-separated) which will be parsed into a set of IPs. For each matching connection, the method looks up ConnectId values and instructs the session registry to "client off" those connections.
   *
   * If the input is empty the call fails. If traffic operations are globally disabled via the feature flag, the call returns a failure indicating rate limiting.
   *
   * @param ips a string containing one or more IP addresses to target (will be parsed into a set)
   * @return a CommonResponse indicating success, or a failure with a message explaining the reason
   */
  @POST
  @Path("/clientOff")
  public CommonResponse clientOff(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }

    if (!this.enableTrafficOperate) {
      // 限流，不允许操作开关流
      return CommonResponse.buildFailedResponse("too many request");
    }

    final Set<String> ipSet = CollectionSdks.toIpSet(ips);
    List<ConnectId> conIds = connectionsService.getIpConnects(ipSet);
    sessionRegistry.clientOff(conIds);
    LOGGER.info("clientOff ips={}, conIds={}", ips, conIds);
    return CommonResponse.buildSuccessResponse();
  }

  /**
   * Re-opens clients (allows traffic) for the given IP addresses by closing any existing IP-level
   * connection blocks.
   *
   * The method parses the provided `ips` string into individual IPs, invokes the connections
   * service to close IP-based connection restrictions for those IPs, and returns a success response.
   * It will return a failure response if `ips` is empty or if traffic operations are currently disabled.
   *
   * @param ips a delimited string of IP addresses (e.g. comma- or newline-separated); parsed into a list internally
   * @return a CommonResponse indicating success, or a failure response when `ips` is empty or traffic operations are disabled
   */
  @POST
  @Path("/clientOpen")
  public CommonResponse clientOn(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }

    if (!this.enableTrafficOperate) {
      // 限流，不允许操作开关流
      return CommonResponse.buildFailedResponse("too many request");
    }

    final List<String> ipList = CollectionSdks.toIpList(ips);
    List<String> conIds = connectionsService.closeIpConnects(ipList);
    LOGGER.info("clientOn ips={}, conIds={}", ips, conIds);

    return CommonResponse.buildSuccessResponse();
  }

  /**
   * Disables (forces off) client connections for the given IPs across the current zone.
   *
   * This call first performs the operation locally; if that succeeds it forwards the same
   * request concurrently to other console servers in the same zone and returns success only
   * if all servers succeed.
   *
   * @param ips comma-separated list of IP addresses (or any delimiter supported by CollectionSdks.toIpList)
   * @return a CommonResponse indicating overall success; failure is returned if `ips` is empty,
   *         if traffic operations are disabled via the enableTrafficOperate flag, or if any
   *         target server (local or remote) reports a failure
   */
  @POST
  @Path("/zone/clientOff")
  public CommonResponse clientOffInZone(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }

    if (!this.enableTrafficOperate) {
      // 限流，不允许操作开关流
      return CommonResponse.buildFailedResponse("too many request");
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
   * Enables (opens) client connections for the given IPs across the current zone.
   *
   * <p>Validates the input and a feature-flag that gates traffic operations. If local enable succeeds,
   * the method propagates the same "client open" request to other console servers in the same zone
   * and returns a failure if any remote server reports failure.
   *
   * @param ips comma-separated list of client IP addresses to open
   * @return a CommonResponse indicating success or failure (validation failure, traffic operations
   *     disabled, or any remote-server failure)
   */
  @POST
  @Path("/zone/clientOpen")
  public CommonResponse clientOnInZone(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }

    if (!this.enableTrafficOperate) {
      // 限流，不允许操作开关流
      return CommonResponse.buildFailedResponse("too many request");
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

  /**
   * Returns the URLs of other console servers in the same zone.
   *
   * Uses the configured SessionServerConfig and MetaServerService to discover console
   * servers and excludes the local server from the result. The returned list may be
   * empty if no other servers are found.
   *
   * @return a list of URLs for other console servers in the current zone (possibly empty)
   */
  public List<URL> getOtherConsoleServersCurrentZone() {
    return Sdks.getOtherConsoleServers(null, sessionServerConfig, metaServerService);
  }

  /**
   * Returns whether traffic operations (client on/off) are currently allowed.
   *
   * @return true if traffic operations are enabled; false if they are blocked (rate-limited)
   */
  public boolean isEnableTrafficOperate() {
    return enableTrafficOperate;
  }

  /**
   * Enable or disable traffic operations (client on/off endpoints).
   *
   * @param enableTrafficOperate true to allow traffic operations; false to reject them (requests will return a rate-limit/failure response)
   */
  public void setEnableTrafficOperate(boolean enableTrafficOperate) {
    this.enableTrafficOperate = enableTrafficOperate;
  }
}
