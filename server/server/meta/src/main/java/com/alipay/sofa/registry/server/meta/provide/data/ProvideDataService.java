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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.server.meta.MetaLeaderService.MetaLeaderElectorListener;
import com.alipay.sofa.registry.store.api.DBResponse;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataService.java, v 0.1 2021年04月06日 16:17 xiaojian.xj Exp $
 */
public interface ProvideDataService extends MetaLeaderElectorListener {

  /**
   * save or update provideData
   *
   * @param key
   * @param value
   * @return
   */
  boolean saveProvideData(String key, String value);

  /**
   * query provideData by key
   *
   * @param key
   * @return
   */
  DBResponse queryProvideData(String key);

  /**
   * delete provideData
   *
   * @param key
   * @return
   */
  boolean removeProvideData(String key);
}
