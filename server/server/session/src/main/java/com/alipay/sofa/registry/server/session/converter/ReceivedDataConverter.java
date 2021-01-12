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
package com.alipay.sofa.registry.server.session.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;

/**
 * The type Received data converter.
 * @author shangyu.wh
 * @version $Id : ReceivedDataConverter.java, v 0.1 2017-12-13 13:42 shangyu.wh Exp $
 */
public class ReceivedDataConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceivedDataConverter.class);

    /**
     * convert for no datum from data node,just use for first get no datum
     * if push empty data to subscriber must reset version,avoid client ignore this empty push
     * @param dataId
     * @param group
     * @param instanceId
     * @param dataCenter
     * @param scope
     * @param subscriberRegisterIdList
     * @param regionLocal
     * @return
     */
    public static ReceivedData getReceivedDataMulti(String dataId, String group, String instanceId,
                                                    String dataCenter, ScopeEnum scope,
                                                    List subscriberRegisterIdList,
                                                    String regionLocal) {
        ReceivedData receivedData = new ReceivedData();
        receivedData.setDataId(dataId);
        receivedData.setGroup(group);
        receivedData.setInstanceId(instanceId);
        receivedData.setSubscriberRegistIds(subscriberRegisterIdList);
        receivedData.setSegment(dataCenter);
        receivedData.setScope(scope.name());
        //no datum set return version as mini as,avoid old client check
        receivedData.setVersion(ValueConstants.DEFAULT_NO_DATUM_VERSION);

        receivedData.setLocalZone(regionLocal);

        Map<String/*zone*/, List<DataBox>> swizzMap = new HashMap<>();
        receivedData.setData(swizzMap);
        return receivedData;
    }

    /**
     * Standard RunEnv
     * @param datum the datum 
     * @param scope the scope 
     * @param subscriberRegisterIdList the subscriber register id list 
     * @param regionLocal the region local 
     * @return received data multi
     */
    public static ReceivedData getReceivedDataMulti(Datum datum, ScopeEnum scope,
                                                    List subscriberRegisterIdList,
                                                    String regionLocal,
                                                    Predicate<String> zonePredicate) {

        if (null == datum) {
            return null;
        }

        // todo judge server mode to decide local region
        ReceivedData receivedData = new ReceivedData();
        receivedData.setDataId(datum.getDataId());
        receivedData.setGroup(datum.getGroup());
        receivedData.setInstanceId(datum.getInstanceId());
        receivedData.setSubscriberRegistIds(subscriberRegisterIdList);
        receivedData.setSegment(datum.getDataCenter());
        receivedData.setScope(scope.name());

        receivedData.setVersion(datum.getVersion());

        receivedData.setLocalZone(regionLocal);

        Map<String/*zone*/, List<DataBox>> swizzMap = new HashMap<>();

        Map<String, Publisher> publisherMap = datum.getPubMap();
        if (publisherMap.isEmpty()) {
            receivedData.setData(swizzMap);
            return receivedData;
        }
        for (Entry<String, Publisher> entry : publisherMap.entrySet()) {
            Publisher publisher = entry.getValue();
            List<ServerDataBox> datas = publisher.getDataList();

            String region = publisher.getCell();

            if (zonePredicate.test(region)) {
                continue;
            }

            if (null == datas) {
                datas = new ArrayList<>();
            }

            List<DataBox> regionDatas = swizzMap.computeIfAbsent(region, k -> new ArrayList<>());
            fillRegionDatas(regionDatas, datas);

        }

        receivedData.setData(swizzMap);

        return receivedData;
    }

    private static void fillRegionDatas(List<DataBox> regionDatas, List<ServerDataBox> datas) {
        for (ServerDataBox data : datas) {
            DataBox box = new DataBox();
            try {
                String dataString = (String) data.extract();
                box.setData(dataString);
                regionDatas.add(box);
            } catch (Exception e) {
                LOGGER.error("ReceivedData convert error", e);
            }
        }
    }

    /**
     * Cloud RunEnv
     * @param datums the datums 
     * @param scope the scope 
     * @param subscriberRegisterIdList the subscriber register id list 
     * @param subscriber decide common info 
     * @return received data multi
     */
    public static ReceivedData getReceivedDataMulti(Map<String, Datum> datums, ScopeEnum scope,
                                                    List subscriberRegisterIdList,
                                                    Subscriber subscriber) {
        ReceivedData receivedData = new ReceivedData();
        receivedData.setDataId(subscriber.getDataId());
        receivedData.setGroup(subscriber.getGroup());
        receivedData.setInstanceId(subscriber.getInstanceId());
        receivedData.setSubscriberRegistIds(subscriberRegisterIdList);
        receivedData.setSegment(ValueConstants.DEFAULT_DATA_CENTER);
        receivedData.setScope(scope.name());

        String regionLocal = subscriber.getCell();
        receivedData.setLocalZone(regionLocal);

        receivedData.setVersion(DatumVersionUtil.nextId());

        Map<String/*zone*/, List<DataBox>> swizzMap = new HashMap<>();

        for (Entry<String/*dataCenter*/, Datum> entry : datums.entrySet()) {
            Datum datum = entry.getValue();

            Map<String, Publisher> publisherMap = datum.getPubMap();
            if (publisherMap.isEmpty()) {
                continue;
            }

            for (Entry<String, Publisher> publishers : publisherMap.entrySet()) {
                Publisher publisher = publishers.getValue();
                List<ServerDataBox> datas = publisher.getDataList();

                String region = publisher.getCell();

                if (ScopeEnum.zone == scope && !regionLocal.equals(region)) {
                    // zone scope subscribe only return zone list
                    continue;
                }

                if (null == datas) {
                    datas = new ArrayList<>();
                }

                List<DataBox> regionDatas = swizzMap.computeIfAbsent(region,
                        k -> new ArrayList<>());
                fillRegionDatas(regionDatas, datas);

            }
        }

        receivedData.setData(swizzMap);
        return receivedData;
    }

    /**
     * Gets merge datum.
     *
     * @param datumMap the datum map 
     * @return the merge datum
     */
    public static Datum getMergeDatum(Map<String, Datum> datumMap) {
        Datum merge = null;
        Map<String, Publisher> mergePublisherMap = new HashMap<>();
        long version = 0;
        for (Datum datum : datumMap.values()) {
            if (datum.getDataId() == null) {
                LOGGER.error("ReceivedData convert error,datum dataId is null,datum={}", datum);
                continue;
            }
            if (null == merge) {
                //new Datum avoid to change datumMap
                merge = new Datum(datum.getDataInfoId(), datum.getDataCenter());
                merge.setDataId(datum.getDataId());
                merge.setGroup(datum.getGroup());
                merge.setInstanceId(datum.getInstanceId());
            }
            mergePublisherMap.putAll(datum.getPubMap());
            version = Math.max(version, datum.getVersion());
        }
        if (null == merge) {
            return null;
        }
        merge.setVersion(version);
        merge.addPublishers(mergePublisherMap);
        merge.setDataCenter(ValueConstants.DEFAULT_DATA_CENTER);
        return merge;
    }

    public static ReceivedConfigData getReceivedConfigData(ServerDataBox dataBox,
                                                           DataInfo dataInfo, Long version) {
        ReceivedConfigData receivedConfigData = new ReceivedConfigData();

        if (dataBox != null) {
            DataBox box = new DataBox();
            String dataString = (String) dataBox.getObject();
            box.setData(dataString);
            receivedConfigData.setDataBox(box);
        }
        receivedConfigData.setDataId(dataInfo.getDataId());
        receivedConfigData.setGroup(dataInfo.getDataType());
        receivedConfigData.setInstanceId(dataInfo.getInstanceId());
        receivedConfigData.setVersion(version);

        return receivedConfigData;
    }
}