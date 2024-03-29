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

import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : RecoverConfigRepository.java, v 0.1 2021年09月22日 20:17 xiaojian.xj Exp $
 */
public interface RecoverConfigRepository {

  /**
   * query recoverConfig
   *
   * @param propertyTable propertyTable
   * @return Set
   */
  public Set<String> queryKey(String propertyTable);

  /**
   * insert data
   *
   * @param propertyTable propertyTable
   * @param propertyKey propertyKey
   * @param recoverClusterId recoverClusterId
   * @return boolean
   */
  public boolean save(String propertyTable, String propertyKey, String recoverClusterId);

  /**
   * delete data
   *
   * @param propertyTable propertyTable
   * @param propertyKey propertyKey
   * @return boolean
   */
  public boolean remove(String propertyTable, String propertyKey);

  void waitSynced();

  public void registerCallback(RecoverConfig config);
}
