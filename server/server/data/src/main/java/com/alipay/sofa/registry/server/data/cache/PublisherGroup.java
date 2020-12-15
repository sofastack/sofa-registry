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
import com.alipay.sofa.registry.common.model.PublisherVersion;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 20:26 yuzhi.lyz Exp $
 */
public final class PublisherGroup {
    private static final Logger                         LOGGER = LoggerFactory
                                                                   .getLogger(PublisherGroup.class);

    private final Lock                                  lock   = new ReentrantLock();

    final String                                        dataInfoId;

    final String                                        dataCenter;

    final String                                        dataId;

    final String                                        instanceId;

    final String                                        group;

    // if the delete publisher from session, mark unpub
    final Map<String/*registerId*/, PublisherEnvelope> pubMap = Maps.newConcurrentMap();

    private long                                        version;

    PublisherGroup(Publisher publisher, String dataCenter) {
        this.dataInfoId = publisher.getDataInfoId();
        this.dataCenter = WordCache.getInstance().getWordCache(dataCenter);
        this.dataId = publisher.getDataId();
        this.instanceId = publisher.getInstanceId();
        this.group = publisher.getGroup();
        this.version = System.currentTimeMillis();
    }

    Datum toDatum() {
        Datum datum = new Datum();
        datum.setDataCenter(dataCenter);
        datum.setDataId(dataId);
        datum.setDataInfoId(dataInfoId);
        datum.setGroup(group);
        datum.setInstanceId(instanceId);
        long ver;
        List<PublisherEnvelope> list = new ArrayList<>(pubMap.size());
        lock.lock();
        try {
            ver = this.version;
            list.addAll(pubMap.values());
        } finally {
            lock.unlock();
        }
        datum.setVersion(ver);
        list.stream().filter(PublisherEnvelope::isPub).forEach(p -> datum.addPublisher(p.publisher));
        return datum;
    }

    Map<String, Publisher> getByConnectId(ConnectId connectId) {
        Map<String, Publisher> map = Maps.newHashMap();
        pubMap.values().forEach(p -> {
            if (p.isConnectId(connectId)) {
                map.put(p.publisher.getRegisterId(), p.publisher);
            }
        });
        return map;
    }

    private DatumVersion updateVersion() {
        this.version = DatumVersionUtil.nextId();
        return new DatumVersion(version);
    }

    private boolean tryAddPublisher(Publisher publisher) {
        PublisherEnvelope exist = pubMap.get(publisher.getRegisterId());
        if (exist != null && !exist.publisherVersion.orderThan(publisher.publisherVersion())) {
            LOGGER.warn("[AddOlderVer] {}, exist={}, add={}", publisher.getRegisterId(),
                exist.publisherVersion, publisher.publisherVersion());
            return false;
        }
        pubMap.put(publisher.getRegisterId(), PublisherEnvelope.of(publisher));
        return true;
    }

    DatumVersion addPublisher(Publisher publisher, ProcessId sessionProcessId) {
        lock.lock();
        try {
            if (tryAddPublisher(publisher)) {
                return updateVersion();
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    DatumVersion clean(ProcessId sessionProcessId) {
        lock.lock();
        try {
            boolean modified = false;
            if (sessionProcessId == null) {
                modified = !pubMap.isEmpty();
                pubMap.clear();
            } else {
                Iterator<Map.Entry<String, PublisherEnvelope>> it = pubMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, PublisherEnvelope> e = it.next();
                    final String registerId = e.getKey();
                    PublisherEnvelope envelope = e.getValue();
                    if (envelope.isPub() && envelope.sessionProcessId.equals(sessionProcessId)) {
                        pubMap.put(registerId, PublisherEnvelope.unpubOf(
                            envelope.publisherVersion.incrRegisterTimestamp(), sessionProcessId));
                        it.remove();
                        modified = true;
                    }
                }
            }
            return modified ? updateVersion() : null;
        } finally {
            lock.unlock();
        }
    }

    DatumVersion remove(ConnectId connectId, ProcessId sessionProcessId, long registerTimestamp) {
        lock.lock();
        try {
            boolean modified = false;
            for (Map.Entry<String, PublisherEnvelope> e : pubMap.entrySet()) {
                PublisherEnvelope existing = e.getValue();
                if (existing.sessionProcessId.equals(sessionProcessId)
                    && existing.isConnectId(connectId)) {
                    // use concurrent version
                    PublisherVersion newVersion = PublisherVersion.of(
                        existing.publisherVersion.getVersion(), registerTimestamp);
                    if (existing.publisherVersion.orderThan(newVersion)) {
                        // mark unpub
                        pubMap.put(e.getKey(),
                            PublisherEnvelope.unpubOf(newVersion, sessionProcessId));
                        modified = true;
                    }
                }
            }
            return modified ? updateVersion() : null;
        } finally {
            lock.unlock();
        }
    }

    DatumVersion remove(ProcessId sessionProcessId, Map<String, PublisherVersion> removedPublishers) {
        if (MapUtils.isEmpty(removedPublishers)) {
            return null;
        }
        lock.lock();
        try {
            boolean modified = false;
            for (Map.Entry<String, PublisherVersion> e : removedPublishers.entrySet()) {
                final String registerId = e.getKey();
                final PublisherVersion removedVer = e.getValue();

                final PublisherEnvelope existing = pubMap.get(registerId);
                if (existing == null) {
                    // the removedPublishers is from pubMap, but now notExist/unpub/pubByOtherSession
                    continue;
                }
                if (existing.publisherVersion.equals(removedVer)) {
                    // sync from leader
                    if (sessionProcessId == null) {
                        pubMap.remove(registerId);
                        modified = true;
                        continue;
                    }
                    if (sessionProcessId.equals(existing.sessionProcessId)) {
                        // syn from session, mark unpub with higher registerTimestamp
                        pubMap.put(registerId, PublisherEnvelope.unpubOf(
                            removedVer.incrRegisterTimestamp(), sessionProcessId));
                        modified = true;
                    } else {
                        LOGGER.warn("[RemovePidModified] {}, exist={}/{}, expect={}/{}",
                            registerId, existing.publisherVersion, existing.sessionProcessId,
                            removedVer, sessionProcessId);
                    }
                } else {
                    // the item has modified after diff, ignored
                    LOGGER.warn("[RemoveVerModified] {}, exist={}, expect={}", registerId,
                        existing.publisherVersion, removedVer);
                }
            }
            return modified ? updateVersion() : null;
        } finally {
            lock.unlock();
        }
    }

    DatumVersion update(List<Publisher> updatedPublishers) {
        if (CollectionUtils.isEmpty(updatedPublishers)) {
            return null;
        }
        for (Publisher p : updatedPublishers) {
            ParaCheckUtil.checkNotNull(p.getSessionProcessId(), "publisher.sessionProcessId");
        }
        lock.lock();
        try {
            boolean modified = false;
            for (Publisher publisher : updatedPublishers) {
                if (tryAddPublisher(publisher)) {
                    modified = true;
                }
            }
            if (modified) {
                return updateVersion();
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    DatumSummary getSummary(String sessionIpAddress) {
        Map<String/*registerId*/, PublisherVersion> publisherVersions = new HashMap<>(64);
        for (Map.Entry<String, PublisherEnvelope> e : Maps.newHashMap(pubMap).entrySet()) {
            PublisherEnvelope envelope = e.getValue();
            PublisherVersion v = envelope.getVersionIfPub();
            if (v == null) {
                continue;
            }
            if (sessionIpAddress == null
                || sessionIpAddress.equals(envelope.sessionProcessId.getHostAddress())) {
                publisherVersions.put(e.getKey(), v);
            }
        }
        return new DatumSummary(dataInfoId, publisherVersions);
    }

    Collection<ProcessId> getSessionProcessIds() {
        Set<ProcessId> set = Sets.newHashSet();
        pubMap.values().forEach(p -> set.add(p.sessionProcessId));
        return set;
    }

    int compact(long tombstoneTimestamp) {
        int count = 0;
        lock.lock();
        try {
            Iterator<Map.Entry<String, PublisherEnvelope>> it = pubMap.entrySet().iterator();
            while (it.hasNext()) {
                PublisherEnvelope envelope = it.next().getValue();
                // compact the unpub
                if (!envelope.isPub()
                    && envelope.publisherVersion.getRegisterTimestamp() < tombstoneTimestamp) {
                    it.remove();
                    count++;
                }
            }
        } finally {
            lock.unlock();
        }
        return count;
    }
}