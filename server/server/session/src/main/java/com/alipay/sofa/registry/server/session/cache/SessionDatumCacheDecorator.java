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
package com.alipay.sofa.registry.server.session.cache;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.store.Interests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author xiaojian.xj
 * @version $Id: SessionDatumCacheDecorator.java, v 0.1 2020年11月03日 20:23 xiaojian.xj Exp $
 */
public class SessionDatumCacheDecorator {

    private static final Logger      taskLogger = LoggerFactory
                                                    .getLogger(SessionDatumCacheDecorator.class);

    @Autowired
    private Interests                sessionInterests;

    @Autowired
    private CacheService             sessionCacheService;

    @Autowired
    private AppRevisionCacheRegistry appRevisionCacheRegistry;

    public Datum getDatumCache(String dataCenter, String dataInfoId) {
        DatumKey datumKey = new DatumKey(dataInfoId, dataCenter);
        Key key = new Key(Key.KeyType.OBJ, datumKey.getClass().getName(), datumKey);

        Value<Datum> value = null;
        try {
            value = sessionCacheService.getValue(key);
        } catch (CacheAccessException e) {
            // The version is set to 0, so that when session checks the datum versions regularly, it will actively re-query the data.
            boolean result = sessionInterests.checkAndUpdateInterestVersionZero(dataCenter,
                dataInfoId);
            taskLogger.error(String.format(
                "error when access cache, so checkAndUpdateInterestVersionZero(return %s): %s",
                result, e.getMessage()), e);
        }

        return value == null ? null : value.getPayload();
    }

    public Map<String, Datum> getDatumsCache(String dataInfoId) {
        Map<String, Datum> map = new HashMap<>();
        NodeManager nodeManager = NodeManagerFactory.getNodeManager(Node.NodeType.META);
        Collection<String> dataCenters = nodeManager.getDataCenters();
        if (dataCenters != null) {
            Collection<Key> keys = dataCenters.stream().map(dataCenter -> new Key(Key.KeyType.OBJ,
                DatumKey.class.getName(), new DatumKey(dataInfoId, dataCenter)))
                .collect(Collectors.toList());

            Map<Key, Value> values = null;
            try {
                values = sessionCacheService.getValues(keys);
            } catch (CacheAccessException e) {
                // The version is set to 0, so that when session checks the datum versions regularly, it will actively re-query the data.
                for (String dataCenter : dataCenters) {
                    boolean result = sessionInterests.checkAndUpdateInterestVersionZero(dataCenter,
                        dataInfoId);
                    taskLogger.error(String.format(
                        "error when access cache, so checkAndUpdateInterestVersionZero(return %s): %s",
                        result, e.getMessage()), e);
                }
            }

            if (values != null) {
                values.forEach((key, value) -> {
                    if (value != null && value.getPayload() != null) {
                        Datum datum = (Datum) value.getPayload();
                        String dataCenter = ((DatumKey) key.getEntityType()).getDataCenter();
                        map.put(dataCenter, datum);
                    }
                });
            }

        }
        return map;
    }

    public Map<String, Map<String, Datum>> getAppDatumCache(String dataInfoId, String instanceId) {
        Map<String/*datacenter*/, Map<String/*appName*/, Datum>> result = new HashMap<>();
        //get metadata from session cache
        for (String appName : appRevisionCacheRegistry.getApps(dataInfoId)) {
            String appDataInfoId = DataInfo.toDataInfoId(appName, instanceId, ValueConstants.SOFA_APP);

            Map<String/*datacenter*/, Datum> appDatum = this.getDatumsCache(appDataInfoId);

            if (CollectionUtils.isEmpty(appDatum)) {
                continue;
            }

            for (Entry<String/*datacenter*/, Datum> datumEntry : appDatum.entrySet()) {
                String datacenter = datumEntry.getKey();
                Datum datum = datumEntry.getValue();
                Map<String, Datum> map = result.computeIfAbsent(datacenter, k -> new HashMap<>());
                map.put(appName, datum);
            }
        }
        return result;
    }
}
