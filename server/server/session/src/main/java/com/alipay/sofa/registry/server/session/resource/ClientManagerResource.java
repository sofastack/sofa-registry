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
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.mapper.ConnectionMapper;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

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
  @Autowired private MetaServerService metaNodeService;
  @Autowired private ConnectionsService connectionsService;

  @Autowired private ConnectionMapper connectionMapper;

  /** Client off */
  @POST
  @Path("/clientOff")
  public CommonResponse clientOff(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    String[] ipArray = StringUtils.split(ips.trim(), ';');
    List<String> ipList = Arrays.asList(ipArray);

    List<ConnectId> conIds = connectionsService.getIpConnects(ipList);

    if (!CollectionUtils.isEmpty(conIds)) {
      sessionRegistry.remove(conIds);
      LOGGER.info("clientOff conIds: {}", conIds.toString());
    }

    return CommonResponse.buildSuccessResponse();
  }

  /** Client on */
  @POST
  @Path("/clientOpen")
  public CommonResponse clientOn(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    String[] ipArray = StringUtils.split(ips.trim(), ';');
    List<String> ipList = Arrays.asList(ipArray);

    if (!CollectionUtils.isEmpty(ipList)) {
      List<String> conIds = connectionsService.closeIpConnects(ipList);
      LOGGER.info("clientOn conIds: {}", conIds.toString());
    }

    return CommonResponse.buildSuccessResponse();
  }

  /** Client off */
  @POST
  @Path("/zone/clientOff")
  public CommonResponse clientOffInZone(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    List<URL> servers = getOtherServersCurrentZone();
    List<CommonResponse> responses = new ArrayList<>(servers.size() + 1);
    responses.add(clientOff(ips));
    final CountDownLatch latch = new CountDownLatch(servers.size());
    if (servers.size() > 0) {
      ExecutorService es = Executors.newFixedThreadPool(servers.size());
      servers.forEach(
          url ->
              es.submit(
                  () -> {
                    try {
                      JerseyClient jerseyClient = JerseyClient.getInstance();
                      MultivaluedMap formData = new MultivaluedHashMap();
                      formData.add("ips", ips);
                      responses.add(
                          jerseyClient
                              .getClient()
                              .target(
                                  new URI(
                                      String.format(
                                          "http://%s:%d", url.getIpAddress(), url.getPort())))
                              .path("/api/clientManager/clientOff")
                              .request()
                              .buildPost(Entity.form(formData))
                              .invoke(CommonResponse.class));
                    } catch (Exception e) {
                      LOGGER.error("send clientoff other session error!url={}", url, e);
                      responses.add(new CommonResponse(false, e.getMessage()));
                    } finally {
                      latch.countDown();
                    }
                  }));
    }
    try {
      latch.await(3000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      return CommonResponse.buildFailedResponse("client off timeout");
    }
    Optional<CommonResponse> failedResponses =
        responses.stream().filter(r -> !r.isSuccess()).findFirst();
    return failedResponses.orElseGet(CommonResponse::buildSuccessResponse);
  }

  /** Client on */
  @POST
  @Path("/zone/clientOpen")
  public CommonResponse clientOnInZone(@FormParam("ips") String ips) {
    if (StringUtils.isEmpty(ips)) {
      return CommonResponse.buildFailedResponse("ips is empty");
    }
    List<URL> servers = getOtherServersCurrentZone();
    List<CommonResponse> responses = new ArrayList<>(servers.size() + 1);
    responses.add(clientOn(ips));
    final CountDownLatch latch = new CountDownLatch(servers.size());
    if (servers.size() > 0) {
      ExecutorService es = Executors.newFixedThreadPool(servers.size());
      servers.forEach(
          url ->
              es.submit(
                  () -> {
                    try {
                      JerseyClient jerseyClient = JerseyClient.getInstance();
                      MultivaluedMap formData = new MultivaluedHashMap();
                      formData.add("ips", ips);
                      responses.add(
                          jerseyClient
                              .getClient()
                              .target(
                                  new URI(
                                      String.format(
                                          "http://%s:%d", url.getIpAddress(), url.getPort())))
                              .path("/api/clientManager/clientOpen")
                              .request()
                              .buildPost(Entity.form(formData))
                              .invoke(CommonResponse.class));
                    } catch (Exception e) {
                      LOGGER.error("send clientOpen other session error!url={}", url, e);
                      responses.add(new CommonResponse(false, e.getMessage()));
                    } finally {
                      latch.countDown();
                    }
                  }));
    }
    try {
      latch.await(3000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      return CommonResponse.buildFailedResponse("client open timeout");
    }
    Optional<CommonResponse> failedResponses =
        responses.stream().filter(r -> !r.isSuccess()).findFirst();
    return failedResponses.orElseGet(CommonResponse::buildSuccessResponse);
  }

  private List<URL> getOtherServersCurrentZone() {
    String localZone = sessionServerConfig.getSessionServerRegion();
    if (StringUtils.isNotBlank(localZone)) {
      localZone = localZone.toUpperCase();
    }
    List<URL> servers =
        metaNodeService.getSessionServerList(localZone).stream()
            .filter(server -> !server.equals(NetUtil.getLocalAddress().getHostAddress()))
            .map(server -> new URL(server, sessionServerConfig.getHttpServerPort()))
            .collect(Collectors.toList());
    return servers;
  }

  @GET
  @Path("/connectionMapper.json")
  public Map<String, String> connectionMapper() {
    return connectionMapper.get();
  }
}
