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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.event.DatumSnapshotEvent;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.forward.ForwardService;
import com.alipay.sofa.registry.server.data.renew.DatumLeaseManager;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * handling snapshot request
 *
 * @author kezhu.wukz
 * @version $Id: ClientOffProcessor.java, v 0.1 2019-05-30 15:48 kezhu.wukz Exp $
 */
public class DatumSnapshotHandler extends AbstractServerHandler<DatumSnapshotRequest> {

    /** LOGGER */
    private static final Logger   LOGGER       = LoggerFactory
                                                   .getLogger(DatumSnapshotHandler.class);

    private static final Logger   RENEW_LOGGER = LoggerFactory.getLogger(
                                                   ValueConstants.LOGGER_NAME_RENEW,
                                                   "[DatumSnapshotHandler]");

    @Autowired
    private ForwardService        forwardService;

    @Autowired
    private DataChangeEventCenter dataChangeEventCenter;

    @Autowired
    private DataServerConfig      dataServerConfig;

    @Autowired
    private DatumLeaseManager     datumLeaseManager;

    @Autowired
    private DatumCache            datumCache;

    @Override
    public void checkParam(DatumSnapshotRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getConnectId(), "DatumSnapshotRequest.connectId");
        ParaCheckUtil.checkNotEmpty(request.getPublishers(), "DatumSnapshotRequest.publishers");
    }

    @Override
    public Object doHandle(Channel channel, DatumSnapshotRequest request) {
        RENEW_LOGGER.info("Received datumSnapshotRequest: {}", request);

        if (forwardService.needForward()) {
            LOGGER.warn("[forward] Snapshot request refused, request: {}", request);
            CommonResponse response = new CommonResponse();
            response.setSuccess(false);
            response.setMessage("Snapshot request refused, Server status is not working");
            return response;
        }

        Map<String, Publisher> pubMap = request.getPublishers().stream()
                .collect(Collectors.toMap(p -> p.getRegisterId(), p -> p));

        // diff the cache and snapshot
        Map<String, Publisher> cachePubMap = datumCache.getOwnByConnectId(request.getConnectId());
        if (cachePubMap == null) {
            RENEW_LOGGER.info(">>>>>>> connectId={}, cachePubMap.size=0, pubMap.size={}, the diff is: pubMap={}",
                    request.getConnectId(), pubMap.size(), pubMap);
        } else {
            Collection disjunction = CollectionUtils.disjunction(pubMap.values(), cachePubMap.values());
            RENEW_LOGGER.info(">>>>>>> connectId={}, cachePubMap.size={}, pubMap.size={}, the diff is: disjunction={}",
                    request.getConnectId(), cachePubMap.size(), pubMap.size(), disjunction);
        }

        dataChangeEventCenter.onChange(
                new DatumSnapshotEvent(request.getConnectId(), dataServerConfig.getLocalDataCenter(), pubMap));

        // record the renew timestamp
        datumLeaseManager.renew(request.getConnectId());

        return CommonResponse.buildSuccessResponse();
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return CommonResponse.buildFailedResponse(msg);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return DatumSnapshotRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}
