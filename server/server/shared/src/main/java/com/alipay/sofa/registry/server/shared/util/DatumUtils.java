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
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author xuanbei
 * @since 2019/2/12
 */
public final class DatumUtils {
    private DatumUtils() {
    }

    /**
     * create new datum when parameter is null.
     *
     * @param datum
     * @param subscriber
     * @return
     */
    public static Datum newDatumIfNull(Datum datum, Subscriber subscriber) {
        if (datum == null) {
            datum = new Datum();
            datum.setDataId(subscriber.getDataId());
            datum.setInstanceId(subscriber.getInstanceId());
            datum.setGroup(subscriber.getGroup());
            datum.setVersion(ValueConstants.DEFAULT_NO_DATUM_VERSION);
            datum.setDataCenter(ValueConstants.DEFAULT_DATA_CENTER);
        }
        return datum;
    }

    public static Map<String, Long> getVersions(Map<String, Datum> datumMap) {
        Map<String, Long> versions = Maps.newHashMapWithExpectedSize(datumMap.size());
        datumMap.forEach((k, v) -> versions.put(k, v.getVersion()));
        return versions;
    }

    public static Datum newEmptyDatum(Subscriber subscriber, String datacenter) {
        Datum datum = new Datum();
        datum.setDataId(subscriber.getDataId());
        datum.setInstanceId(subscriber.getInstanceId());
        datum.setGroup(subscriber.getGroup());
        datum.setVersion(ValueConstants.DEFAULT_NO_DATUM_VERSION);
        datum.setDataCenter(datacenter);
        return datum;
    }

    public static Datum newEmptyDatum(DataInfo dataInfo, String datacenter) {
        Datum datum = new Datum();
        datum.setDataId(dataInfo.getDataId());
        datum.setInstanceId(dataInfo.getInstanceId());
        datum.setGroup(dataInfo.getDataType());
        datum.setVersion(ValueConstants.DEFAULT_NO_DATUM_VERSION);
        datum.setDataCenter(datacenter);
        return datum;
    }
}
