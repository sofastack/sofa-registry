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

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.ClientOffRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.event.DatumSnapshotEvent;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.DisconnectEventHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.forward.ForwardService;

/**
 * handling snapshot request
 *
 * @author kezhu.wukz
 * @version $Id: ClientOffProcessor.java, v 0.1 2019-05-30 15:48 kezhu.wukz Exp $
 */
public class DatumSnapshotHandler extends AbstractServerHandler<DatumSnapshotRequest> {

    /** LOGGER */
    private static final Logger    LOGGER = LoggerFactory.getLogger(DatumSnapshotHandler.class);

    @Autowired
    private ForwardService         forwardService;

    @Autowired
    private DataServerConfig       dataServerBootstrapConfig;

    @Autowired
    private DisconnectEventHandler disconnectEventHandler;

    @Autowired
    private DataChangeEventCenter  dataChangeEventCenter;

    @Autowired
    private DataServerConfig       dataServerConfig;

    @Override
    public void checkParam(DatumSnapshotRequest request) throws RuntimeException {
        //        ParaCheckUtil.checkNotEmpty(request.getHosts(), "ClientOffRequest.hosts");
    }

    @Override
    public Object doHandle(Channel channel, DatumSnapshotRequest request) {
        if (forwardService.needForward()) {
            LOGGER.warn("[forward] Snapshot request refused, request: {}", request);
            CommonResponse response = new CommonResponse();
            response.setSuccess(false);
            response.setMessage("Request refused, Server status is not working");
            return response;
        }

        Map<String, Publisher> pubMap = request.getPublishers().stream().collect(
                Collectors.toMap(p -> p.getRegisterId(), p -> p));
        dataChangeEventCenter.onChange(new DatumSnapshotEvent(request.getConnectId(), dataServerConfig
                .getLocalDataCenter(), pubMap));

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
        return ClientOffRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}
