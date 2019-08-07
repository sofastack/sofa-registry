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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.GetChangeListRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handle other datacenter's meta node's request for get the change node list
 * @author shangyu.wh
 * @version $Id: GetChangeListRequestHandler.java, v 0.1 2018-02-12 16:14 shangyu.wh Exp $
 */
public class GetChangeListRequestHandler extends AbstractServerHandler<GetChangeListRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger("META-CONNECT");

    @Autowired
    private Registry            metaServerRegistry;

    @Override
    public Object reply(Channel channel, GetChangeListRequest getChangeListRequest) {
        DataCenterNodes dataCenterNodes;
        try {
            dataCenterNodes = metaServerRegistry.getDataCenterNodes(getChangeListRequest
                .getNodeType());
            LOGGER.info("Get change Node list {} success!from {}", dataCenterNodes,
                channel.getRemoteAddress());
        } catch (Exception e) {
            LOGGER.error("Get change Node list error!", e);
            throw new RuntimeException("Get change Node list error!", e);
        }
        return dataCenterNodes;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return GetChangeListRequest.class;
    }
}