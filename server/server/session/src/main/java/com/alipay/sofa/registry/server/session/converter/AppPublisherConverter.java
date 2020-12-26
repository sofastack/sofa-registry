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

import com.alipay.sofa.registry.common.model.AppRegisterServerDataBox;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.store.AppPublisher;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.server.session.cache.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.utils.AddressUtil;
import com.google.common.collect.ArrayListMultimap;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author xiaojian.xj
 * @version $Id: AppPublisherConverter.java, v 0.1 2020年11月24日 20:50 xiaojian.xj Exp $
 */
public class AppPublisherConverter {

    public static Publisher convert(AppPublisher appPublisher,
                                    AppRevisionCacheRegistry appRevisionCacheRegistry,
                                    DataInfo dataInfo) {
        Publisher publisher = new Publisher();
        String dataInfoId = dataInfo.getDataInfoId();
        fillCommonRegion(publisher, appPublisher, dataInfo);
        List<ServerDataBox> dataList = new ArrayList<>();
        for (AppRegisterServerDataBox appRegisterServerDataBox : appPublisher.getAppDataList()) {
            AppRevision revisionRegister = appRevisionCacheRegistry
                .getRevision(appRegisterServerDataBox.getRevision());
            if (!revisionRegister.getInterfaceMap().containsKey(dataInfoId)) {
                continue;
            }
            Map<String, Collection<String>> params = extractParams(revisionRegister,
                appRegisterServerDataBox, dataInfoId);
            ServerDataBox serverDataBox = new ServerDataBox(AddressUtil.buildURL(
                appRegisterServerDataBox.getUrl(), params));
            serverDataBox.object2bytes();
            dataList.add(serverDataBox);
        }
        publisher.setDataList(dataList);
        return publisher;

    }

    private static Map<String, Collection<String>> extractParams(AppRevision revisionRegister,
                                                                 AppRegisterServerDataBox serverDataBox,
                                                                 String dataInfoId) {
        ArrayListMultimap<String, String> multimap = ArrayListMultimap.create();

        combineParams(revisionRegister.getBaseParams(), multimap);
        combineParams(serverDataBox.getBaseParams(), multimap);

        if (!CollectionUtils.isEmpty(revisionRegister.getInterfaceMap())) {
            AppRevisionInterface appRevisionInterface = revisionRegister.getInterfaceMap().get(
                dataInfoId);
            if (appRevisionInterface != null) {
                combineParams(appRevisionInterface.getServiceParams(), multimap);
            }
        }
        if (!CollectionUtils.isEmpty(serverDataBox.getInterfaceParams())) {
            Map<String, List<String>> params = serverDataBox.getInterfaceParams().get(dataInfoId);
            combineParams(params, multimap);
        }
        return multimap.asMap();
    }

    private static void combineParams(Map<String, List<String>> params, ArrayListMultimap<String, String> multimap) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        params.forEach((key, value) -> multimap.putAll(key, value));
    }

    private static void fillCommonRegion(Publisher publisher, AppPublisher source, DataInfo dataInfo) {

        publisher.setAppName(source.getAppName());
        //ZONE MUST BE CURRENT SESSION ZONE
        publisher.setCell(source.getCell());
        publisher.setClientId(source.getClientId());
        publisher.setDataId(dataInfo.getDataId());
        publisher.setGroup(dataInfo.getDataType());
        publisher.setInstanceId(dataInfo.getInstanceId());
        publisher.setRegisterId(source.getRegisterId());
        publisher.setProcessId(source.getProcessId());
        publisher.setVersion(source.getVersion());
        publisher.setRegisterTimestamp(source.getRegisterTimestamp());

        publisher.setClientRegisterTimestamp(source.getClientRegisterTimestamp());
        publisher.setSourceAddress(source.getSourceAddress());

        publisher.setClientVersion(source.getClientVersion());
        publisher.setDataInfoId(dataInfo.getDataInfoId());
    }

}