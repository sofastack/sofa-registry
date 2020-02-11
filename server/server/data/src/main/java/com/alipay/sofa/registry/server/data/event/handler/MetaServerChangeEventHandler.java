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
package com.alipay.sofa.registry.server.data.event.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alipay.remoting.Connection;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.event.DataServerChangeEvent;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.event.MetaServerChangeEvent;
import com.alipay.sofa.registry.server.data.event.StartTaskEvent;
import com.alipay.sofa.registry.server.data.event.StartTaskTypeEnum;
import com.alipay.sofa.registry.server.data.remoting.MetaNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.metaserver.IMetaServerService;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerConnectionFactory;
import com.alipay.sofa.registry.server.data.util.TimeUtil;
import com.google.common.collect.Lists;

/**
 *
 * @author qian.lqlq
 * @version $Id: MetaServerChangeEventHandler.java, v 0.1 2018-03-13 15:31 qian.lqlq Exp $
 */
public class MetaServerChangeEventHandler extends AbstractEventHandler<MetaServerChangeEvent> {

    private static final Logger         LOGGER    = LoggerFactory
                                                      .getLogger(MetaServerChangeEventHandler.class);
    private static final int            TRY_COUNT = 3;

    @Autowired
    private DataServerConfig            dataServerConfig;

    @Autowired
    private IMetaServerService          metaServerService;

    @Autowired
    private MetaNodeExchanger           metaNodeExchanger;

    @Autowired
    private EventCenter                 eventCenter;

    @Autowired
    private MetaServerConnectionFactory metaServerConnectionFactory;

    @Override
    public List<Class<? extends MetaServerChangeEvent>> interest() {
        return Lists.newArrayList(MetaServerChangeEvent.class);
    }

    @Override
    public void doHandle(MetaServerChangeEvent event) {
        Map<String, Set<String>> ipMap = event.getIpMap();
        for (Entry<String, Set<String>> ipEntry : ipMap.entrySet()) {
            String dataCenter = ipEntry.getKey();
            Set<String> ips = ipEntry.getValue();
            if (!CollectionUtils.isEmpty(ips)) {
                for (String ip : ips) {
                    Connection connection = metaServerConnectionFactory.getConnection(dataCenter,
                        ip);
                    if (connection == null || !connection.isFine()) {
                        registerMetaServer(dataCenter, ip);
                    }
                }
                Set<String> ipSet = metaServerConnectionFactory.getIps(dataCenter);
                for (String ip : ipSet) {
                    if (!ips.contains(ip)) {
                        metaServerConnectionFactory.remove(dataCenter, ip);
                        LOGGER
                            .info(
                                "[MetaServerChangeEventHandler] remove connection, datacenter:{}, ip:{}",
                                dataCenter, ip);
                    }
                }
            } else {
                //remove connections of dataCenter if the connectionMap of the dataCenter in ipMap is empty
                removeDataCenter(dataCenter);
            }
        }
        //remove connections of dataCenter if the dataCenter not exist in ipMap
        Set<String> dataCenters = metaServerConnectionFactory.getAllDataCenters();
        for (String dataCenter : dataCenters) {
            if (!ipMap.containsKey(dataCenter)) {
                removeDataCenter(dataCenter);
            }
        }
    }

    private void registerMetaServer(String dataCenter, String ip) {

        PeerId leader = metaServerService.getLeader();

        for (int tryCount = 0; tryCount < TRY_COUNT; tryCount++) {
            try {
                Channel channel = metaNodeExchanger.connect(new URL(ip, dataServerConfig
                    .getMetaServerPort()));
                //connect all meta server
                if (channel != null && channel.isConnected()) {
                    metaServerConnectionFactory.register(dataCenter, ip,
                        ((BoltChannel) channel).getConnection());
                }
                //register leader meta node
                if (ip.equals(leader.getIp())) {
                    Object obj = null;
                    try {
                        obj = metaNodeExchanger.request(new Request() {
                            @Override
                            public Object getRequestBody() {
                                return new DataNode(new URL(DataServerConfig.IP), dataServerConfig
                                    .getLocalDataCenter());
                            }

                            @Override
                            public URL getRequestUrl() {
                                return new URL(ip, dataServerConfig.getMetaServerPort());
                            }
                        }).getResult();
                    } catch (Exception e) {
                        PeerId newLeader = metaServerService.refreshLeader();
                        LOGGER
                            .error(

                                String
                                    .format(
                                        "[MetaServerChangeEventHandler] register data node send error!retry once leader :%s error",
                                        newLeader.getIp()), e);
                    }
                    if (obj instanceof NodeChangeResult) {
                        NodeChangeResult<DataNode> result = (NodeChangeResult<DataNode>) obj;
                        Map<String, Long> versionMap = result.getDataCenterListVersions();

                        //send renew after first register dataNode
                        Set<StartTaskTypeEnum> set = new HashSet<>();
                        set.add(StartTaskTypeEnum.RENEW);
                        eventCenter.post(new StartTaskEvent(set));

                        eventCenter.post(new DataServerChangeEvent(result.getNodes(), versionMap,
                            DataServerChangeEvent.FromType.REGISTER_META));
                        break;
                    }
                }

            } catch (Exception e) {
                LOGGER.error("[MetaServerChangeEventHandler] connect metaServer:{} error", ip, e);
                TimeUtil.randomDelay(1000);
            }
        }
    }

    private void removeDataCenter(String dataCenter) {
        metaServerConnectionFactory.remove(dataCenter);
        LOGGER.info("[MetaServerChangeEventHandler] remove connections of datacenter : {}",
            dataCenter);
    }
}