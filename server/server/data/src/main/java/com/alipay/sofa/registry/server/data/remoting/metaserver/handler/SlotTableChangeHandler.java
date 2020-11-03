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
package com.alipay.sofa.registry.server.data.remoting.metaserver.handler;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractClientHandler;
import com.alipay.sofa.registry.server.data.cache.SlotManager;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-02 17:24 yuzhi.lyz Exp $
 */
public final class SlotTableChangeHandler extends AbstractClientHandler<SlotTableChangeRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlotTableChangeHandler.class);

    @Autowired
    private SlotManager         slotManager;

    @Override
    public void checkParam(SlotTableChangeRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotNull(request.getSlotTable(), "SlotTableChangeRequest.slotTable");
    }

    @Override
    public Object doHandle(Channel channel, SlotTableChangeRequest request) {

        return null;
    }

    @Override
    public Object buildFailedResponse(String msg) {
        return CommonResponse.buildFailedResponse(msg);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}
