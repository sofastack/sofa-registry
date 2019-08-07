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

import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.datasync.AcceptorStore;
import com.alipay.sofa.registry.server.data.datasync.Operator;
import com.alipay.sofa.registry.server.data.datasync.SyncDataService;

/**
 *
 * @author shangyu.wh
 * @version $Id: SyncDataService.java, v 0.1 2018-03-07 16:13 shangyu.wh Exp $
 */
public class SyncDataServiceImpl implements SyncDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncDataServiceImpl.class,
                                           "[SyncDataService]");

    @Override
    public void appendOperator(Operator operator) {
        AcceptorStore acceptorStore = StoreServiceFactory.getStoreService(operator.getSourceType()
            .toString());
        if (acceptorStore != null) {
            acceptorStore.addOperator(operator);
        } else {
            LOGGER.error("Can't find acceptor store type {} to Append operator!",
                operator.getSourceType());
            throw new RuntimeException("Can't find acceptor store to Append operator!");
        }
    }

    @Override
    public SyncData getSyncDataChange(SyncDataRequest syncDataRequest) {
        AcceptorStore acceptorStore = StoreServiceFactory.getStoreService(syncDataRequest
            .getDataSourceType());
        if (acceptorStore != null) {
            return acceptorStore.getSyncData(syncDataRequest);
        } else {
            LOGGER.error("Can't find acceptor store type {} to get sync Data!",
                syncDataRequest.getDataSourceType());
            throw new RuntimeException("Can't find acceptor store to get sync Data!");
        }
    }

}