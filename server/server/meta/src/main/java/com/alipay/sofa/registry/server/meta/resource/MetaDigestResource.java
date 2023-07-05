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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.ReporterUtils;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultMetaServerManager;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: MetaDigestResource.java, v 0.1 2018-06-25 21:13 shangyu.wh Exp $
 */
@Path("digest")
@LeaderAwareRestController
public class MetaDigestResource {

  private static final Logger TASK_LOGGER =
      LoggerFactory.getLogger(MetaDigestResource.class, "[Resource]");

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(MetaDigestResource.class, "[DBService]");

  @Autowired private DefaultMetaServerManager metaServerManager;

  @Autowired private ProvideDataService provideDataService;

  @PostConstruct
  public void init() {
    MetricRegistry metrics = new MetricRegistry();
    metrics.register(
        "metaNodeList", (Gauge<Map>) () -> getRegisterNodeByType(NodeType.META.name()));
    metrics.register(
        "dataNodeList", (Gauge<Map>) () -> getRegisterNodeByType(NodeType.DATA.name()));
    metrics.register(
        "sessionNodeList", (Gauge<Map>) () -> getRegisterNodeByType(NodeType.SESSION.name()));
    metrics.register("pushSwitch", (Gauge<Map>) () -> getPushSwitch());
    ReporterUtils.startSlf4jReporter(60, metrics);
  }

  @GET
  @Path("{type}/node/query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map getRegisterNodeByType(@PathParam("type") String type) {
    try {
      return metaServerManager.getSummary(NodeType.valueOf(type.toUpperCase())).getNodes();
    } catch (Exception e) {
      TASK_LOGGER.error("Fail get Register Node By Type:{}", type, e);
      throw new RuntimeException("Fail get Register Node By Type:" + type, e);
    }
  }

  @GET
  @Path("pushSwitch")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> getPushSwitch() {
    Map<String, String> resultMap = new HashMap<>(1);
    resultMap.putAll(globalPushSwitch());
    resultMap.putAll(grayPushSwitch());
    return resultMap;
  }

  public Map<String, String> globalPushSwitch() {
    DBResponse<PersistenceData> ret =
        provideDataService.queryProvideData(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);

    Map<String, String> resultMap = new HashMap<>(1);
    if (ret.getOperationStatus() == OperationStatus.SUCCESS) {
      String result = getEntityData(ret);
      if (result != null && !result.isEmpty()) {
        resultMap.put("stopPush", result);
      } else {
        resultMap.put("stopPush", result);
        resultMap.put("msg", "data is empty");
      }
    } else if (ret.getOperationStatus() == OperationStatus.NOTFOUND) {
      resultMap.put("msg", "(global push switch)OperationStatus is NOTFOUND");
    }
    DB_LOGGER.info("[getPushSwitch] {}", resultMap);
    return resultMap;
  }

  public Map<String, String> grayPushSwitch() {
    DBResponse<PersistenceData> ret =
        provideDataService.queryProvideData(ValueConstants.PUSH_SWITCH_GRAY_OPEN_DATA_ID);

    Map<String, String> resultMap = new HashMap<>(1);
    if (ret.getOperationStatus() == OperationStatus.SUCCESS) {
      String result = getEntityData(ret);
      if (result != null && !result.isEmpty()) {
        resultMap.put("grayPushSwitch", result);
      } else {
        resultMap.put("grayPushSwitch", result);
        resultMap.put("gray push switch msg", "data is empty");
      }
    } else if (ret.getOperationStatus() == OperationStatus.NOTFOUND) {
      resultMap.put("gray push switch msg", "OperationStatus is NOTFOUND");
    }
    DB_LOGGER.info("[getGrayPushSwitch] {}", resultMap);
    return resultMap;
  }

  private static String getEntityData(DBResponse<PersistenceData> resp) {
    if (resp != null && resp.getEntity() != null) {
      return resp.getEntity().getData();
    }
    return null;
  }

  @VisibleForTesting
  protected MetaDigestResource setMetaServerManager(DefaultMetaServerManager metaServerManager) {
    this.metaServerManager = metaServerManager;
    return this;
  }

  @VisibleForTesting
  protected MetaDigestResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }
}
