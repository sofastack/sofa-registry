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
package com.alipay.sofa.registry.server.data.remoting.metaserver;

import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.DataHeartBeatResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.SlotManager;
import com.alipay.sofa.registry.server.shared.meta.AbstractMetaServerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author qian.lqlq
 * @version $Id: MetaServiceImpl.java, v 0.1 2018－03－07 20:41 qian.lqlq Exp $
 */
public class MetaServerServiceImpl extends AbstractMetaServerService<DataHeartBeatResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaServerServiceImpl.class);

    @Autowired
    private DataServerConfig    dataServerConfig;

    @Autowired
    private SlotManager         slotManager;

    @Override
    protected void handleRenewResult(DataHeartBeatResponse result) {
        updateMetaIps(result.getMetaNodesMap().keySet());
        slotManager.updateSlotTable(result.getSlotTable());
    }
}