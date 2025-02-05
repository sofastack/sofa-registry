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
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : BaseDatumStorage.java, v 0.1 2022年05月12日 11:54 xiaojian.xj Exp $
 */
public class BaseDatumStorage {

  private final Logger logger;

  private final String dataCenter;

  private final SlotFunction slotFunction = SlotFunctionRegistry.getFunc();

  private final Map<Integer, PublisherGroups> publisherGroupsMap = Maps.newConcurrentMap();

  public BaseDatumStorage(String dataCenter, Logger logger) {
    this.dataCenter = dataCenter;
    this.logger = logger;
  }

  private PublisherGroups getPublisherGroups(String dataInfoId) {
    final Integer slotId = slotFunction.slotOf(dataInfoId);
    PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups == null) {
      logger.warn(
          "[nullGroups]dataCenter={},slotId={},dataInfoId={}", dataCenter, slotId, dataInfoId);
    }
    return groups;
  }

  private PublisherGroups getPublisherGroups(int slotId) {
    PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups == null) {
      logger.warn("[nullGroups]dataCenter={},slotId={}", dataCenter, slotId);
    }
    return groups;
  }

  public Datum get(String dataInfoId) {
    final PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.getDatum(dataInfoId);
  }

  public DatumVersion getVersion(String dataInfoId) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.getVersion(dataInfoId);
  }

  public Map<String, DatumVersion> getVersions(int slotId, Collection<String> targetDataInfoIds) {
    PublisherGroups groups = getPublisherGroups(slotId);
    return groups == null ? Collections.emptyMap() : groups.getVersions(targetDataInfoIds);
  }

  public Map<String, Datum> getAll() {
    Map<String, Datum> m = Maps.newHashMapWithExpectedSize(128);
    publisherGroupsMap.values().forEach(g -> m.putAll(g.getAllDatum()));
    return m;
  }

  public Map<String, List<Publisher>> getAllPublisher() {
    Map<String, List<Publisher>> m = Maps.newHashMapWithExpectedSize(128);
    publisherGroupsMap.values().forEach(g -> m.putAll(g.getAllPublisher()));
    return m;
  }

  public Map<String, Integer> getPubCount() {
    Map<String, Integer> map = Maps.newHashMapWithExpectedSize(128);
    publisherGroupsMap.values().forEach(g -> map.putAll(g.getPubCount()));
    return map;
  }

  public Map<String, Publisher> getByConnectId(ConnectId connectId) {
    Map<String, Publisher> m = Maps.newHashMapWithExpectedSize(64);
    publisherGroupsMap.values().forEach(g -> m.putAll(g.getByConnectId(connectId)));
    return m;
  }

  public Map<String, Map<String, Publisher>> getPublishers(int slotId) {

    PublisherGroups groups = getPublisherGroups(slotId);
    if (groups == null) {
      return Collections.emptyMap();
    }
    Map<String, List<Publisher>> publisherMap = groups.getAllPublisher();
    Map<String, Map<String, Publisher>> ret = Maps.newHashMapWithExpectedSize(publisherMap.size());
    for (Map.Entry<String, List<Publisher>> publishers : publisherMap.entrySet()) {
      final String dataInfoId = publishers.getKey();

      final List<Publisher> list = publishers.getValue();

      if (list.isEmpty()) {
        logger.warn(
            "[emptyPubs]dataCenter={},slotId={},dataInfoId={}", dataCenter, slotId, dataInfoId);
      }
      // only copy the non empty publishers
      Map<String, Publisher> map =
          ret.computeIfAbsent(dataInfoId, k -> Maps.newHashMapWithExpectedSize(list.size()));
      for (Publisher p : list) {
        map.put(p.getRegisterId(), p);
      }
    }
    return ret;
  }

  public DatumVersion createEmptyDatumIfAbsent(String dataInfoId) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.createGroupIfAbsent(dataInfoId).getVersion();
  }

  public Map<String, DatumVersion> clean(
      int slotId, ProcessId sessionProcessId, CleanContinues cleanContinues) {
    // clean by sessionProcessId, the sessionProcessId could not be null
    ParaCheckUtil.checkNotNull(sessionProcessId, "sessionProcessId");
    PublisherGroups groups = getPublisherGroups(slotId);
    if (groups == null) {
      return Collections.emptyMap();
    }
    return groups.clean(sessionProcessId, cleanContinues);
  }

  // only for http testapi
  public DatumVersion removePublishers(String dataInfoId, ProcessId sessionProcessId) {
    // the sessionProcessId is null when the call from sync leader
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.remove(dataInfoId, sessionProcessId);
  }

  public DatumVersion putPublisher(String dataInfoId, List<Publisher> publishers) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.put(dataInfoId, publishers);
  }

  public DatumVersion putPublisher(Publisher publisher) {
    return putPublisher(publisher.getDataInfoId(), Collections.singletonList(publisher));
  }

  public DatumVersion removePublishers(
      String dataInfoId,
      ProcessId sessionProcessId,
      Map<String, RegisterVersion> removedPublishers) {
    // the sessionProcessId is null when the call from sync leader
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.remove(dataInfoId, sessionProcessId, removedPublishers);
  }

  public void foreach(int slotId, BiConsumer<String, PublisherGroup> f) {
    final PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups != null) {
      groups.foreach(f);
    }
  }

  public Map<String, DatumVersion> updateVersion(int slotId) {
    PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups == null) {
      return Collections.emptyMap();
    }
    return groups.updateVersion();
  }

  public DatumVersion updateVersion(String dataInfoId) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.updateVersion(dataInfoId);
  }

  public Set<ProcessId> getSessionProcessIds() {
    Set<ProcessId> ids = Sets.newHashSet();
    publisherGroupsMap.values().forEach(g -> ids.addAll(g.getSessionProcessIds()));
    return ids;
  }

  public Map<String, Integer> compact(long tombstoneTimestamp) {
    Map<String, Integer> compacts = Maps.newHashMap();
    publisherGroupsMap.values().forEach(g -> compacts.putAll(g.compact(tombstoneTimestamp)));
    return compacts;
  }

  public void putPublisherGroups(int slotId) {
    publisherGroupsMap.computeIfAbsent(
        slotId,
        k -> {
          PublisherGroups groups = new PublisherGroups(dataCenter);
          logger.info(
              "{} add publisherGroup {}, role={}, slotNum={}",
              dataCenter,
              slotId,
              publisherGroupsMap.size());
          return groups;
        });
  }

  public boolean removePublisherGroups(int slotId) {
    boolean removed = publisherGroupsMap.remove(slotId) != null;
    logger.info(
        "{}, remove publisherGroup {}, removed={}, slotNum={}",
        dataCenter,
        slotId,
        removed,
        publisherGroupsMap.size());
    return removed;
  }

  public int tombstoneNum() {
    int count = 0;
    for (PublisherGroups groups : publisherGroupsMap.values()) {
      count += groups.tombstoneNum();
    }
    return count;
  }

  public DatumVersion clearPublishers(String dataInfoId) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.clearPublishers(dataInfoId);
  }

  public Map<String, DatumVersion> clearGroupPublishers(String group) {
    Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(1024);
    for (PublisherGroups publisherGroups : publisherGroupsMap.values()) {
      Map<String, DatumVersion> datumVersions = publisherGroups.clearGroupPublishers(group);
      if (!CollectionUtils.isEmpty(datumVersions)) {
        ret.putAll(datumVersions);
      }
    }
    return ret;
  }
}
