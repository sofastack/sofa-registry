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
package com.alipay.sofa.registry.server.data.datasync;

import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;

/**
 *
 * @author shangyu.wh
 * @version $Id: AcceptorStore.java, v 0.1 2018-03-22 12:21 shangyu.wh Exp $
 */
public interface AcceptorStore {

    /**
     * appender operator logs to Acceptor
     * @param operator
     */
    void addOperator(Operator operator);

    /**
     * notify change fire sync data get request,get sync data return
     * @param syncDataRequest
     * @return
     */
    SyncData getSyncData(SyncDataRequest syncDataRequest);

    /**
     * scheduler get change Acceptor to notify other dataCenter or data node get sync data
     */
    void changeDataCheck();

    /**
     * scheduler check Acceptor has Expired,remove all queue logs
     */
    void checkAcceptorsChangAndExpired();

    /**
     * get sync type,dataCenter or dataNode
     * @return
     */
    String getType();

}