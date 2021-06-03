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

import com.alipay.sofa.registry.common.model.metaserver.ClientManagerPods;
import java.util.List;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerPodsRepository.java, v 0.1 2021年05月11日 16:47 xiaojian.xj Exp $
 */
public interface ClientManagerPodsRepository {

  /**
   * client open
   *
   * @param ipSet
   * @return
   */
  boolean clientOpen(Set<String> ipSet);

  /**
   * client off
   *
   * @param ipSet
   * @return
   */
  boolean clientOff(Set<String> ipSet);

  /**
   * query records
   *
   * @param maxId
   * @return
   */
  List<ClientManagerPods> queryAfterThan(long maxId);

  /**
   * query records
   *
   * @param maxId
   * @return
   */
  List<ClientManagerPods> queryAfterThan(long maxId, long limit);

  int queryTotalCount();
}
