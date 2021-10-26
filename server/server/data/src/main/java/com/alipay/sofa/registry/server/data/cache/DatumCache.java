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
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * cache of datum, providing query function to the upper module
 *
 * @author kezhu.wukz
 * @author qian.lqlq
 * @version $Id: DatumCache.java, v 0.1 2017-12-06 20:50 qian.lqlq Exp $
 */
public class DatumCache {
  @Autowired private DatumStorage localDatumStorage;

  @Autowired private DataServerConfig dataServerConfig;

  /**
   * get datum by specific dataCenter and dataInfoId
   *
   * @param dataCenter
   * @param dataInfoId
   * @return
   */
  public Datum get(String dataCenter, String dataInfoId) {
    return localDatumStorage.get(dataInfoId);
  }

  public void clean(String dataCenter, String dataInfoId) {
    localDatumStorage.remove(dataInfoId, null);
  }

  public DatumVersion getVersion(String dataCenter, String dataInfoId) {
    // TODO only get local datacenter
    DatumVersion version = localDatumStorage.getVersion(dataInfoId);
    return version;
  }

  public Map<String, DatumVersion> getVersions(
      String dataCenter, int slotId, Collection<String> targetDataInfoIds) {
    // local
    return localDatumStorage.getVersions(slotId, targetDataInfoIds);
  }

  public DatumVersion updateVersion(String dataCenter, String dataInfoId) {
    return localDatumStorage.updateVersion(dataInfoId);
  }

  /**
   * get all datum
   *
   * @return
   */
  public Map<String, Map<String, Datum>> getAll() {
    Map<String, Map<String, Datum>> datumMap = new HashMap<>();
    datumMap.put(dataServerConfig.getLocalDataCenter(), localDatumStorage.getAll());
    return datumMap;
  }

  // get without datum.version, it's more efficient than getDatum
  public Map<String, Map<String, List<Publisher>>> getAllPublisher() {
    Map<String, Map<String, List<Publisher>>> datumMap = new HashMap<>();
    datumMap.put(dataServerConfig.getLocalDataCenter(), localDatumStorage.getAllPublisher());
    return datumMap;
  }

  public Map<String, Map<String, Integer>> getPubCount() {
    Map<String, Map<String, Integer>> map = Maps.newHashMap();
    map.put(dataServerConfig.getLocalDataCenter(), localDatumStorage.getPubCount());
    return map;
  }

  public Map<String, Publisher> getByConnectId(ConnectId connectId) {
    return localDatumStorage.getByConnectId(connectId);
  }

  @VisibleForTesting
  public void setLocalDatumStorage(DatumStorage localDatumStorage) {
    this.localDatumStorage = localDatumStorage;
  }

  @VisibleForTesting
  public void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }

  public DatumStorage getLocalDatumStorage() {
    return localDatumStorage;
  }
}
