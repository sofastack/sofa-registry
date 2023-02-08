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

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.Map.Entry;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author shangyu.wh
 * @version $Id: DataDigestResource.java, v 0.1 2018-10-19 16:16 shangyu.wh Exp $
 */
@Path("digest")
public class DataDigestResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataDigestResource.class);

  private static final String SESSION = "SESSION";

  private static final String META = "META";

  @Autowired Exchange boltExchange;

  @Autowired MetaServerServiceImpl metaServerService;

  @Autowired DataServerConfig dataServerConfig;

  @Autowired DatumStorageDelegate datumStorageDelegate;

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
    if (StringUtils.isBlank(instanceId)) {
      instanceId = ValueConstants.DEFAULT_INSTANCE_ID;
    }
    if (StringUtils.isBlank(dataCenter)) {
      dataCenter = dataServerConfig.getLocalDataCenter();
    }
    String dataInfoId = DataInfo.toDataInfoId(dataId, instanceId, group);
    Map<String, Datum> retList = new HashMap<>();
    retList.put(dataCenter, datumStorageDelegate.get(dataCenter, dataInfoId));
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
            Map<String, Publisher> publisherMap = datumStorageDelegate.getByConnectId(connectId);
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

      Map<String, Map<String, Datum>> allMap = datumStorageDelegate.getLocalAll();
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
    if (StringUtils.isNotBlank(type)) {
      String inputType = type.trim().toUpperCase();

      switch (inputType) {
        case SESSION:
          List<String> sessionList = getSessionServerList();
          map.put(dataServerConfig.getLocalDataCenter(), sessionList);
          break;
        case META:
          List<String> metaList = getMetaServerLeader();
          map.put(dataServerConfig.getLocalDataCenter(), metaList);
          break;
        default:
          throw new IllegalArgumentException("unsupported server type:" + type);
      }
    }

    return map;
  }

  List<String> getSessionServerList() {
    Server server = boltExchange.getServer(dataServerConfig.getPort());
    if (server == null) {
      return Collections.emptyList();
    }
    Map<String, Channel> channels = server.selectAvailableChannelsForHostAddress();
    List<String> ret = Lists.newArrayListWithCapacity(channels.size());
    for (Channel channel : channels.values()) {
      BoltChannel boltChannel = (BoltChannel) channel;
      Connection conn = boltChannel.getConnection();
      ret.add(conn.getRemoteIP() + ":" + conn.getRemotePort());
    }
    return ret;
  }

  List<String> getMetaServerLeader() {
    String leader = metaServerService.getMetaServerLeader();
    if (StringUtils.isNotBlank(leader)) {
      return Lists.newArrayList(leader);
    } else {
      return Collections.emptyList();
    }
  }

  @GET
  @Path("datum/getDataInfoIdList")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<String> getDataInfoIdList() {
    try {
      Map<String, Map<String, Datum>> allMap = datumStorageDelegate.getLocalAll();
      if (CollectionUtils.isEmpty(allMap)) {
        return Collections.emptyList();
      }
      List<String> dataInfos = Lists.newArrayListWithCapacity(1024);
      StringBuilder builder = new StringBuilder(128);
      for (Entry<String, Map<String, Datum>> dataCenterEntry : allMap.entrySet()) {
        String dataCenter = dataCenterEntry.getKey();
        Map<String, Datum> datumMap = dataCenterEntry.getValue();
        for (Datum datum : datumMap.values()) {
          builder
              .append(dataCenter)
              .append(",")
              .append(datum.getDataInfoId())
              .append(",")
              .append(datum.publisherSize());

          dataInfos.add(builder.toString());
          builder.setLength(0);
        }
      }
      return dataInfos;
    } catch (Throwable e) {
      LOGGER.error("failed to get dataInfoList", e);
      throw new RuntimeException(e);
    }
  }
}
