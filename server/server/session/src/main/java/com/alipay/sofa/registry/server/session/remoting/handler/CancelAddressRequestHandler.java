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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.sessionserver.CancelAddressRequest;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *
 * @author shangyu.wh
 * @version $Id: CancelHandler.java, v 0.1 2017-11-30 15:29 shangyu.wh Exp $
 */
public class CancelAddressRequestHandler extends AbstractServerHandler<CancelAddressRequest> {

    private static final Logger LOGGER          = LoggerFactory
                                                    .getLogger(CancelAddressRequestHandler.class);

    private static final Logger EXCHANGE_LOGGER = LoggerFactory.getLogger("SESSION-EXCHANGE",
                                                    "[CancelAddressRequestHandler]");

    @Autowired
    private Registry            sessionRegistry;

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return CancelAddressRequest.class;
    }

    @Override
    protected NodeType getConnectNodeType() {
        return NodeType.CLIENT;
    }

    @Override
    public Object reply(Channel channel, CancelAddressRequest cancelProcessRequest) {

        Result result = new Result();

        try {
            EXCHANGE_LOGGER.info("request={}", cancelProcessRequest);
            List<ConnectId> connectIds = cancelProcessRequest.getConnectIds();
            if (connectIds == null || connectIds.isEmpty()) {
                LOGGER.error("Request connectIds cannot be null or empty!");
                result.setMessage("Request connectIds cannot be null or empty!");
                result.setSuccess(false);
                return result;
            }
            sessionRegistry.cancel(connectIds);
        } catch (Exception e) {
            LOGGER.error("Cancel Address Request error!", e);
            throw new RuntimeException("Cancel Address Request error!", e);
        }

        result.setSuccess(true);

        return result;
    }

}