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
package com.alipay.sofa.registry.server.data.cache;

import java.util.Map;
import java.util.Set;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;

/**
 *
 * @author kezhu.wukz
 * @version $Id: DatumAccessService.java, v 0.1 2019-12-05 11:51 kezhu.wukz Exp $
 */
public interface DatumStorage {

    /**
     * get datum by specific dataCenter and dataInfoId
     *
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    Datum get(String dataCenter, String dataInfoId);

    /**
     * get datum of all datercenters by dataInfoId
     *
     * @param dataInfoId
     * @return
     */
    Map<String, Datum> get(String dataInfoId);

    /**
     * get all datum
     *
     * @return
     */
    Map<String, Map<String, Datum>> getAll();

    /**
     *
     *
     * @param connectId
     * @return
     */
    Map<String, Publisher> getByConnectId(String connectId);

    /**
     * Getter method for property <tt>OWN_CONNECT_ID_INDEX</tt>.
     *
     * @return property value of OWN_CONNECT_ID_INDEX
     */
    Set<String> getAllConnectIds();

    /**
     * put datum into cache
     *
     * @param changeType
     * @param datum
     * @return the last version before datum changed, if datum is not exist, return null
     */
    MergeResult putDatum(DataChangeTypeEnum changeType, Datum datum);

    /**
     * remove datum ant contains all pub data,and clean all the client map reference
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    boolean cleanDatum(String dataCenter, String dataInfoId);

    boolean removePublisher(String dataCenter, String dataInfoId, String registerId);
    /**
     * cover datum by snapshot
     */
    Datum putSnapshot(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
                      Map<String, Publisher> snapshotPubMap);

    Map<String, Long> getVersions(String dataInfoId);

    Map<String, DatumSummary> getDatumSummary(String dataCenter, String targetIpAddress);

}