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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.server.data.cache.SlotManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.dataserver.PublishDataRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;

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
    private SessionServerConnectionFactory sessionServerConnectionFactory;

    @Autowired
    private DataChangeEventCenter          dataChangeEventCenter;

    @Autowired
    private DataServerConfig               dataServerConfig;

    @Autowired
    private ThreadPoolExecutor             publishProcessorExecutor;

    @Autowired
    private SlotManager                    slotManager;

    @Override
    public void checkParam(PublishDataRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotNull(request.getSessionProcessId(),
            "PublishDataRequest.sessionProcessId");

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
        Publisher publisher = Publisher.internPublisher(request.getPublisher());

        final SlotAccess slotAccess = slotManager.checkSlotAccess(publisher.getDataInfoId(),
            request.getSlotTableEpoch());
        if (slotAccess.isMoved()) {
            LOGGER.warn("[moved] Slot has moved, access: {}, request: {}", slotAccess, request);
            return SlotAccessGenericResponse.buildFailedResponse(slotAccess);
        }

        if (slotAccess.isMigrating()) {
            LOGGER.warn("[migrating] Slot is migrating, access: {}, request: {}", slotAccess,
                request);
            return SlotAccessGenericResponse.buildFailedResponse(slotAccess);
        }
        dataChangeEventCenter.onChange(publisher, dataServerConfig.getLocalDataCenter());

        if (publisher.getPublishType() != PublishType.TEMPORARY) {
            String connectId = WordCache.getInstance().getWordCache(
                publisher.getSourceAddress().getAddressString() + ValueConstants.CONNECT_ID_SPLIT
                        + publisher.getTargetAddress().getAddressString());
            sessionServerConnectionFactory.registerConnectId(request.getSessionProcessId()
                .toString(), connectId);
        }
        return SlotAccessGenericResponse.buildSuccessResponse(slotAccess);
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
        return publishProcessorExecutor;
    }
}
