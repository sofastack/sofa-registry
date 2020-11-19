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

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * cache of datum, providing query function to the upper module
 *
 * @author kezhu.wukz
 * @author qian.lqlq
 * @version $Id: DatumCache.java, v 0.1 2017-12-06 20:50 qian.lqlq Exp $
 */
public class DatumCache {

    @Autowired
    private DatumStorage     localDatumStorage;

    @Autowired
    private DataServerConfig dataServerConfig;

    /**
     * get datum by specific dataCenter and dataInfoId
     *
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public Datum get(String dataCenter, String dataInfoId) {
        return localDatumStorage.get(dataInfoId);
    }

    /**
     * get datum of all data centers by dataInfoId
     *
     * @param dataInfoId
     * @return
     */
    public Map<String, Datum> get(String dataInfoId) {
        Map<String, Datum> datumMap = new HashMap<>();

        //local
        Datum localDatum = localDatumStorage.get(dataInfoId);
        datumMap.put(dataServerConfig.getLocalDataCenter(), localDatum);

        return datumMap;
    }

    /**
     * get datum of all data centers by dataInfoId
     *
     * @param dataInfoId
     * @return
     */
    public Map<String, Long> getVersions(String dataInfoId) {
        Map<String, Long> datumMap = new HashMap<>();

        //local
        Long localVersions = localDatumStorage.getVersions(dataInfoId);
        if (localVersions != null) {
            datumMap.put(dataServerConfig.getLocalDataCenter(), localVersions);
        }

        return datumMap;
    }

    /**
     * get datum group by dataCenter
     *
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public Map<String, Datum> getDatumGroupByDataCenter(String dataCenter, String dataInfoId) {
        Map<String, Datum> map = new HashMap<>();
        if (StringUtils.isEmpty(dataCenter)) {
            map = this.get(dataInfoId);
        } else {
            Datum datum = this.get(dataCenter, dataInfoId);
            if (datum != null) {
                map.put(dataCenter, datum);
            }
        }
        return map;
    }

    /**
     * get all datum
     *
     * @return
     */
    public Map<String, Map<String, Datum>> getAll() {
        Map<String, Map<String, Datum>> datumMap = new HashMap<>();
        datumMap.put(dataServerConfig.getLocalDataCenter(), localDatumStorage.getAll());
        return datumMap;
    }

    /**
     *
     *
     * @param connectId
     * @return
     */
    public Map<String, Publisher> getByConnectId(String connectId) {
        return localDatumStorage.getByConnectId(connectId);
    }

    /**
     * put datum into cache
     *
     * @param datum
     * @return the last version before datum changed, if datum is not exist, return null
     */
    public MergeResult putDatum(Datum datum) {
        return localDatumStorage.putDatum(datum);
    }

    /**
     * remove datum ant contains all pub data,and clean all the client map reference
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    public boolean cleanDatum(String dataCenter, String dataInfoId) {
        return localDatumStorage.cleanDatum(dataInfoId);
    }

    /**
     * cover datum by snapshot
     */
    public Datum putSnapshot(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
                             Map<String, Publisher> snapshotPubMap) {
        return localDatumStorage.putSnapshot(dataInfoId, toBeDeletedPubMap, snapshotPubMap);
    }

    /**
     * Getter method for property <tt>OWN_CONNECT_ID_INDEX</tt>.
     *
     * @return property value of OWN_CONNECT_ID_INDEX
     */
    public Set<String> getAllConnectIds() {
        return localDatumStorage.getAllConnectIds();
    }

}