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
package com.alipay.sofa.registry.server.session.utils;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Subscriber;

import java.util.HashMap;

/**
 * @author xuanbei
 * @since 2019/2/12
 */
public class DatumUtils {
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
            datum.setPubMap(new HashMap<>());
            datum.setDataCenter(ValueConstants.DEFAULT_DATA_CENTER);
        }
        return datum;
    }
}
