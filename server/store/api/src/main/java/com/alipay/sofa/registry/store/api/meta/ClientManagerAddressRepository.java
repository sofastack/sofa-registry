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

import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerResult;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressRepository.java, v 0.1 2021年05月11日 16:47 xiaojian.xj Exp $
 */
public interface ClientManagerAddressRepository {

  /**
   * client open
   *
   * @param ipSet ipSet
   * @return ClientManagerResult
   */
  ClientManagerResult clientOpen(Set<AddressVersion> ipSet);

  /**
   * client off
   *
   * @param ipSet ipSet
   * @return ClientManagerResult
   */
  ClientManagerResult clientOff(Set<AddressVersion> ipSet);

  /**
   * reduce
   *
   * @param ipSet ipSet
   * @return ClientManagerResult
   */
  ClientManagerResult reduce(Set<AddressVersion> ipSet);

  ClientManagerAddress queryClientOffData();

  void waitSynced();

  List<String> getExpireAddress(Date date, int limit);

  int cleanExpired(List<String> expireAddress);

  int getClientOffSizeBefore(Date date);

  void wakeup();
}
