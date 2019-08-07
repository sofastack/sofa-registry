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
package com.alipay.sofa.registry.server.session.node;

import com.alipay.sofa.registry.common.model.Node.NodeType;
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
public class NodeManagerFactory implements ApplicationContextAware {

    private static Map<NodeType, NodeManager> nodeManagerMap = new HashMap<>();

    public static NodeManager getNodeManager(NodeType nodeType) {
        return nodeManagerMap.get(nodeType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, NodeManager> map = applicationContext.getBeansOfType(NodeManager.class);

        map.forEach((key, value) -> nodeManagerMap.put(value.getNodeType(), value));
    }
}