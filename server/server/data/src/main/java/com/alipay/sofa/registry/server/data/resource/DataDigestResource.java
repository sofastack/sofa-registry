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
package com.alipay.sofa.registry.server.data.resource;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: DataDigestResource.java, v 0.1 2018-10-19 16:16 shangyu.wh Exp $
 */
@Path("digest")
public class DataDigestResource {

  private static final String SESSION = "SESSION";

  private static final String META = "META";

  @Autowired private SessionServerConnectionFactory sessionServerConnectionFactory;

  @Autowired private MetaServerServiceImpl metaServerService;

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private DatumCache datumCache;

  @GET
  @Path("datum/query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Datum> getDatumByDataInfoId(
      @QueryParam("dataId") String dataId,
      @QueryParam("group") String group,
      @QueryParam("instanceId") String instanceId,
      @QueryParam("dataCenter") String dataCenter) {

    ParaCheckUtil.checkNotBlank(dataId, "dataId");
    ParaCheckUtil.checkNotBlank(group, "group");
    ParaCheckUtil.checkNotBlank(instanceId, "instanceId");
    ParaCheckUtil.checkNotBlank(dataCenter, "dataCenter");
    String dataInfoId = DataInfo.toDataInfoId(dataId, instanceId, group);
    Map<String, Datum> retList = new HashMap<>();
    retList.put(dataCenter, datumCache.get(dataCenter, dataInfoId));
    return retList;
  }

  @POST
  @Path("connect/query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Map<String, Publisher>> getPublishersByConnectId(Map<String, String> map) {
    Map<String, Map<String, Publisher>> ret = new HashMap<>();
    if (map != null && !map.isEmpty()) {
      map.forEach(
          (clientConnectId, sessionConnectId) -> {
            ConnectId connectId = ConnectId.of(clientConnectId, sessionConnectId);
            Map<String, Publisher> publisherMap = datumCache.getByConnectId(connectId);
            if (publisherMap != null && !publisherMap.isEmpty()) {
              ret.put(connectId.toString(), publisherMap);
            }
          });
    }
    return ret;
  }

  @GET
  @Path("datum/count")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDatumCount() {
    return getLocalDatumCount();
  }

  protected String getLocalDatumCount() {
    StringBuilder sb = new StringBuilder("CacheDigest");
    try {

      Map<String, Map<String, Datum>> allMap = datumCache.getAll();
      if (!allMap.isEmpty()) {
        for (Entry<String, Map<String, Datum>> dataCenterEntry : allMap.entrySet()) {
          String dataCenter = dataCenterEntry.getKey();
          Map<String, Datum> datumMap = dataCenterEntry.getValue();
          sb.append(
              String.format(" [Datum] size of datum in %s is %s", dataCenter, datumMap.size()));
          int pubCount = datumMap.values().stream().mapToInt(Datum::publisherSize).sum();
          sb.append(
              String.format(",[Publisher] size of publisher in %s is %s", dataCenter, pubCount));
        }
      } else {
        sb.append(" datum cache is empty");
      }

    } catch (Throwable t) {
      sb.append(" cache digest error!");
    }

    return sb.toString();
  }

  @GET
  @Path("{type}/serverList/query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, List<String>> getServerListAll(@PathParam("type") String type) {
    Map<String, List<String>> map = new HashMap<>();
    if (type != null && !type.isEmpty()) {
      String inputType = type.toUpperCase();

      switch (inputType) {
        case SESSION:
          List<String> sessionList = getSessionServerList();
          if (sessionList != null) {
            map.put(dataServerConfig.getLocalDataCenter(), sessionList);
          }
          break;
        case META:
          List<String> metaList = getMetaServerList();
          if (metaList != null) {
            map.put(dataServerConfig.getLocalDataCenter(), metaList);
          }
          break;
        default:
          throw new IllegalArgumentException("unsupported server type:" + type);
      }
    }

    return map;
  }

  public List<String> getSessionServerList() {
    List<String> connections =
        sessionServerConnectionFactory.getSessionConnections().stream()
            .filter(connection -> connection != null && connection.isFine())
            .map(connection -> connection.getRemoteIP() + ":" + connection.getRemotePort())
            .collect(Collectors.toList());
    return connections;
  }

  public List<String> getMetaServerList() {
    return Lists.newArrayList(metaServerService.getMetaServerList());
  }
}
