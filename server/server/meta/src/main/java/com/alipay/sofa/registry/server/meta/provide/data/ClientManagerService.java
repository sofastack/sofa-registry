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

import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.meta.MetaLeaderService.MetaLeaderElectorListener;
import com.alipay.sofa.registry.store.api.DBResponse;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerService.java, v 0.1 2021年05月12日 15:19 xiaojian.xj Exp $
 */
public interface ClientManagerService extends MetaLeaderElectorListener {

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
   * query client off ips
   *
   * @return
   */
  DBResponse<ProvideData> queryClientOffSet();
}
