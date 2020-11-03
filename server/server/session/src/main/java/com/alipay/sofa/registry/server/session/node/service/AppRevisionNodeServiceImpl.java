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

import com.alipay.sofa.registry.common.model.metaserver.CheckRevisionsRequest;
import com.alipay.sofa.registry.common.model.metaserver.FetchRevisionsRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.AppRevisionKey;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.RaftClientManager;
import com.alipay.sofa.registry.server.session.node.SessionNodeManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AppRevisionNodeServiceImpl implements AppRevisionNodeService {
    private static final Logger   LOGGER = LoggerFactory.getLogger(SessionNodeManager.class,
                                             "[AppRevisionService]");

    @Autowired
    protected SessionServerConfig sessionServerConfig;

    @Autowired
    protected NodeExchanger       metaNodeExchanger;

    @Autowired
    RaftClientManager             raftClientManager;

    public void register(AppRevisionRegister appRevision) {
        Request<AppRevisionRegister> request = new Request<AppRevisionRegister>() {
            @Override
            public AppRevisionRegister getRequestBody() {
                return appRevision;
            }

            @Override
            public URL getRequestUrl() {
                return new URL(raftClientManager.getLeader().getIp(),
                    sessionServerConfig.getMetaServerPort());
            }
        };
        try {
            Response response = metaNodeExchanger.request(request);
        } catch (RequestException e) {
            LOGGER.error("add app revision error! " + e.getMessage(), e);
            throw new RuntimeException("add app revision error! " + e.getMessage(), e);
        }
    }

    public List<AppRevisionKey> checkRevisions(String keysDigest) {
        Request<CheckRevisionsRequest> request = new Request<CheckRevisionsRequest>() {
            @Override
            public CheckRevisionsRequest getRequestBody() {
                return new CheckRevisionsRequest(keysDigest);
            }

            @Override
            public URL getRequestUrl() {
                return new URL(raftClientManager.getLeader().getIp(),
                    sessionServerConfig.getMetaServerPort());
            }
        };
        try {
            Response response = metaNodeExchanger.request(request);
            return (List<AppRevisionKey>) response.getResult();
        } catch (RequestException e) {
            LOGGER.error("check app revisions error! " + e.getMessage(), e);
            throw new RuntimeException("check app revisions error! " + e.getMessage(), e);
        }

    }

    public List<AppRevisionRegister> fetchMulti(List<AppRevisionKey> keys) {
        Request<FetchRevisionsRequest> request = new Request<FetchRevisionsRequest>() {
            @Override
            public FetchRevisionsRequest getRequestBody() {
                return new FetchRevisionsRequest(keys);
            }

            @Override
            public URL getRequestUrl() {
                return new URL(raftClientManager.getLeader().getIp(),
                    sessionServerConfig.getMetaServerPort());
            }
        };
        try {
            Response response = metaNodeExchanger.request(request);
            Object result = response.getResult();
            return (List<AppRevisionRegister>) result;
        } catch (RequestException e) {
            LOGGER.error("fetch app revision error! " + e.getMessage(), e);
            throw new RuntimeException("fetch app revision error! " + e.getMessage(), e);
        }
    }
}
