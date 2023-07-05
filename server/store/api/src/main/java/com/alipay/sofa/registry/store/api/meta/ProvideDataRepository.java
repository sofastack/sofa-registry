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
package com.alipay.sofa.registry.store.api.meta;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import java.util.Map;

/**
 * @author chen.zhu
 *     <p>Mar 09, 2021
 */
public interface ProvideDataRepository {

  /**
   * save or update provideData, such as stop_push_switch; directly use new value to override old
   * value;
   *
   * @param persistenceData persistenceData
   * @return boolean
   */
  boolean put(PersistenceData persistenceData);

  /**
   * save or update with cas, such as node_server_operate_list; it use previous value to produce new
   * value;
   *
   * @param persistenceData persistenceData
   * @param expectVersion expectVersion
   * @return boolean
   */
  boolean put(PersistenceData persistenceData, long expectVersion);

  /**
   * query provideData by key
   *
   * @param key key
   * @return PersistenceData
   */
  PersistenceData get(String key);

  /**
   * delete provideData
   *
   * @param key key
   * @return boolean
   */
  boolean remove(String key, long version);

  /**
   * query all provide data
   *
   * @return Map
   */
  Map<String, PersistenceData> getAll();
}
