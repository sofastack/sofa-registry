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
package com.alipay.sofa.registry.server.meta.bootstrap;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.server.meta.node.NodeService;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.store.StoreService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: StoreServiceFactory.java, v 0.1 2018-01-11 22:12 shangyu.wh Exp $
 */
public class ServiceFactory implements ApplicationContextAware {

    private static Map<NodeType, StoreService>       storeServiceMap   = new HashMap<>();

    private static Map<NodeType, NodeConnectManager> connectManagerMap = new HashMap<>();

    private static Map<NodeType, NodeService>        nodeServiceMap    = new HashMap<>();

    /**
     * get storeservice by node type
     * @param nodeType
     * @return
     */
    public static StoreService getStoreService(NodeType nodeType) {
        return storeServiceMap.get(nodeType);
    }

    /**
     * get node service by node type
     * @param nodeType
     * @return
     */
    public static NodeService getNodeService(NodeType nodeType) {
        return nodeServiceMap.get(nodeType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, StoreService> map = applicationContext.getBeansOfType(StoreService.class);

        map.forEach((key, value) -> storeServiceMap.put(value.getNodeType(), value));

        Map<String, NodeConnectManager> managerMap = applicationContext
                .getBeansOfType(NodeConnectManager.class);

        managerMap.forEach((key, value) -> connectManagerMap.put(value.getNodeType(), value));

        Map<String, NodeService> nodeServiceBeanMap = applicationContext
                .getBeansOfType(NodeService.class);

        nodeServiceBeanMap.forEach((key, value) -> nodeServiceMap.put(value.getNodeType(), value));
    }
}