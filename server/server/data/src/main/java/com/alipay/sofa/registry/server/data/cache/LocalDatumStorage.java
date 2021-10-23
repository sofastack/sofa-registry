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
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.slot.SlotChangeListener;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 19:40 yuzhi.lyz Exp $
 */
public final class LocalDatumStorage implements DatumStorage {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalDatumStorage.class);

  private final SlotFunction slotFunction = SlotFunctionRegistry.getFunc();
  private final Map<Integer, PublisherGroups> publisherGroupsMap = Maps.newConcurrentMap();

  @Autowired private DataServerConfig dataServerConfig;

  private PublisherGroups getPublisherGroups(String dataInfoId) {
    final Integer slotId = slotFunction.slotOf(dataInfoId);
    PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups == null) {
      LOGGER.warn("[nullGroups] {}, {}", slotId, dataInfoId);
    }
    return groups;
  }

  private PublisherGroups getPublisherGroups(int slotId) {
    PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups == null) {
      LOGGER.warn("[nullGroups] {}", slotId);
    }
    return groups;
  }

  @Override
  public Datum get(String dataInfoId) {
    final PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.getDatum(dataInfoId);
  }

  @Override
  public DatumVersion getVersion(String dataInfoId) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.getVersion(dataInfoId);
  }

  @Override
  public Map<String, DatumVersion> getVersions(int slotId, Collection<String> targetDataInfoIds) {
    PublisherGroups groups = getPublisherGroups(slotId);
    return groups == null ? Collections.emptyMap() : groups.getVersions(targetDataInfoIds);
  }

  @Override
  public Map<String, Datum> getAll() {
    Map<String, Datum> m = Maps.newHashMapWithExpectedSize(128);
    publisherGroupsMap.values().forEach(g -> m.putAll(g.getAllDatum()));
    return m;
  }

  @Override
  public Map<String, List<Publisher>> getAllPublisher() {
    Map<String, List<Publisher>> m = Maps.newHashMapWithExpectedSize(128);
    publisherGroupsMap.values().forEach(g -> m.putAll(g.getAllPublisher()));
    return m;
  }

  @Override
  public Map<String, Integer> getPubCount() {
    Map<String, Integer> map = Maps.newHashMapWithExpectedSize(128);
    publisherGroupsMap.values().forEach(g -> map.putAll(g.getPubCount()));
    return map;
  }

  @Override
  public Map<String, Publisher> getByConnectId(ConnectId connectId) {
    Map<String, Publisher> m = Maps.newHashMapWithExpectedSize(64);
    publisherGroupsMap.values().forEach(g -> m.putAll(g.getByConnectId(connectId)));
    return m;
  }

  @Override
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
      // only copy the non empty publishers
      if (!list.isEmpty()) {
        Map<String, Publisher> map =
            ret.computeIfAbsent(dataInfoId, k -> Maps.newHashMapWithExpectedSize(list.size()));
        for (Publisher p : list) {
          map.put(p.getRegisterId(), p);
        }
      }
    }
    return ret;
  }

  @Override
  public DatumVersion createEmptyDatumIfAbsent(String dataInfoId, String dataCenter) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.createGroupIfAbsent(dataInfoId).getVersion();
  }

  @Override
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
  @Override
  public DatumVersion remove(String dataInfoId, ProcessId sessionProcessId) {
    // the sessionProcessId is null when the call from sync leader
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.remove(dataInfoId, sessionProcessId);
  }

  @Override
  public DatumVersion put(String dataInfoId, List<Publisher> publishers) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.put(dataInfoId, publishers);
  }

  @Override
  public DatumVersion put(Publisher publisher) {
    return put(publisher.getDataInfoId(), Collections.singletonList(publisher));
  }

  @Override
  public DatumVersion remove(
      String dataInfoId,
      ProcessId sessionProcessId,
      Map<String, RegisterVersion> removedPublishers) {
    // the sessionProcessId is null when the call from sync leader
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.remove(dataInfoId, sessionProcessId, removedPublishers);
  }

  @Override
  public Map<String, Map<String, DatumSummary>> getDatumSummary(int slotId, Set<String> sessions) {
    final PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups != null) {
      return groups.getSummary(sessions);
    }

    if (CollectionUtils.isEmpty(sessions)) {
      return Collections.emptyMap();
    }

    Map<String /*sessionIp*/, Map<String /*dataInfoId*/, DatumSummary>> summaries =
        Maps.newHashMapWithExpectedSize(sessions.size());
    for (String sessionIp : sessions) {
      summaries.put(sessionIp, Collections.emptyMap());
    }
    return summaries;
  }

  @Override
  public Map<String, DatumSummary> getDatumSummary(int slotId) {
    final PublisherGroups groups = publisherGroupsMap.get(slotId);
    return groups != null ? groups.getAllSummary() : Collections.emptyMap();
  }

  @Override
  public SlotChangeListener getSlotChangeListener() {
    return new SlotListener();
  }

  @Override
  public Set<ProcessId> getSessionProcessIds() {
    Set<ProcessId> ids = Sets.newHashSet();
    publisherGroupsMap.values().forEach(g -> ids.addAll(g.getSessionProcessIds()));
    return ids;
  }

  @Override
  public Map<String, Integer> compact(long tombstoneTimestamp) {
    Map<String, Integer> compacts = Maps.newHashMap();
    publisherGroupsMap.values().forEach(g -> compacts.putAll(g.compact(tombstoneTimestamp)));
    return compacts;
  }

  @Override
  public int tombstoneNum() {
    int count = 0;
    for (PublisherGroups groups : publisherGroupsMap.values()) {
      count += groups.tombstoneNum();
    }
    return count;
  }

  @Override
  public Map<String, DatumVersion> updateVersion(int slotId) {
    PublisherGroups groups = publisherGroupsMap.get(slotId);
    if (groups == null) {
      return Collections.emptyMap();
    }
    return groups.updateVersion();
  }

  @Override
  public DatumVersion updateVersion(String dataInfoId) {
    PublisherGroups groups = getPublisherGroups(dataInfoId);
    return groups == null ? null : groups.updateVersion(dataInfoId);
  }

  private final class SlotListener implements SlotChangeListener {

    @Override
    public void onSlotAdd(int slotId, Slot.Role role) {
      publisherGroupsMap.computeIfAbsent(
          slotId,
          k -> {
            PublisherGroups groups = new PublisherGroups(dataServerConfig.getLocalDataCenter());
            LOGGER.info(
                "{} add publisherGroup {}, role={}, slotNum={}",
                dataServerConfig.getLocalDataCenter(),
                slotId,
                role,
                publisherGroupsMap.size());
            return groups;
          });
    }

    @Override
    public void onSlotRemove(int slotId, Slot.Role role) {
      boolean removed = publisherGroupsMap.remove(slotId) != null;
      LOGGER.info(
          "{}, remove publisherGroup {}, removed={}, role={}, slotNum={}",
          dataServerConfig.getLocalDataCenter(),
          slotId,
          removed,
          role,
          publisherGroupsMap.size());
    }
  }

  @VisibleForTesting
  public void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }

  @VisibleForTesting
  public DataServerConfig getDataServerConfig() {
    return dataServerConfig;
  }
}
