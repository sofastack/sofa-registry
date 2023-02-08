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

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: SessionOpenResource.java, v 0.1 2018-03-21 11:06 shangyu.wh Exp $
 */
@Path("api/servers")
public class SessionOpenResource {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private MetaServerService metaNodeService;

  @Autowired private SlotTableCache slotTableCache;

  @GET
  @Path("query.json")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getSessionServerListJson(@QueryParam("zone") String zone) {
    if (StringUtils.isBlank(zone)) {
      zone = sessionServerConfig.getSessionServerRegion();
    }

    if (StringUtils.isNotBlank(zone)) {
      zone = zone.toUpperCase();
    }
    return getSessionServers(zone);
  }

  @GET
  @Path("query")
  @Produces(MediaType.TEXT_PLAIN)
  public String getSessionServerList(@QueryParam("zone") String zone) {
    return Joiner.on(";").join(getSessionServerListJson(zone));
  }

  @GET
  @Path("alive")
  public String checkAlive() {
    return "OK";
  }

  /**
   * Get server list for current data center
   *
   * @return
   */
  @GET
  @Path("dataCenter")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getCurrentDataCenterServerList() {
    return getSessionServers(null);
  }

  private List<String> getSessionServers(String zone) {
    List<String> serverList = metaNodeService.getSessionServerList(zone);

    serverList =
        serverList.stream()
            .map(server -> server + ":" + sessionServerConfig.getServerPort())
            .collect(Collectors.toList());
    return serverList;
  }

  @GET
  @Path("slot")
  @Produces(MediaType.APPLICATION_JSON)
  public Slot getSlot(@QueryParam("dataInfoId") String dataInfoId) {
    ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
    return slotTableCache.getSlot(sessionServerConfig.getSessionServerDataCenter(), dataInfoId);
  }
}
