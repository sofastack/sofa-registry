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
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.slot.DataSlotManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-03 16:55 yuzhi.lyz Exp $
 */
public final class LocalSlotDatumStorage implements DatumStorage {
    @Autowired
    private DataSlotManager  dataSlotManager;
    @Autowired
    private DataServerConfig dataServerConfig;

    private final Map<Short, DatumStorage> localDatumStorage = new ConcurrentHashMap<>();

    @Override
    public Datum get(String dataCenter, String dataInfoId) {
        return null;
    }

    @Override
    public Map<String, Datum> get(String dataInfoId) {
        return null;
    }

    @Override
    public Map<String, Map<String, Datum>> getAll() {
        return null;
    }

    @Override
    public Map<String, Publisher> getByConnectId(String connectId) {
        return null;
    }

    @Override
    public Map<String, Publisher> getOwnByConnectId(String connectId) {
        return null;
    }

    @Override
    public Set<String> getAllConnectIds() {
        return null;
    }

    @Override
    public MergeResult putDatum(DataChangeTypeEnum changeType, Datum datum) {
        return null;
    }

    @Override
    public boolean cleanDatum(String dataCenter, String dataInfoId) {
        return false;
    }

    @Override
    public Datum putSnapshot(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
                             Map<String, Publisher> snapshotPubMap) {
        return null;
    }

    @Override
    public Map<String, Long> getVersions(String dataInfoId) {
        return null;
    }
}
