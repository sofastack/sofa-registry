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
import com.alipay.sofa.registry.common.model.dataserver.SessionServerRegisterRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author qian.lqlq
 * @version $Id: SessionServerRegisterProcessor.java, v 0.1 2018年04月14日 17:32 qian.lqlq Exp $
 */
public class SessionServerRegisterHandler extends
                                         AbstractServerHandler<SessionServerRegisterRequest> {

    @Autowired
    private SessionServerConnectionFactory sessionServerConnectionFactory;

    @Override
    public void checkParam(SessionServerRegisterRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotNull(request.getProcessId(), "request.processId");
    }

    @Override
    public Object doHandle(Channel channel, SessionServerRegisterRequest request) {
        sessionServerConnectionFactory.registerSession(request.getProcessId(),
            ((BoltChannel) channel).getConnection());
        return CommonResponse.buildSuccessResponse();
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return CommonResponse.buildFailedResponse(msg);
    }

    @Override
    public Class interest() {
        return SessionServerRegisterRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.SESSION;
    }
}