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
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.dataserver.ClientOffRequest;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * processor to remove data of specific clients immediately
 *
 * @author qian.lqlq
 * @version $Id: ClientOffProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class ClientOffHandler extends AbstractDataHandler<ClientOffRequest> {

    @Autowired
    private DataServerConfig        dataServerConfig;

    @Autowired
    protected DataChangeEventCenter dataChangeEventCenter;

    @Override
    public void checkParam(ClientOffRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotEmpty(request.getConnectIds(), "ClientOffRequest.connectIds");
        ParaCheckUtil.checkNotNull(request.getSessionProcessId(), "request.sessionProcessId");
    }

    @Override
    public Object doHandle(Channel channel, ClientOffRequest request) {
        processSessionProcessId(channel, request.getSessionProcessId());

        List<ConnectId> connectIds = request.getConnectIds();
        for (ConnectId connectId : connectIds) {
            Map<String, DatumVersion> modifieds = localDatumStorage.remove(connectId,
                request.getSessionProcessId(), request.getGmtOccur());
            dataChangeEventCenter.onChange(modifieds.keySet(),
                dataServerConfig.getLocalDataCenter());
        }
        return CommonResponse.buildSuccessResponse();
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return CommonResponse.buildFailedResponse(msg);
    }

    @Override
    public Class interest() {
        return ClientOffRequest.class;
    }
}
