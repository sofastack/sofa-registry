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
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.node.SessionNodeManager;
import com.alipay.sofa.registry.server.session.provideData.processor.StopPushProvideDataProcessor;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author xiaojian.xj
 * @version $Id: SessionStopPushResource.java, v 0.1 2020年09月01日 20:54 xiaojian.xj Exp $
 */
@Path("stopPush")
public class SessionStopPushResource {

    private final static Logger          LOGGER = LoggerFactory
                                                    .getLogger(SessionStopPushResource.class);

    @Autowired
    private StopPushProvideDataProcessor stopPushProvideDataProcessor;

    @Autowired
    protected SessionServerConfig        sessionServerConfig;

    /**
     * api use to close or open all session's push switch when all meta were down
     * @param type true: stop push; false: open push
     * @return
     */
    @GET
    @Path("all/session/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public CommonResponse stopAllPush(@PathParam("type") String type) {

        if (!StringUtils.equals("true", type) || !StringUtils.equals("false", type)) {
            // param was wrong
            return CommonResponse.buildFailedResponse("type was wrong. it should be boolean.");
        }

        // process localhost stop push switch
        CommonResponse response = operatePushSwitch(type);
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();
        if (response.isSuccess()) {
            successList.add(NetUtil.getLocalAddress().getHostAddress());
        } else {
            failList.add(NetUtil.getLocalAddress().getHostAddress());
        }

        // operate other session push switch
        boolean operateSuccess = operateOtherSessionPushSwitch(type, successList, failList);

        response.setSuccess(response.isSuccess() && operateSuccess);
        response.setMessage(String.format("success:%s, fail:%s", successList, failList));
        return response;
    }

    /**
     * api use to close or open local push switch when all meta were down
     * @param type
     * @return
     */
    @GET
    @Path("session/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public CommonResponse stopPush(@PathParam("type") String type) {
        if (!StringUtils.equals("true", type) || !StringUtils.equals("false", type)) {
            // param was wrong
            return CommonResponse.buildFailedResponse("type was wrong. it should be boolean.");
        }

        return operatePushSwitch(type);
    }

    private CommonResponse operatePushSwitch(String type) {
        try {
            ProvideData provideData = new ProvideData(new ServerDataBox(Boolean.valueOf(type)),
                null, null);
            stopPushProvideDataProcessor.changeDataProcess(provideData);

            return CommonResponse.buildSuccessResponse();
        } catch (Throwable e) {
            LOGGER.error("operate push switch error!", e);
            return CommonResponse.buildFailedResponse("operate push switch fail");
        }

    }

    /**
     * operate other session push switch
     * @param type
     * @param successList
     * @param failList
     * @return
     */
    private boolean operateOtherSessionPushSwitch(String type, List<String> successList,
                                                  List<String> failList) {
        NodeManager nodeManager = NodeManagerFactory.getNodeManager(NodeType.SESSION);

        List<URL> serverUrls = new ArrayList<>();
        if (nodeManager instanceof SessionNodeManager) {
            SessionNodeManager sessionNodeManager = (SessionNodeManager) nodeManager;
            List<String> serverList = sessionNodeManager.getZoneServerList(sessionServerConfig.getSessionServerRegion());

            serverUrls = serverList.stream()
                    .filter(server -> !server.equals(NetUtil.getLocalAddress().getHostAddress()))
                    .map(server -> new URL(server, sessionServerConfig.getHttpServerPort()))
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(serverUrls)) {
            return true;
        }

        ExecutorService es = Executors.newFixedThreadPool(serverUrls.size());
        CompletionService cs = new ExecutorCompletionService(es);

        serverUrls.forEach(url -> cs.submit((Callable<CommonResponse>) () -> {
            try {
                JerseyClient jerseyClient = JerseyClient.getInstance();
                Channel channel = jerseyClient.connect(url);
                GenericType<CommonResponse> response = new GenericType<CommonResponse>() {
                };
                return channel.getWebTarget().property(ClientProperties.READ_TIMEOUT, 3000)
                        .path("stopPush/session/" + type).request()
                        .get(response);
            } catch (Throwable e) {
                LOGGER.error("operate other subscriber push switch error!url={}", url, e);
                return CommonResponse.buildFailedResponse(url.getHost());
            }
        }));

        boolean success = true;
        try {
            for (int i = 0; i < serverUrls.size(); i++) {
                Future<CommonResponse> future = cs.poll(3000, TimeUnit.MILLISECONDS);
                if(future == null || !future.get().isSuccess()){
                    success = false;
                    failList.add(future.get().getMessage());
                    continue;
                }
                successList.add(future.get().getMessage());

            }
        } catch (Throwable e) {
            success = false;
            LOGGER.error("operate other session push switch error!", e);
        } finally {
            es.shutdown();
        }
        return success;
    }
}