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
package com.alipay.sofa.registry.server.meta.node.impl;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.GetChangeListRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.meta.node.MetaNodeService;
import com.alipay.sofa.registry.server.meta.remoting.MetaClientExchanger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaNodeServiceImpl.java, v 0.1 2018-02-12 14:13 shangyu.wh Exp $
 */
public class MetaNodeServiceImpl implements MetaNodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaNodeServiceImpl.class);

    @Autowired
    private MetaClientExchanger metaClientExchanger;

    @Override
    public DataCenterNodes getDataCenterNodes(GetChangeListRequest getChangeListRequest) {
        try {
            Request<GetChangeListRequest> changeListRequest = new Request<GetChangeListRequest>() {

                @Override
                public GetChangeListRequest getRequestBody() {
                    return getChangeListRequest;
                }

                @Override
                public URL getRequestUrl() {
                    return metaClientExchanger.getDataCenterUrl(getChangeListRequest
                        .getDataCenterId());
                }
            };

            Response response = metaClientExchanger.request(changeListRequest);
            Object result = response.getResult();
            if (result instanceof DataCenterNodes) {
                return (DataCenterNodes) result;
            } else {
                LOGGER.error("getDataCenterNodes has not get response or response type illegal!");
                throw new RuntimeException(
                    "getDataCenterNodes has not get response or response type illegal!");
            }

        } catch (RequestException e) {
            LOGGER.error("MetaNodeService get DataCenter Nodes error! " + e.getRequestMessage(), e);
            throw new RuntimeException("MetaNodeService get DataCenter Nodes error! "
                                       + e.getRequestMessage(), e);
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.META;
    }
}