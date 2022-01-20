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
package com.alipay.sofa.registry.server.shared.client.manager;

import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerResult;
import com.alipay.sofa.registry.store.api.DBResponse;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : ClientManagerService.java, v 0.1 2022年01月20日 14:42 xiaojian.xj Exp $
 */
public interface ClientManagerService {

  /**
   * client open
   *
   * @param ipSet
   * @return
   */
  ClientManagerResult clientOpen(Set<String> ipSet);

  /**
   * client off
   *
   * @param ipSet
   * @return
   */
  ClientManagerResult clientOff(Set<String> ipSet);

  ClientManagerResult clientOffWithSub(Set<AddressVersion> address);

  /**
   * query client off ips
   *
   * @return
   */
  DBResponse<ClientManagerAddress> queryClientOffAddress();
  /**
   * reduce
   *
   * @param ipSet
   * @return
   */
  ClientManagerResult reduce(Set<String> ipSet);

  void waitSynced();
}
