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
package com.alipay.sofa.registry.server.session.assemble;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.AppPublisher;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.AssembleType;
import com.alipay.sofa.registry.server.session.cache.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.cache.SessionDatumCacheDecorator;
import com.alipay.sofa.registry.server.session.converter.AppPublisherConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version $Id: AppInterfaceAssembleService.java, v 0.1 2020年11月24日 19:34 xiaojian.xj Exp $
 */
public class AppInterfaceAssembleService implements AssembleService {

    @Autowired
    private SessionDatumCacheDecorator sessionDatumCacheDecorator;

    @Autowired
    private AppRevisionCacheRegistry   appRevisionCacheRegistry;

    @Override
    public AssembleType support() {
        return AssembleType.sub_app_and_interface;
    }

    @Override
    public Map<String/*datacenter*/, Datum> getAssembleDatum(Subscriber subscriber) {
        //get interface pub
        Map<String/*datacenter*/, Datum> interfaceDatumMap = sessionDatumCacheDecorator
            .getDatumsCache(subscriber.getDataInfoId());

        Map<String/*datacenter*/, Map<String/*appName*/, Datum>> appDatumMap = sessionDatumCacheDecorator
            .getAppDatumCache(subscriber.getDataInfoId(), subscriber.getInstanceId());

        Set<String> datacenters = new HashSet<>();
        datacenters.addAll(interfaceDatumMap.keySet());
        datacenters.addAll(appDatumMap.keySet());

        Map<String, Datum> assembleResult = new HashMap<>();
        for (String datacenter : datacenters) {

            Datum interfaceDatum = interfaceDatumMap.get(datacenter);
            Map<String, Datum> appDatum = appDatumMap.get(datacenter);

            Datum assembleDatum = doMerge(interfaceDatum, appDatum, subscriber);

            assembleResult.put(datacenter, assembleDatum);
        }
        return assembleResult;
    }

    @Override
    public Datum getAssembleDatum(String datacenter, Subscriber subscriber) {
        //get interface pub
        Datum interfaceDatum = sessionDatumCacheDecorator.getDatumCache(datacenter,
            subscriber.getDataInfoId());

        Map<String/*datacenter*/, Map<String/*appName*/, Datum>> appDatumMap = sessionDatumCacheDecorator
            .getAppDatumCache(subscriber.getDataInfoId(), subscriber.getInstanceId());

        Map<String, Datum> appDatum = appDatumMap.get(datacenter);
        Datum assembleDatum = doMerge(interfaceDatum, appDatum, subscriber);

        return assembleDatum;
    }

    private Datum doMerge(Datum interfaceDatum, Map<String, Datum> appDatum, Subscriber subscriber) {
        if (Objects.isNull(interfaceDatum) && CollectionUtils.isEmpty(appDatum)) {
            return null;
        }

        String dataCenter;
        if (Objects.nonNull(interfaceDatum)) {
            dataCenter = interfaceDatum.getDataCenter();
        } else {
            dataCenter = appDatum.values().stream().findAny().get().getDataCenter();
        }

        DataInfo dataInfo = DataInfo.valueOf(subscriber.getDataInfoId());
        Datum datum = new Datum();
        datum.setDataInfoId(dataInfo.getDataInfoId());
        datum.setDataCenter(dataCenter);
        datum.setDataId(dataInfo.getDataId());
        datum.setInstanceId(dataInfo.getInstanceId());
        datum.setGroup(dataInfo.getDataType());

        if (interfaceDatum != null) {
            datum.setVersion(interfaceDatum.getVersion());
            datum.getPubMap().putAll(interfaceDatum.getPubMap());
        }

        if (!CollectionUtils.isEmpty(appDatum)) {
            for (Datum app : appDatum.values()) {
                datum.setVersion(Math.max(app.getVersion(), datum.getVersion()));
                for (Publisher publisher : app.getPubMap().values()) {
                    if (!(publisher instanceof AppPublisher)) {
                        continue;
                    }
                    AppPublisher appPublisher = (AppPublisher) publisher;

                    datum.getPubMap().put(
                        appPublisher.getRegisterId(),
                        AppPublisherConverter.convert(appPublisher, appRevisionCacheRegistry,
                            dataInfo));
                }
            }
        }

        return datum;
    }
}