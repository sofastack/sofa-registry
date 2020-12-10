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
package com.alipay.sofa.registry.server.session.provideData.processor;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistConstants;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistManager;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 *
 * @author shangyu.wh
 * @version 1.0: BlackListProvideDataProcessor.java, v 0.1 2019-10-09 20:21 shangyu.wh Exp $
 */
public class BlackListProvideDataProcessor implements ProvideDataProcessor {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(BlackListProvideDataProcessor.class);

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private Registry            sessionRegistry;

    @Autowired
    private Exchange            boltExchange;

    @Autowired
    private BlacklistManager    blacklistManager;

    @Override
    public void changeDataProcess(ProvideData provideData) {
        //black list data
        if (provideData.getProvideData() == null
            || provideData.getProvideData().getObject() == null) {
            LOGGER.info("Fetch session blacklist no data existed,current config not change!");
            return;
        }
        String data = (String) provideData.getProvideData().getObject();
        if (data != null) {
            Map<String, Map<String, Set<String>>> blacklistConfigMap = blacklistManager
                .convertBlacklistConfig(data);
            clientOffBlackIp(blacklistConfigMap);
            LOGGER.info("Fetch session blacklist data switch {} success!", data);
        } else {
            LOGGER.info("Fetch session blacklist data null,current config not change!");
        }
        return;
    }

    private void clientOffBlackIp(Map<String, Map<String, Set<String>>> blacklistConfigMap) {

        if (blacklistConfigMap != null) {
            Set<String> ipSet = new HashSet();

            for (Map.Entry<String, Map<String, Set<String>>> configEntry : blacklistConfigMap
                .entrySet()) {
                if (BlacklistConstants.FORBIDDEN_PUB.equals(configEntry.getKey())
                    || BlacklistConstants.FORBIDDEN_SUB_BY_PREFIX.equals(configEntry.getKey())) {
                    Map<String, Set<String>> typeMap = configEntry.getValue();
                    if (typeMap != null) {
                        for (Map.Entry<String, Set<String>> typeEntry : typeMap.entrySet()) {
                            if (BlacklistConstants.IP_FULL.equals(typeEntry.getKey())) {
                                if (typeEntry.getValue() != null) {
                                    ipSet.addAll(typeEntry.getValue());
                                }
                            }
                        }
                    }
                }

            }

            sessionRegistry.remove(getIpConnects(ipSet));
        }
    }

    public List<ConnectId> getIpConnects(Set<String> _ipList) {

        Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

        List<ConnectId> connections = new ArrayList<>();

        if (sessionServer != null) {
            Collection<Channel> channels = sessionServer.getChannels();
            for (Channel channel : channels) {
                String ip = channel.getRemoteAddress().getAddress().getHostAddress();
                if (_ipList.contains(ip)) {
                    connections.add(ConnectId.of(channel.getRemoteAddress(),
                        channel.getLocalAddress()));
                }
            }
        }

        return connections;
    }

    @Override
    public void fetchDataProcess(ProvideData provideData) {

    }

    @Override
    public boolean support(ProvideData provideData) {
        return ValueConstants.BLACK_LIST_DATA_ID.equals(provideData.getDataInfoId());
    }
}