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

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 21:52 yuzhi.lyz Exp $
 */
public final class PublisherGroups {
    private final Map<String, PublisherGroup> publisherGroupMap = Maps.newConcurrentMap();
    private final String                      dataCenter;

    PublisherGroups(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    Datum getDatum(String dataInfoId) {
        PublisherGroup group = publisherGroupMap.get(dataInfoId);
        return group == null ? null : group.toDatum();
    }

    DatumVersion getVersion(String dataInfoId) {
        PublisherGroup group = publisherGroupMap.get(dataInfoId);
        return group == null ? null : group.getVersion();
    }

    Map<String, DatumVersion> getVersions() {
        final Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
        publisherGroupMap.forEach((k, v) -> ret.put(k, v.getVersion()));
        return ret;
    }

    Map<String, Datum> getAllDatum() {
        Map<String, Datum> map = Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
        publisherGroupMap.forEach((k, v) -> {
            map.put(k, v.toDatum());
        });
        return map;
    }

    Map<String, List<Publisher>> getAllPublisher() {
        Map<String, List<Publisher>> map = Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
        publisherGroupMap.forEach((k, v) -> {
            map.put(k, v.getPublishers());
        });
        return map;
    }

    Map<String, Publisher> getByConnectId(ConnectId connectId) {
        Map<String, Publisher> map = Maps.newHashMapWithExpectedSize(64);
        publisherGroupMap.values().forEach(v -> map.putAll(v.getByConnectId(connectId)));
        return map;
    }

    PublisherGroup createGroupIfAbsent(String dataInfoId) {
        return publisherGroupMap
                .computeIfAbsent(dataInfoId,
                        k -> new PublisherGroup(dataInfoId, dataCenter));
    }

    Map<String, DatumVersion> clean(ProcessId sessionProcessId) {
        Map<String, DatumVersion> versionMap = Maps.newHashMapWithExpectedSize(64);
        publisherGroupMap.values().forEach(g -> {
            DatumVersion ver = g.clean(sessionProcessId);
            if (ver != null) {
                versionMap.put(g.dataInfoId, ver);
            }
        });
        return versionMap;
    }

    DatumVersion remove(String dataInfoId, ProcessId sessionProcessId) {
        PublisherGroup group = publisherGroupMap.get(dataInfoId);
        return group == null ? null : group.clean(sessionProcessId);
    }

    DatumVersion put(String dataInfoId, List<Publisher> publishers) {
        if (CollectionUtils.isEmpty(publishers)) {
            return null;
        }
        PublisherGroup group = createGroupIfAbsent(dataInfoId);
        return group.put(publishers);
    }

    DatumVersion remove(String dataInfoId, ProcessId sessionProcessId,
                        Map<String, RegisterVersion> removedPublishers) {
        PublisherGroup group = publisherGroupMap.get(dataInfoId);
        return group == null ? null : group.remove(sessionProcessId, removedPublishers);
    }

    Map<String, DatumSummary> getSummary(String sessionIpAddress) {
        Map<String, DatumSummary> summaries = Maps.newHashMap();
        publisherGroupMap.forEach((k, g) -> {
            DatumSummary summary = g.getSummary(sessionIpAddress);
            if (!summary.isEmpty()) {
                summaries.put(k, summary);
            }
        });
        return summaries;
    }

    Set<ProcessId> getSessionProcessIds() {
        Set<ProcessId> ids = Sets.newHashSet();
        publisherGroupMap.values().forEach(g -> ids.addAll(g.getSessionProcessIds()));
        return ids;
    }

    Map<String, Integer> compact(long tombstoneTimestamp) {
        Map<String, Integer> compacts = Maps.newHashMap();
        publisherGroupMap.values().forEach(g -> {
            int count = g.compact(tombstoneTimestamp);
            if (count != 0) {
                compacts.put(g.dataInfoId, count);
            }
        });
        return compacts;
    }

    void updateVersion() {
        publisherGroupMap.values().forEach(g -> g.updateVersion());
    }

    DatumVersion updateVersion(String dataInfoId) {
        PublisherGroup group = publisherGroupMap.get(dataInfoId);
        if (group == null) {
            return null;
        }
        return group.updateVersion();
    }

    @Override
    public String toString() {
        return StringFormatter
            .format("PubGroups{{},size={}}", dataCenter, publisherGroupMap.size());
    }
}
