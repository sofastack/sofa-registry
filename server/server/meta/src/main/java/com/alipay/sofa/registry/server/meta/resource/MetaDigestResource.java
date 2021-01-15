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
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.jraft.annotation.RaftReference;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaDigestResource.java, v 0.1 2018-06-25 21:13 shangyu.wh Exp $
 */
@Path("digest")
public class MetaDigestResource {

    private static final Logger      TASK_LOGGER = LoggerFactory.getLogger(
                                                     MetaDigestResource.class, "[Resource]");

    private static final Logger      DB_LOGGER   = LoggerFactory.getLogger(
                                                     MetaDigestResource.class, "[DBService]");

    @Autowired
    private DefaultMetaServerManager metaServerManager;

    @RaftReference
    private DBService                persistenceDataDBService;

    @PostConstruct
    public void init() {
        MetricRegistry metrics = new MetricRegistry();
        metrics.register("metaNodeList", (Gauge<Map>) () -> getRegisterNodeByType(NodeType.META.name()));
        metrics.register("dataNodeList", (Gauge<Map>) () -> getRegisterNodeByType(NodeType.DATA.name()));
        metrics.register("sessionNodeList", (Gauge<Map>) () -> getRegisterNodeByType(NodeType.SESSION.name()));
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

    /**
     * return true mean push switch on
     */
    @GET
    @Path("pushSwitch")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getPushSwitch() {
        Map<String, Object> resultMap = new HashMap<>(1);
        try {
            DBResponse ret = persistenceDataDBService
                .get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);

            if (ret == null) {
                //default push switch on
                resultMap.put("pushSwitch", "open");
                resultMap.put("msg", "get null Data from db!");
            } else {
                if (ret.getOperationStatus() == OperationStatus.SUCCESS) {
                    PersistenceData data = (PersistenceData) ret.getEntity();
                    String result = data.getData();
                    if (result != null && !result.isEmpty()) {
                        resultMap.put("pushSwitch", "false".equalsIgnoreCase(result) ? "open"
                            : "closed");
                    } else {
                        resultMap.put("pushSwitch", "open");
                        resultMap.put("msg", "data is empty");
                    }
                } else if (ret.getOperationStatus() == OperationStatus.NOTFOUND) {
                    resultMap.put("pushSwitch", "open");
                    resultMap.put("msg", "OperationStatus is NOTFOUND");
                } else {
                    DB_LOGGER.error("get Data DB status error!");
                    throw new RuntimeException("Get Data DB status error!");
                }
            }
            DB_LOGGER.info("getPushSwitch: {}", resultMap);
        } catch (Exception e) {
            DB_LOGGER.error("get persistence Data dataInfoId {} from db error!",
                ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, e);
            throw new RuntimeException("Get persistence Data from db error!", e);
        }

        return resultMap;
    }
}