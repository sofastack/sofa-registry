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

import java.util.Collection;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.MetaNode;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaNodeManager.java, v 0.1 2018-03-05 10:58 shangyu.wh Exp $
 */
public class MetaNodeManager extends AbstractNodeManager<MetaNode> {

    @Override
    public MetaNode getNode(String dataInfoId) {
        return null;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.META;
    }

    @Override
    public Collection<String> getDataCenters() {
        return nodes.keySet();
    }

    @Override
    public void renewNode() {
    }
}