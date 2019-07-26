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
package com.alipay.sofa.registry.server.session.node.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.SessionServerRegisterRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;

/**
 *
 * @author shangyu.wh
 * @version $Id: NodeService.java, v 0.1 2017-11-28 11:09 shangyu.wh Exp $
 */
public interface DataNodeService {

    /**
     * new publisher data transform to data server
     *
     * @param publisher
     */
    void register(Publisher publisher);

    /**
     * remove publisher data from data server
     *
     * @param publisher
     */
    void unregister(Publisher publisher);

    /**
     * session server support api to stop some client node,all register data on data server will be removed
     * data on session server will be remove too
     *
     * @param connectIds
     */
    void clientOff(List<String> connectIds);

    /**
     * Get some dataInfoId version from one data server
     *
     * @param dataNodeUrl
     * @param dataInfoIdList
     * @return
     */
    Map<String/*datacenter*/, Map<String/*datainfoid*/, Long>> fetchDataVersion(URL dataNodeUrl,
                                                                                  Collection<String> dataInfoIdList);

    /**
     * fetch one dataCenter publisher data from data server
     *
     * @param dataInfoId
     * @param dataCenterId
     * @return
     */
    Datum fetchDataCenter(String dataInfoId, String dataCenterId);

    /**
     * fetch all dataCenter datum
     * @param dataInfoId
     * @return
     */
    Map<String/*datacenterId*/, Datum> fetchGlobal(String dataInfoId);

    /**
     * fetch datum by specify dataCenter and dataInfoId
     * @param dataInfoId
     * @param dataCenterId
     * @return
     */
    Map<String, Datum> getDatumMap(String dataInfoId, String dataCenterId);

    /**
     * register session process id when connect to data node
     * process id see SessionProcessIdGenerator
     * @param sessionServerRegisterRequest
     * @param dataUrl
     */
    void registerSessionProcessId(SessionServerRegisterRequest sessionServerRegisterRequest,
                                  URL dataUrl);

    /**
     * check publisher digest same as session current store,and renew the lastUpdateTime of this connectId
     */
    Boolean renewDatum(RenewDatumRequest renewDatumRequest);

    /**
     * Correct the publishers information of this connectId on dataServer
     */
    void sendDatumSnapshot(DatumSnapshotRequest datumSnapshotRequest);

}