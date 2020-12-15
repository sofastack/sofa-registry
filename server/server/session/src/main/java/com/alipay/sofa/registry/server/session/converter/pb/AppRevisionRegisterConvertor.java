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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.AppRevisionInterfacePb;
import com.alipay.sofa.registry.common.model.client.pb.AppRevisionRegisterPb;
import com.alipay.sofa.registry.common.model.client.pb.QueryValues;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRevisionRegisterConvertor {
    public static AppRevisionRegister convert2Java(AppRevisionRegisterPb appRevisionRegisterPb) {
        if (appRevisionRegisterPb == null) {
            return null;
        }
        AppRevisionRegister appRevision = new AppRevisionRegister();
        appRevision.setAppName(appRevisionRegisterPb.getAppname());
        appRevision.setRevision(appRevisionRegisterPb.getRevision());

        Map<String, List<String>> baseParams = new HashMap<>();
        for (Map.Entry<String, QueryValues> entry : appRevisionRegisterPb.getBaseParamsMap()
            .entrySet()) {
            QueryValues values = entry.getValue();
            baseParams.put(entry.getKey(),
                entry.getValue().getValuesList().subList(0, values.getValuesCount()));
        }
        appRevision.setBaseParams(baseParams);
        Map<String, AppRevisionInterface> serviceParams = new HashMap<>();
        for (AppRevisionInterfacePb interfacePb : appRevisionRegisterPb.getInterfacesList()) {
            AppRevisionInterface inf = new AppRevisionInterface();
            inf.setDataId(interfacePb.getDataId());
            inf.setGroup(interfacePb.getGroup());
            inf.setInstanceId(interfacePb.getInstanceId());
            interfacePb.getServiceParamsMap();
            for (Map.Entry<String, QueryValues> entry : interfacePb.getServiceParamsMap()
                .entrySet()) {
                String key = entry.getKey();
                QueryValues values = entry.getValue();
                inf.getServiceParams().put(key,
                    values.getValuesList().subList(0, values.getValuesCount()));
            }
            serviceParams.put(
                DataInfo.toDataInfoId(inf.getDataId(), inf.getInstanceId(), inf.getGroup()), inf);
        }
        appRevision.setInterfaces(serviceParams);
        return appRevision;
    }
}
