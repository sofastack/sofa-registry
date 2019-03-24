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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.dataserver.PublishDataRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.forward.ForwardService;
import com.alipay.sofa.registry.server.data.util.ThreadPoolExecutorDataServer;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * processor to publish data
 *
 * @author qian.lqlq
 * @version $Id: ForwardPublishDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class PublishDataHandler extends AbstractServerHandler<PublishDataRequest> {

    /** LOGGER */
    private static final Logger            LOGGER = LoggerFactory
                                                      .getLogger(PublishDataHandler.class);

    @Autowired
    private ForwardService                 forwardService;

    @Autowired
    private SessionServerConnectionFactory sessionServerConnectionFactory;

    @Autowired
    private DataChangeEventCenter          dataChangeEventCenter;

    @Autowired
    private DataServerConfig               dataServerConfig;

    private ThreadPoolExecutor             publishExecutor;

    public PublishDataHandler(DataServerConfig dataServerConfig) {

        publishExecutor = new ThreadPoolExecutorDataServer("PublishProcessorExecutor",
            dataServerConfig.getPublishExecutorMinPoolSize(),
            dataServerConfig.getPublishExecutorMaxPoolSize(),
            dataServerConfig.getNotifyDataSyncExecutorKeepAliveTime(), TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(dataServerConfig.getPublishExecutorQueueSize()),
            new NamedThreadFactory("DataServer-PublishProcessorExecutor-executor", true));
    }

    @Override
    public void checkParam(PublishDataRequest request) throws RuntimeException {
        Publisher publisher = request.getPublisher();
        ParaCheckUtil.checkNotNull(publisher, "PublishDataRequest.publisher");
        ParaCheckUtil.checkNotBlank(publisher.getDataId(), "publisher.dataId");
        ParaCheckUtil.checkNotBlank(publisher.getInstanceId(), "publisher.instanceId");
        ParaCheckUtil.checkNotBlank(publisher.getGroup(), "publisher.group");
        ParaCheckUtil.checkNotBlank(publisher.getDataInfoId(), "publisher.dataInfoId");
        ParaCheckUtil.checkNotNull(publisher.getVersion(), "publisher.version");
        ParaCheckUtil.checkNotBlank(publisher.getRegisterId(), "publisher.registerId");

        if (publisher.getPublishType() != PublishType.TEMPORARY) {
            ParaCheckUtil.checkNotNull(publisher.getSourceAddress(), "publisher.sourceAddress");
        }
    }

    @Override
    public Object doHandle(Channel channel, PublishDataRequest request) {
        Publisher publisher = Publisher.processPublisher(request.getPublisher());
        if (forwardService.needForward(publisher.getDataInfoId())) {
            LOGGER.warn("[forward] Publish request refused, request: {}", request);
            CommonResponse response = new CommonResponse();
            response.setSuccess(false);
            response.setMessage("Request refused, Server status is not working");
            return response;
        }

        dataChangeEventCenter.onChange(publisher, dataServerConfig.getLocalDataCenter());
        if (publisher.getPublishType() != PublishType.TEMPORARY) {
            sessionServerConnectionFactory.registerClient(request.getSessionServerProcessId(),
                publisher.getSourceAddress().getAddressString());
        }

        return CommonResponse.buildSuccessResponse();
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return new CommonResponse(false, msg);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return PublishDataRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }

    @Override
    public Executor getExecutor() {
        return publishExecutor;
    }
}
