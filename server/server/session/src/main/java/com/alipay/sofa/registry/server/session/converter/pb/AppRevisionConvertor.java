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

import com.alipay.sofa.registry.common.model.client.pb.MetaRegister;
import com.alipay.sofa.registry.common.model.client.pb.MetaService;
import com.alipay.sofa.registry.common.model.client.pb.StringList;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * 
 * @author xiaojian.xj
 * @version $Id: AppRevisionConvertor.java, v 0.1 2021年02月04日 17:34 xiaojian.xj
 *          Exp $
 */
public class AppRevisionConvertor {

	public static AppRevision convert2Java(MetaRegister metaRegister) {

        AppRevision revision = new AppRevision();
        String appName = metaRegister.getApplication();
        revision.setAppName(appName);
        revision.setRevision(metaRegister.getRevision());
        revision.setClientVersion(metaRegister.getClientVersion());

        Map<String, List<String>> baseParams = new HashMap<>();
        for (Entry<String, StringList> entry : metaRegister.getBaseParamsMap().entrySet()) {
            StringList value = entry.getValue();
            baseParams
                .put(entry.getKey(), value.getValuesList().subList(0, value.getValuesCount()));
        }
        revision.setBaseParams(baseParams);

        Map<String, AppRevisionInterface> interfaceMap = new HashMap<>();
        for (Entry<String, MetaService> entry : metaRegister.getServicesMap().entrySet()) {
            interfaceMap.put(entry.getKey(), MetaServiceConvertor.convert2Java(entry.getValue()));
        }
        revision.setInterfaceMap(interfaceMap);
        return revision;
    }
	public static MetaRegister convert2Pb(AppRevision appRevision) {
        MetaRegister.Builder builder = MetaRegister.newBuilder();

        builder.setApplication(appRevision.getAppName());
        builder.setRevision(appRevision.getRevision());

        for (Entry<String, List<String>> entry : Optional.ofNullable(appRevision.getBaseParams())
            .orElse(new HashMap<>()).entrySet()) {
            StringList.Builder listBuilder = StringList.newBuilder().addAllValues(entry.getValue());
            builder.putBaseParams(entry.getKey(), listBuilder.build());
        }

        for (Entry<String, AppRevisionInterface> entry : Optional
            .ofNullable(appRevision.getInterfaceMap()).orElse(new HashMap<>()).entrySet()) {
            builder.putServices(entry.getKey(), MetaServiceConvertor.convert2Pb(entry.getValue()));
        }
        return builder.build();
    }
}
