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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.server.session.store.engine.StoreEngine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/** Abstract Implementation of ClientStore. */
public abstract class AbstractClientStore<T extends StoreData<String>> implements ClientStore<T> {

  protected final StoreEngine<T> storeEngine;
  private final ConnectDataIndexer connectDataIndexer =
      new ConnectDataIndexer(getClass().getName());

  public AbstractClientStore(StoreEngine<T> storeEngine) {
    this.storeEngine = storeEngine;
  }

  @Override
  public boolean add(T storeData) {
    return connectDataIndexer.add(
        storeData.connectId(),
        new DataPos(storeData.getDataInfoId(), storeData.getId()),
        () -> storeEngine.putIfAbsent(storeData).getKey());
  }

  @Override
  public T get(String dataInfoId, String registerId) {
    return storeEngine.get(dataInfoId, registerId);
  }

  @Override
  public T delete(String dataInfoId, String registerId) {
    return storeEngine.delete(dataInfoId, registerId);
  }

  @Override
  public Collection<T> getByConnectId(ConnectId connectId) {
    List<T> result = new ArrayList<>();
    for (DataPos pos : connectDataIndexer.queryByKey(connectId)) {
      String dataInfoId = pos.getDataInfoId();
      String registerId = pos.getRegisterId();
      T storeData = storeEngine.get(dataInfoId, registerId);
      if (storeData != null) {
        result.add(storeData);
      }
    }
    return result;
  }

  @Override
  public Collection<T> getByDataInfoId(String dataInfoId) {
    return storeEngine.get(dataInfoId);
  }

  @Override
  public Collection<T> getAll() {
    return storeEngine.getAll();
  }

  @Override
  public Collection<String> getNonEmptyDataInfoId() {
    return storeEngine.getNonEmptyDataInfoId();
  }

  @Override
  public Collection<T> delete(ConnectId connectId) {
    List<T> result = new ArrayList<>();
    for (DataPos pos : connectDataIndexer.queryByKey(connectId)) {
      String dataInfoId = pos.getDataInfoId();
      String registerId = pos.getRegisterId();
      T storeData = storeEngine.get(dataInfoId, registerId);
      if (storeData == null || !storeData.connectId().equals(connectId)) {
        continue;
      }
      if (storeEngine.delete(storeData)) {
        result.add(storeData);
      }
    }
    return result;
  }

  @Override
  public Collection<ConnectId> getAllConnectId() {
    return connectDataIndexer.getKeys();
  }

  @Override
  public StoreEngine.StoreStat stat() {
    return storeEngine.stat();
  }

  static class ConnectDataIndexer extends DataIndexer<ConnectId, DataPos> {

    public ConnectDataIndexer(String name) {
      super(name);
    }

    @Override
    protected void dataStoreForEach(BiConsumer<ConnectId, DataPos> consumer) {}
  }
}
