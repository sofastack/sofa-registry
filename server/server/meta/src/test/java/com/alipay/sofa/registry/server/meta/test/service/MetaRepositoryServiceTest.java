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

import com.alipay.sofa.registry.common.model.metaserver.MetaNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.repository.NodeRepository;
import com.alipay.sofa.registry.server.meta.repository.service.MetaRepositoryService;
import com.alipay.sofa.registry.server.meta.store.RenewDecorate;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version 1.0: MetaRepositoryServiceTest.java, v 0.1 2019-08-01 12:18 shangyu.wh Exp $
 */
public class MetaRepositoryServiceTest {

    @Test
    public void testMetaRepositoryServicePut() {
        Map<String/*dataCenter*/, NodeRepository> registry = new ConcurrentHashMap<>();
        MetaRepositoryService metaRepositoryService = new MetaRepositoryService(registry);

        String ip = "192.1.1.1";
        String dataCenter = "zue";

        metaRepositoryService.setNodeConfig(new NodeConfig() {
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

        RenewDecorate<MetaNode> metaNode = new RenewDecorate(new MetaNode(new URL(ip, 0),
            dataCenter));
        metaRepositoryService.put(ip, metaNode);

        Assert.assertEquals(metaRepositoryService.get(ip).getRenewal().getIp(), ip);
    }

    @Test
    public void testMetaRepositoryServiceRemove() {
        Map<String/*dataCenter*/, NodeRepository> registry = new ConcurrentHashMap<>();
        MetaRepositoryService metaRepositoryService = new MetaRepositoryService(registry);

        String ip = "192.1.1.1";
        String dataCenter = "zue";

        metaRepositoryService.setNodeConfig(new NodeConfig() {
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

        RenewDecorate<MetaNode> metaNode = new RenewDecorate(new MetaNode(new URL(ip, 0),
            dataCenter));

        metaRepositoryService.put(ip, metaNode);

        metaRepositoryService.remove(ip);

        Assert.assertEquals(metaRepositoryService.getAllData().size(), 0);
    }

    @Test
    public void testMetaRepositoryServiceReplace() {
        Map<String/*dataCenter*/, NodeRepository> registry = new ConcurrentHashMap<>();
        MetaRepositoryService metaRepositoryService = new MetaRepositoryService(registry);

        String ip = "192.1.1.1";
        String dataCenter = "zue";

        metaRepositoryService.setNodeConfig(new NodeConfig() {
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

        RenewDecorate<MetaNode> metaNode = new RenewDecorate(new MetaNode(new URL(ip, 0),
            dataCenter));

        MetaNode metaNodeFix = new MetaNode(new URL(ip, 0), dataCenter);

        RenewDecorate<MetaNode> metaNode2 = new RenewDecorate(metaNodeFix);

        metaRepositoryService.put(ip, metaNode);

        //        Assert.assertEquals(metaRepositoryService.get(ip).getRenewal().getNodeStatus(),
        //            NodeStatus.INIT);

        metaRepositoryService.replace(ip, metaNode2);

        //        Assert.assertEquals(metaRepositoryService.get(ip).getRenewal().getNodeStatus(),
        //            NodeStatus.WORKING);
        Assert.assertEquals(metaRepositoryService.getAllData().size(), 1);
    }

    @Test
    public void testMetaRepositoryServiceReplaceAll() {
        Map<String/*dataCenter*/, NodeRepository> registry = new ConcurrentHashMap<>();
        MetaRepositoryService metaRepositoryService = new MetaRepositoryService(registry);

        String ip = "192.1.1.1";
        String dataCenter = "zue";

        metaRepositoryService.setNodeConfig(new NodeConfig() {
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

        RenewDecorate<MetaNode> metaNode = new RenewDecorate(new MetaNode(new URL(ip, 0),
            dataCenter));

        MetaNode metaNodeFix = new MetaNode(new URL(ip, 0), dataCenter);

        RenewDecorate<MetaNode> metaNode2 = new RenewDecorate(metaNodeFix);

        metaRepositoryService.put(ip, metaNode);
        //
        //        Assert.assertEquals(metaRepositoryService.get(ip).getRenewal().getNodeStatus(),
        //            NodeStatus.INIT);

        Map<String, RenewDecorate<MetaNode>> map = new HashMap<>();
        map.put(ip, metaNode2);

        metaRepositoryService.replaceAll(dataCenter, map, 1234l);

        //        Assert.assertEquals(metaRepositoryService.get(ip).getRenewal().getNodeStatus(),
        //            NodeStatus.WORKING);
        Assert.assertEquals(metaRepositoryService.getAllDataMap().get(dataCenter).size(), 1);
    }
}