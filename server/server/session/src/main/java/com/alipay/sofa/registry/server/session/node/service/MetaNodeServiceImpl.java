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
package com.alipay.sofa.registry.server.session.node.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.metaserver.FetchProvideDataRequest;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.RaftClientManager;
import com.alipay.sofa.registry.server.session.node.SessionNodeManager;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaNodeServiceImpl.java, v 0.1 2018-04-17 21:23 shangyu.wh Exp $
 */
public class MetaNodeServiceImpl implements MetaNodeService {

    private static final Logger   LOGGER = LoggerFactory.getLogger(SessionNodeManager.class,
                                             "[MetaNodeService]");

    @Autowired
    protected SessionServerConfig sessionServerConfig;

    @Autowired
    protected NodeExchanger       metaNodeExchanger;

    @Autowired
    RaftClientManager             raftClientManager;

    @Override
    public ProvideData fetchData(String dataInfoId) {
        try {

            Request<FetchProvideDataRequest> request = new Request<FetchProvideDataRequest>() {

                @Override
                public FetchProvideDataRequest getRequestBody() {

                    return new FetchProvideDataRequest(dataInfoId);
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(raftClientManager.getLeader().getIp(),
                        sessionServerConfig.getMetaServerPort());
                }
            };

            Response response = metaNodeExchanger.request(request);

            Object result = response.getResult();
            if (result instanceof ProvideData) {
                return (ProvideData) result;
            } else {
                LOGGER.error("fetch null provider data!");
                throw new RuntimeException("MetaNodeService fetch null provider data!");
            }
        } catch (RequestException e) {
            LOGGER.error("fetch provider data error! " + e.getMessage(), e);
            throw new RuntimeException("fetch provider data error! " + e.getMessage(), e);
        }
    }
}