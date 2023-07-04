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
import com.alipay.sofa.registry.common.model.Tuple;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Session Data store manager,according base data function
 *
 * <p>Session Data struct
 *
 * <p>- DataInfo ID | - Publisher List | - Subscriber List
 *
 * @author shangyu.wh
 * @version $Id: DataManager.java, v 0.1 2017-11-30 17:57 shangyu.wh Exp $
 */
public interface DataManager<DATA, ID, DATAINFOID> {

  /**
   * new publisher and subscriber data add
   *
   * @param data data
   * @return boolean
   */
  boolean add(DATA data);

  /**
   * query data by client node connectId
   *
   * @param connectId connectId
   * @return Map
   */
  Map<ID, DATA> queryByConnectId(ConnectId connectId);

  /**
   * query data by client node connectId
   *
   * @param connectIds connectIds
   * @return Map
   */
  Map<ConnectId, Map<ID, DATA>> queryByConnectIds(Set<ConnectId> connectIds);

  /**
   * remove data by client node connectId
   *
   * @param connectId connectId
   * @return Map
   */
  Map<ID, DATA> deleteByConnectId(ConnectId connectId);

  /**
   * remove data by client node connectIds
   *
   * @param connectIds connectIds
   * @return Map
   */
  Map<ConnectId, Map<ID, DATA>> deleteByConnectIds(Set<ConnectId> connectIds);

  DATA queryById(ID registerId, DATAINFOID dataInfoId);

  /**
   * remove single data by register id
   *
   * @param registerId registerId
   * @param dataInfoId dataInfoId
   * @return Data
   */
  DATA deleteById(ID registerId, DATAINFOID dataInfoId);

  /**
   * dataInfoId.size and data.size
   *
   * @return Tuple
   */
  Tuple<Long, Long> count();

  Set<ConnectId> getConnectIds();

  Collection<DATA> getDatas(DATAINFOID dataInfoId);

  List<DATA> getDataList();

  Map<DATAINFOID, Map<ID, DATA>> getDatas();

  Collection<DATAINFOID> getDataInfoIds();

  void forEach(BiConsumer<DATAINFOID, Map<ID, DATA>> consumer);
}
