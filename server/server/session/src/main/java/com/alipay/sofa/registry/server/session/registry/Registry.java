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
package com.alipay.sofa.registry.server.session.registry;

import java.util.List;

import com.alipay.sofa.registry.common.model.store.StoreData;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionRegistry.java, v 0.1 2017-11-27 19:49 shangyu.wh Exp $
 */
public interface Registry {

    /**
     * register new publisher or subscriber data
     *
     * @param data
     */
    void register(StoreData<String> data);

    /**
     * cancel publisher or subscriber data by client ip address and port(ip:port),one ip address has more than one processId
     * when disconnection between client node and session node,disconnect event fire remove all pub and sub info on session and data node
     * this function always use in Console manage Client node list,input ip list must transform to connectId through connect manage
     *
     * @param connectIds
     */
    void cancel(List<String> connectIds);

    /**
     * remove publisher or subscriber data by client ip address and port(ip:port)
     * this function always use in rest api Console manage ,the run mode is standard
     * remove subscriber data will push empty datum to some one who has dataInfoId begin with pushEmptyDataDataIdPrefixes config
     *
     * @param connectIds
     */
    void remove(List<String> connectIds);

    /**
     * message mode com.alipay.sofa.registry.client.provider for client node to unregister single subscriber or publisher data
     *
     * @param data
     */
    void unRegister(StoreData<String> data);

    /**
     * Timing tasks to send all subscriber dataInfoId version to data,
     * Select higher version data push to client node
     *
     */
    void fetchChangData();

    /**
     * for fetchChangData first invoke
     */
    void fetchChangDataProcess();

    /**
     * for scheduler clean no connect client
     */
    void cleanClientConnect();
}