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
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.slot.SlotChangeListener;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 19:40 yuzhi.lyz Exp $
 */
public final class LocalDatumStorage implements DatumStorage {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(LocalDatumStorage.class, "[LocalStorage]");

  private final String dataCenter;

  private final BaseDatumStorage storage;

  public LocalDatumStorage(String dataCenter) {
    this.dataCenter = dataCenter;
    this.storage = new BaseDatumStorage(dataCenter, LOGGER);
  }

  @Override
  public Set<String> allDataCenters() {
    return Collections.singleton(dataCenter);
  }

  @Override
  public Datum get(String dataCenter, String dataInfoId) {
    return storage.get(dataInfoId);
  }

  @Override
  public DatumVersion getVersion(String dataCenter, String dataInfoId) {
    return storage.getVersion(dataInfoId);
  }

  @Override
  public Map<String, DatumVersion> getVersions(
      String dataCenter, int slotId, Collection<String> targetDataInfoIds) {
    return storage.getVersions(slotId, targetDataInfoIds);
  }

  @Override
  public Map<String, Datum> getAll(String dataCenter) {
    return storage.getAll();
  }

  @Override
  public Map<String, List<Publisher>> getAllPublisher(String dataCenter) {
    return storage.getAllPublisher();
  }

  @Override
  public Map<String, Integer> getPubCount(String dataCenter) {
    return storage.getPubCount();
  }

  @Override
  public void putPublisherGroups(String dataCenter, int slotId) {
    storage.putPublisherGroups(slotId);
  }

  @Override
  public Map<String, Publisher> getByConnectId(ConnectId connectId) {
    return storage.getByConnectId(connectId);
  }

  @Override
  public Map<String, Map<String, Publisher>> getPublishers(String dataCenter, int slotId) {
    return storage.getPublishers(slotId);
  }

  @Override
  public DatumVersion createEmptyDatumIfAbsent(String dataCenter, String dataInfoId) {
    return storage.createEmptyDatumIfAbsent(dataInfoId);
  }

  @Override
  public Map<String, DatumVersion> cleanBySessionId(
      String dataCenter, int slotId, ProcessId sessionProcessId, CleanContinues cleanContinues) {
    return storage.clean(slotId, sessionProcessId, cleanContinues);
  }

  @Override
  public boolean removePublisherGroups(String dataCenter, int slotId) {
    return storage.removePublisherGroups(slotId);
  }

  // only for http testapi
  @Override
  public DatumVersion removePublishers(
      String dataCenter, String dataInfoId, ProcessId sessionProcessId) {
    return storage.removePublishers(dataInfoId, sessionProcessId);
  }

  @Override
  public DatumVersion putPublisher(
      String dataCenter, String dataInfoId, List<Publisher> publishers) {
    return storage.putPublisher(dataInfoId, publishers);
  }

  @Override
  public DatumVersion putPublisher(String dataCenter, Publisher publisher) {
    return putPublisher(
        dataCenter, publisher.getDataInfoId(), Collections.singletonList(publisher));
  }

  @Override
  public DatumVersion removePublishers(
      String dataCenter,
      String dataInfoId,
      ProcessId sessionProcessId,
      Map<String, RegisterVersion> removedPublishers) {
    return storage.removePublishers(dataInfoId, sessionProcessId, removedPublishers);
  }

  @Override
  public void foreach(String dataCenter, int slotId, BiConsumer f) {
    storage.foreach(slotId, f);
  }

  @Override
  public SlotChangeListener getSlotChangeListener(boolean localDataCenter) {
    return new SlotListener();
  }

  @Override
  public Set<ProcessId> getSessionProcessIds(String dataCenter) {
    return storage.getSessionProcessIds();
  }

  @Override
  public Map<String, Integer> compact(String dataCenter, long tombstoneTimestamp) {
    return storage.compact(tombstoneTimestamp);
  }

  @Override
  public int tombstoneNum(String dataCenter) {
    return storage.tombstoneNum();
  }

  @Override
  public Map<String, DatumVersion> updateVersion(String dataCenter, int slotId) {
    return storage.updateVersion(slotId);
  }

  @Override
  public DatumVersion updateVersion(String dataCenter, String dataInfoId) {
    return storage.updateVersion(dataInfoId);
  }

  @Override
  public boolean removeStorage(String dataCenter) {
    throw new UnsupportedOperationException("local datum remove is unsupported");
  }

  @Override
  public DatumVersion clearPublishers(String dataCenter, String dataInfoId) {
    throw new UnsupportedOperationException("local datum clearPublishers is unsupported");
  }

  @Override
  public Map<String, DatumVersion> clearGroupPublishers(String dataCenter, String group) {
    throw new UnsupportedOperationException("local datum clearGroupPublishers is unsupported");
  }

  private final class SlotListener implements SlotChangeListener {

    @Override
    public void onSlotAdd(String dataCenter, int slotId, Slot.Role role) {
      putPublisherGroups(dataCenter, slotId);
      LOGGER.info("{} add publisherGroup {}, role={},", dataCenter, slotId, role);
    }

    @Override
    public void onSlotRemove(String dataCenter, int slotId, Slot.Role role) {
      boolean removed = removePublisherGroups(dataCenter, slotId);
      LOGGER.info(
          "{}, remove publisherGroup {}, removed={}, role={}", dataCenter, slotId, removed, role);
    }
  }
}
