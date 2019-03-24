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
package com.alipay.sofa.registry.server.data.datasync.sync;

import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.node.DataServerNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author shangyu.wh
 * @version $Id: LocalAcceptorStore.java, v 0.1 2018-03-22 12:31 shangyu.wh Exp $
 */
public class LocalAcceptorStore extends AbstractAcceptorStore {
    @Override
    public String getType() {
        return DataSourceTypeEnum.BACKUP.toString();
    }

    @Override
    public List<String> getTargetDataIp(String dataInfoId) {

        List<String> ips = new ArrayList<>();
        List<DataServerNode> dataServerNodes = metaServerService
                .getDataServers(getDataServerConfig().getLocalDataCenter(), dataInfoId);
        if (dataServerNodes != null && !dataServerNodes.isEmpty()) {
            ips = dataServerNodes.stream().map(DataServerNode::getIp).collect(Collectors.toList());
        }

        return ips;
    }
}