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
package com.alipay.sofa.registry.server.session.correction.service;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublisherDigest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version $Id: PublisherDigestServiceImpl.java, v 0.1 2019-05-30 21:17 shangyu.wh Exp $
 */
public class PublisherDigestServiceImpl implements PublisherDigestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherDigestServiceImpl.class);

    /**
     * store publishers
     */
    @Autowired
    private DataStore           sessionDataStore;

    /**
     * calculate data node url
     */
    @Autowired
    private NodeManager         dataNodeManager;

    @Override
    public Map<String/*dataServerIp*/, String/*pubDigestSum*/> getConnectDigest(String connectId) {

        Map<String, List<Publisher>> connectIdPubs = getConnectSnapShot(connectId);

        if(connectIdPubs != null && !connectIdPubs.isEmpty()){
            Map<String, String> digestMap = new HashMap<>();
            connectIdPubs.forEach((dataIP,publishers)-> digestMap.put(dataIP, String.valueOf(PublisherDigest.getDigestValueSum(publishers))));

            return digestMap;
        }

        return null;
    }

    @Override
    public Map<String/*dataServerIp*/, List<Publisher>> getConnectSnapShot(String connectId) {

        Map<String,Publisher> pubMap = sessionDataStore.queryByConnectId(connectId);

        if(pubMap != null && !pubMap.isEmpty()){

            Map<String, List<Publisher>> connectIdPubs = new ConcurrentHashMap<>();

            pubMap.values().forEach(publisher->{

                Node dataNode = dataNodeManager.getNode(publisher.getDataInfoId());

                List<Publisher> connPublishers = connectIdPubs.computeIfAbsent(dataNode.getNodeUrl().getIpAddress(), k-> new ArrayList<>());

                connPublishers.add(publisher);
            });

            return connectIdPubs;
        } else {
            LOGGER.error("Cannot get publishers by connectId:{}!",connectId);
        }

        return null;
    }
}