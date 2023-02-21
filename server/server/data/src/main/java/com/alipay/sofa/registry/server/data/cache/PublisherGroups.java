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
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 21:52 yuzhi.lyz Exp $
 */
public final class PublisherGroups {
  private final Map<String, PublisherGroup> publisherGroupMap = Maps.newConcurrentMap();
  private final String dataCenter;

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

  Map<String, DatumVersion> getVersions(Collection<String> targetDataInfoIds) {
    if (CollectionUtils.isEmpty(targetDataInfoIds)) {
      final Map<String, DatumVersion> ret =
          Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
      publisherGroupMap.forEach((k, v) -> ret.put(k, v.getVersion()));
      return ret;
    }
    final Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(targetDataInfoIds.size());
    for (String dataInfoId : targetDataInfoIds) {
      PublisherGroup group = publisherGroupMap.get(dataInfoId);
      if (group != null) {
        ret.put(dataInfoId, group.getVersion());
      }
    }
    return ret;
  }

  Map<String, Datum> getAllDatum() {
    Map<String, Datum> map = Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
    publisherGroupMap.forEach(
        (k, v) -> {
          map.put(k, v.toDatum());
        });
    return map;
  }

  Map<String, List<Publisher>> getAllPublisher() {
    Map<String, List<Publisher>> map = Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
    publisherGroupMap.forEach(
        (k, v) -> {
          map.put(k, v.getPublishers());
        });
    return map;
  }

  Map<String, Integer> getPubCount() {
    Map<String, Integer> map = Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
    publisherGroupMap.forEach((k, v) -> map.put(k, v.pubMap.size()));
    return map;
  }

  Map<String, Publisher> getByConnectId(ConnectId connectId) {
    Map<String, Publisher> map = Maps.newHashMapWithExpectedSize(64);
    publisherGroupMap.values().forEach(v -> map.putAll(v.getByConnectId(connectId)));
    return map;
  }

  PublisherGroup createGroupIfAbsent(String dataInfoId) {
    return publisherGroupMap.computeIfAbsent(
        dataInfoId, k -> new PublisherGroup(dataInfoId, dataCenter));
  }

  Map<String, DatumVersion> clean(ProcessId sessionProcessId, CleanContinues cleanContinues) {
    Map<String, DatumVersion> versionMap = Maps.newHashMapWithExpectedSize(64);
    for (PublisherGroup g : publisherGroupMap.values()) {
      DatumVersion ver = g.clean(sessionProcessId, cleanContinues);
      if (ver != null) {
        versionMap.put(g.dataInfoId, ver);
      }
    }
    return versionMap;
  }

  DatumVersion remove(String dataInfoId, ProcessId sessionProcessId) {
    PublisherGroup group = publisherGroupMap.get(dataInfoId);
    return group == null ? null : group.clean(sessionProcessId, CleanContinues.ALWAYS);
  }

  DatumVersion put(String dataInfoId, List<Publisher> publishers) {
    if (CollectionUtils.isEmpty(publishers)) {
      return null;
    }
    PublisherGroup group = createGroupIfAbsent(dataInfoId);
    return group.put(publishers);
  }

  DatumVersion remove(
      String dataInfoId,
      ProcessId sessionProcessId,
      Map<String, RegisterVersion> removedPublishers) {
    PublisherGroup group = publisherGroupMap.get(dataInfoId);
    return group == null ? null : group.remove(sessionProcessId, removedPublishers);
  }

  void foreach(BiConsumer<String, PublisherGroup> f) {
    publisherGroupMap.forEach(f);
  }

  Set<ProcessId> getSessionProcessIds() {
    Set<ProcessId> ids = Sets.newHashSet();
    publisherGroupMap.values().forEach(g -> ids.addAll(g.getSessionProcessIds()));
    return ids;
  }

  Map<String, Integer> compact(long tombstoneTimestamp) {
    Map<String, Integer> compacts = Maps.newHashMap();
    publisherGroupMap
        .values()
        .forEach(
            g -> {
              int count = g.compact(tombstoneTimestamp);
              if (count != 0) {
                compacts.put(g.dataInfoId, count);
              }
            });
    return compacts;
  }

  int tombstoneNum() {
    int count = 0;
    for (PublisherGroup group : publisherGroupMap.values()) {
      count += group.tombstoneNum();
    }
    return count;
  }

  Map<String, DatumVersion> updateVersion() {
    Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(publisherGroupMap.size());
    for (PublisherGroup g : publisherGroupMap.values()) {
      DatumVersion v = g.updateVersion();
      if (v != null) {
        ret.put(g.dataInfoId, v);
      }
    }
    return ret;
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
    return StringFormatter.format("PubGroups{{},size={}}", dataCenter, publisherGroupMap.size());
  }

  public DatumVersion clearPublishers(String dataInfoId) {
    PublisherGroup publisherGroup = publisherGroupMap.get(dataInfoId);
    return publisherGroup == null ? null : publisherGroup.clearPublishers();
  }

  public Map<String, DatumVersion> clearGroupPublishers(String group) {
    Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(256);
    for (PublisherGroup publisherGroup : publisherGroupMap.values()) {
      if (!StringUtils.equals(DataInfo.valueOf(publisherGroup.dataInfoId).getGroup(), group)) {
        continue;
      }
      DatumVersion datumVersion = publisherGroup.clearPublishers();
      if (datumVersion != null) {
        ret.put(publisherGroup.dataInfoId, datumVersion);
      }
    }
    return ret;
  }
}
