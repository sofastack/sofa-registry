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

import static com.alipay.sofa.registry.server.data.change.ChangeMetrics.SKIP_SAME_VALUE_COUNTER;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.ProcessIdCache;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 20:26 yuzhi.lyz Exp $
 */
public final class PublisherGroup {
  private static final Logger LOGGER = LoggerFactory.getLogger(PublisherGroup.class);

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  final String dataInfoId;

  final String dataCenter;

  final String dataId;

  final String instanceId;

  final String group;

  // if the delete publisher from session, mark unpub
  final Map<String /*registerId*/, PublisherEnvelope> pubMap = Maps.newConcurrentMap();

  private volatile long version;

  private static final int RECENT_VERSIONS_CAP = 10;

  private final ArrayDeque<Long> recentVersions = new ArrayDeque<>(RECENT_VERSIONS_CAP);

  PublisherGroup(String dataInfoId, String dataCenter) {
    DataInfo dataInfo = DataInfo.valueOf(dataInfoId);
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
    this.dataCenter = WordCache.getWordCache(dataCenter);
    this.dataId = WordCache.getWordCache(dataInfo.getDataId());
    this.instanceId = WordCache.getWordCache(dataInfo.getInstanceId());
    this.group = WordCache.getWordCache(dataInfo.getGroup());
    if (DatumVersionUtil.useConfregVersionGen()) {
      this.version = DatumVersionUtil.confregNextId(0);
    } else {
      this.version = DatumVersionUtil.nextId();
    }
  }

  DatumVersion getVersion() {
    return new DatumVersion(version);
  }

  Datum toDatum() {
    Datum datum = new Datum();
    datum.setDataCenter(dataCenter);
    datum.setDataId(dataId);
    datum.setDataInfoId(dataInfoId);
    datum.setGroup(group);
    datum.setInstanceId(instanceId);
    long ver;
    List<Publisher> list = new ArrayList<>(pubMap.size());
    lock.readLock().lock();
    datum.setRecentVersions(
        recentVersions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    try {
      ver = this.version;
      for (PublisherEnvelope envelope : pubMap.values()) {
        if (envelope.isPub()) {
          list.add(envelope.publisher);
        }
      }
    } finally {
      lock.readLock().unlock();
    }
    datum.setVersion(ver);
    list.forEach(datum::addPublisher);
    return datum;
  }

  List<Publisher> getPublishers() {
    List<Publisher> list = new ArrayList<>(pubMap.size());
    for (PublisherEnvelope envelope : pubMap.values()) {
      if (envelope.isPub()) {
        list.add(envelope.publisher);
      }
    }
    return list;
  }

  Map<String, Publisher> getByConnectId(ConnectId connectId) {
    Map<String, Publisher> map = Maps.newHashMap();
    for (PublisherEnvelope p : pubMap.values()) {
      if (p.isConnectId(connectId)) {
        map.put(p.publisher.getRegisterId(), p.publisher);
      }
    }
    return map;
  }

  DatumVersion updateVersion() {
    final boolean useConfreg = DatumVersionUtil.useConfregVersionGen();
    lock.writeLock().lock();
    try {
      long lastVersion = this.version;
      if (useConfreg) {
        this.version = DatumVersionUtil.confregNextId(lastVersion);
      } else {
        this.version = DatumVersionUtil.nextId();
      }
      appendRecentVersion(lastVersion);
      return new DatumVersion(version);
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void appendRecentVersion(long version) {
    for (int i = 0; recentVersions.size() >= RECENT_VERSIONS_CAP && i < 3; i++) {
      this.recentVersions.pollFirst();
    }
    this.recentVersions.addLast(version);
  }

  private boolean tryAddPublisher(Publisher publisher) {
    PublisherEnvelope exist = pubMap.get(publisher.getRegisterId());
    final RegisterVersion registerVersion = publisher.registerVersion();
    if (exist == null) {
      PublisherEnvelope envelope = PublisherEnvelope.of(publisher);
      pubMap.put(publisher.getRegisterId(), envelope);
      return envelope.isPub();
    }

    if (exist.registerVersion.equals(registerVersion)) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "[AddSameVer] {}, {}, exist={}, add={}",
            publisher.getDataInfoId(),
            publisher.getRegisterId(),
            exist.registerVersion,
            publisher.registerVersion());
      }
      return false;
    }
    if (!exist.registerVersion.orderThan(registerVersion)) {
      LOGGER.warn(
          "[AddOlderVer] {}, {}, exist={}, add={}",
          publisher.getDataInfoId(),
          publisher.getRegisterId(),
          exist.registerVersion,
          publisher.registerVersion());
      return false;
    }
    PublisherEnvelope envelope = PublisherEnvelope.of(publisher);
    pubMap.put(publisher.getRegisterId(), envelope);

    if (exist.publisher == null) {
      // publisher is null after client_off
      LOGGER.info(
          "[ReplaceEmptyPub] {}, {}, exist={}, add={}, regIsPub={}",
          publisher.getDataInfoId(),
          publisher.getRegisterId(),
          exist.registerVersion,
          publisher.registerVersion(),
          envelope.isPub());
      return envelope.isPub();
    }
    try {
      boolean same =
          exist.publisher.getDataList() == null
              ? publisher.getDataList() == null
              : exist.publisher.getDataList().equals(publisher.getDataList());
      if (same) {
        SKIP_SAME_VALUE_COUNTER.inc();
        LOGGER.info(
            "[SkipUpVer] {}, {}, exist={}, add={}",
            publisher.getDataInfoId(),
            publisher.getRegisterId(),
            exist.registerVersion,
            publisher.registerVersion());
      }
      return !same;
    } catch (Throwable t) {
      // unexpect run into here, if it happens,
      // return true to update version because pubMap has been put a newer version publish
      LOGGER.error(
          "[PubChangeJudgement]judge {}, {} change error, exist={}, add={}.",
          publisher.getDataInfoId(),
          publisher.getRegisterId(),
          exist.registerVersion,
          publisher.registerVersion(),
          t);
      return true;
    }
  }

  DatumVersion addPublisher(Publisher publisher) {
    publisher.setSessionProcessId(ProcessIdCache.cache(publisher.getSessionProcessId()));
    lock.writeLock().lock();
    try {
      if (tryAddPublisher(publisher)) {
        return updateVersion();
      }
      return null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  DatumVersion clean(ProcessId sessionProcessId, CleanContinues cleanContinues) {
    sessionProcessId = ProcessIdCache.cache(sessionProcessId);
    if (sessionProcessId == null) {
      // not check continues
      lock.writeLock().lock();
      try {
        final int size = pubMap.size();
        if (size == 0) {
          return null;
        }
        cleanContinues.onClean(size);
        pubMap.clear();
        return updateVersion();
      } finally {
        lock.writeLock().unlock();
      }
    }
    // collect the pub of the processId without lock
    Map<String, PublisherEnvelope> cleans = Maps.newHashMapWithExpectedSize(64);
    for (Map.Entry<String, PublisherEnvelope> pub : pubMap.entrySet()) {
      PublisherEnvelope envelope = pub.getValue();
      if (envelope.isPub() && envelope.sessionProcessId.equals(sessionProcessId)) {
        cleans.put(pub.getKey(), envelope);
      }
    }
    // clean modify the version, need to lock
    lock.writeLock().lock();
    try {
      boolean modified = false;
      for (Map.Entry<String, PublisherEnvelope> clean : cleans.entrySet()) {
        if (!cleanContinues.continues()) {
          break;
        }
        if (pubMap.remove(clean.getKey(), clean.getValue())) {
          cleanContinues.onClean(1);
          modified = true;
        }
      }
      return modified ? updateVersion() : null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  DatumVersion remove(ProcessId sessionProcessId, Map<String, RegisterVersion> removedPublishers) {
    sessionProcessId = ProcessIdCache.cache(sessionProcessId);
    if (MapUtils.isEmpty(removedPublishers)) {
      return null;
    }
    lock.writeLock().lock();
    try {
      boolean modified = false;
      for (Map.Entry<String, RegisterVersion> e : removedPublishers.entrySet()) {
        final String registerId = e.getKey();
        final RegisterVersion removedVer = e.getValue();

        final PublisherEnvelope existing = pubMap.get(registerId);
        if (existing == null || !existing.isPub()) {
          // the removedPublishers is from pubMap, but now notExist/unpub/pubByOtherSession
          continue;
        }
        // remove the existing <= removedVer
        if (existing.registerVersion.equals(removedVer)
            || existing.registerVersion.orderThan(removedVer)) {
          // sync from local-leader/remote-leader
          if (sessionProcessId == null) {
            pubMap.remove(registerId);
            modified = true;
            continue;
          }
          if (sessionProcessId.equals(existing.sessionProcessId)) {
            // syn from session, mark unpub with higher registerTimestamp
            pubMap.put(
                registerId,
                PublisherEnvelope.unpubOf(removedVer.incrRegisterTimestamp(), sessionProcessId));
            modified = true;
          } else {
            LOGGER.warn(
                "[RemovePidModified] {}, {}, exist={}/{}, expect={}/{}",
                dataInfoId,
                registerId,
                existing.registerVersion,
                existing.sessionProcessId,
                removedVer,
                sessionProcessId);
          }
        } else {
          // the item has modified after diff, ignored
          LOGGER.warn(
              "[RemoveVerModified] {}, {}, exist={}, expect={}",
              dataInfoId,
              registerId,
              existing.registerVersion,
              removedVer);
        }
      }
      return modified ? updateVersion() : null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  DatumVersion put(List<Publisher> puts) {
    for (Publisher p : puts) {
      ParaCheckUtil.checkNotNull(p.getSessionProcessId(), "publisher.sessionProcessId");
      ParaCheckUtil.checkEquals(p.getDataInfoId(), dataInfoId, "publisher.dataInfoId");
      p.setSessionProcessId(ProcessIdCache.cache(p.getSessionProcessId()));
    }
    lock.writeLock().lock();
    try {
      boolean modified = false;
      for (Publisher publisher : puts) {
        if (tryAddPublisher(publisher)) {
          modified = true;
        }
      }
      if (modified) {
        return updateVersion();
      }
      return null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  public int pubSize() {
    return pubMap.size();
  }

  public void foreach(BiConsumer<String /*registerId*/, PublisherEnvelope> f) {
    pubMap.forEach(f);
  }

  Collection<ProcessId> getSessionProcessIds() {
    Set<ProcessId> set = Sets.newHashSet();
    for (PublisherEnvelope e : pubMap.values()) {
      if (e.isPub()) {
        set.add(e.sessionProcessId);
      }
    }
    return set;
  }

  int compact(long tombstoneTimestamp) {
    // compact not modify the version, no need to lock
    int count = 0;
    Map<String, PublisherEnvelope> compacts = Maps.newHashMap();
    for (Map.Entry<String, PublisherEnvelope> e : pubMap.entrySet()) {
      final PublisherEnvelope envelope = e.getValue();
      if (!envelope.isPub() && envelope.tombstoneTimestamp <= tombstoneTimestamp) {
        compacts.put(e.getKey(), envelope);
      }
    }

    for (Map.Entry<String, PublisherEnvelope> compact : compacts.entrySet()) {
      if (pubMap.remove(compact.getKey(), compact.getValue())) {
        count++;
      }
    }
    return count;
  }

  int tombstoneNum() {
    int count = 0;
    for (PublisherEnvelope envelope : pubMap.values()) {
      if (!envelope.isPub()) {
        count++;
      }
    }
    return count;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "PubGroup{{},size={},ver={}}", dataInfoId, pubMap.size(), version);
  }

  public DatumVersion clearPublishers() {
    lock.writeLock().lock();
    try {
      if (pubSize() > 0) {
        pubMap.clear();
        return updateVersion();
      }
      return null;
    } finally {
      lock.writeLock().unlock();
    }
  }
}
