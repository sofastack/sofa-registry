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
package com.alipay.sofa.registry.server.data.remoting.dataserver.handler;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.NotifyOnlineRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DataServerCache;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author qian.lqlq
 * @version $Id: NotifyOnlineProcessor.java, v 0.1 2018-04-29 14:36 qian.lqlq Exp $
 */
public class NotifyOnlineHandler extends AbstractServerHandler<NotifyOnlineRequest> {

    @Autowired
    private DataServerCache dataServerCache;

    @Override
    public void checkParam(NotifyOnlineRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getIp(), "ip");
    }

    @Override
    public Object doHandle(Channel channel, NotifyOnlineRequest request) {
        long version = request.getVersion();
        if (version >= dataServerCache.getCurVersion()) {
            dataServerCache.addNotWorkingServer(version, request.getIp());
        }
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
        return NotifyOnlineRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}