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
import com.alipay.sofa.registry.common.model.dataserver.HeartbeatRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;

/**
 *
 * @author kezhu.wukz
 * @version $Id: HeartbeatHandler.java, v 0.1 2019年11月24日 17:32 kezhu.wukz Exp $
 */
public class HeartbeatHandler extends AbstractServerHandler<HeartbeatRequest> {

    @Override
    public void checkParam(HeartbeatRequest request) throws RuntimeException {
    }

    @Override
    public Object doHandle(Channel channel, HeartbeatRequest request) {
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
        return HeartbeatRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}