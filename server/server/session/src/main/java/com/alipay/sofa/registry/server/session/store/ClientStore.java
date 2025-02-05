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
import java.util.Collection;

/** Used to store client information. */
public interface ClientStore<T extends StoreData<String>> {

  /**
   * Add client.
   *
   * @param storeData client data
   * @return true if add successfully
   */
  boolean add(T storeData);

  /**
   * Get client data by dataInfoId and registerId.
   *
   * @param dataInfoId dataInfoId
   * @param registerId registerId
   * @return client data
   */
  T get(String dataInfoId, String registerId);

  /**
   * Delete client by dataInfoId and registerId.
   *
   * @param dataInfoId dataInfoId
   * @param registerId registerId
   * @return client deleted
   */
  T delete(String dataInfoId, String registerId);

  /**
   * Get client by dataInfoId.
   *
   * @param dataInfoId dataInfoId
   * @return client data
   */
  Collection<T> getByDataInfoId(String dataInfoId);

  /**
   * Get client by ConnectId.
   *
   * @param connectId ConnectId
   * @return client data
   */
  Collection<T> getByConnectId(ConnectId connectId);

  /**
   * Delete client data with ConnectId.
   *
   * @param connectId ConnectId
   * @return deleted client data
   */
  Collection<T> delete(ConnectId connectId);

  /**
   * Get all connectId.
   *
   * @return ConnectId collection
   */
  Collection<ConnectId> getAllConnectId();

  /**
   * Get all client data.
   *
   * @return all client data
   */
  Collection<T> getAll();

  /**
   * Get all dataInfoId which has non-empty client collection.
   *
   * @return dataInfo collection
   */
  Collection<String> getNonEmptyDataInfoId();

  /**
   * Get stat info.
   *
   * @return store stat info
   */
  StoreEngine.StoreStat stat();
}
