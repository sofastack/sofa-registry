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
package com.alipay.sofa.registry.server.meta.test.service;

import com.alipay.sofa.registry.common.model.Node.NodeStatus;
import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.repository.service.SessionRepositoryService;
import com.alipay.sofa.registry.server.meta.store.RenewDecorate;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version 1.0: SessionRepositoryServiceTest.java, v 0.1 2019-08-01 13:04 shangyu.wh Exp $
 */
public class SessionRepositoryServiceTest {

    @Test
    public void testSessionRepositoryServicePut() {
        ConcurrentHashMap<String/*ipAddress*/, RenewDecorate<SessionNode>> registry = new ConcurrentHashMap<>();
        SessionRepositoryService sessionRepositoryService = new SessionRepositoryService(registry);

        String ip = "192.1.1.1";
        String dataCenter = "zue";

        sessionRepositoryService.setNodeConfig(new NodeConfig() {
            @Override
            public Map<String, Collection<String>> getMetaNode() {
                return null;
            }

            @Override
            public Map<String, Collection<String>> getMetaNodeIP() {
                return null;
            }

            @Override
            public String getLocalDataCenter() {
                return dataCenter;
            }

            @Override
            public String getMetaDataCenter(String metaIpAddress) {
                return dataCenter;
            }

            @Override
            public Set<String> getDataCenterMetaServers(String dataCenter) {
                return null;
            }
        });

        RenewDecorate<SessionNode> sessionNode = new RenewDecorate(new SessionNode(new URL(ip, 0),
            dataCenter));
        sessionRepositoryService.put(ip, sessionNode);

        Assert.assertEquals(sessionRepositoryService.get(ip).getRenewal().getIp(), ip);
    }

    @Test
    public void testSessionRepositoryServiceRemove() {
        ConcurrentHashMap<String/*ipAddress*/, RenewDecorate<SessionNode>> registry = new ConcurrentHashMap<>();
        SessionRepositoryService sessionRepositoryService = new SessionRepositoryService(registry);

        String ip = "192.1.1.1";
        String dataCenter = "zue";

        sessionRepositoryService.setNodeConfig(new NodeConfig() {
            @Override
            public Map<String, Collection<String>> getMetaNode() {
                return null;
            }

            @Override
            public Map<String, Collection<String>> getMetaNodeIP() {
                return null;
            }

            @Override
            public String getLocalDataCenter() {
                return dataCenter;
            }

            @Override
            public String getMetaDataCenter(String metaIpAddress) {
                return dataCenter;
            }

            @Override
            public Set<String> getDataCenterMetaServers(String dataCenter) {
                return null;
            }
        });

        RenewDecorate<SessionNode> sessionNode = new RenewDecorate(new SessionNode(new URL(ip, 0),
            dataCenter));
        sessionRepositoryService.put(ip, sessionNode);

        sessionRepositoryService.remove(ip);

        Assert.assertEquals(sessionRepositoryService.getAllData().size(), 0);
    }

    @Test
    public void testSessionRepositoryServiceReplace() {
        ConcurrentHashMap<String/*ipAddress*/, RenewDecorate<SessionNode>> registry = new ConcurrentHashMap<>();
        SessionRepositoryService sessionRepositoryService = new SessionRepositoryService(registry);

        String ip = "192.1.1.1";
        String dataCenter = "zue";

        sessionRepositoryService.setNodeConfig(new NodeConfig() {
            @Override
            public Map<String, Collection<String>> getMetaNode() {
                return null;
            }

            @Override
            public Map<String, Collection<String>> getMetaNodeIP() {
                return null;
            }

            @Override
            public String getLocalDataCenter() {
                return dataCenter;
            }

            @Override
            public String getMetaDataCenter(String metaIpAddress) {
                return dataCenter;
            }

            @Override
            public Set<String> getDataCenterMetaServers(String dataCenter) {
                return null;
            }
        });

        RenewDecorate<SessionNode> sessionNod = new RenewDecorate(new SessionNode(new URL(ip, 0),
            dataCenter));

        SessionNode metaNodeFix = new SessionNode(new URL(ip, 0), dataCenter);
        metaNodeFix.setNodeStatus(NodeStatus.WORKING);

        RenewDecorate<SessionNode> metaNode2 = new RenewDecorate(metaNodeFix);

        sessionRepositoryService.put(ip, sessionNod);

        Assert.assertEquals(sessionRepositoryService.get(ip).getRenewal().getNodeStatus(),
            NodeStatus.INIT);

        sessionRepositoryService.replace(ip, metaNode2);

        Assert.assertEquals(sessionRepositoryService.get(ip).getRenewal().getNodeStatus(),
            NodeStatus.WORKING);
        Assert.assertEquals(sessionRepositoryService.getAllData().size(), 1);
        Assert.assertEquals(sessionRepositoryService.getAllDataMap().get(dataCenter).size(), 1);
    }

}