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

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultClientManagerService.java, v 0.1 2021年05月12日 15:16 xiaojian.xj Exp $
 */
public class DefaultClientManagerService implements ClientManagerService {

  @Autowired private ClientManagerAddressRepository clientManagerAddressRepository;

  /**
   * client open
   *
   * @param ipSet
   * @return
   */
  @Override
  public boolean clientOpen(Set<String> ipSet) {
    return clientManagerAddressRepository.clientOpen(ipSet);
  }

  /**
   * client off
   *
   * @param ipSet
   * @return
   */
  @Override
  public boolean clientOff(Set<String> ipSet) {
    return clientManagerAddressRepository.clientOff(ipSet);
  }

  /**
   * query client off ips
   *
   * @return
   */
  @Override
  public DBResponse<ProvideData> queryClientOffSet() {

    ClientManagerAddress address = clientManagerAddressRepository.queryClientOffData();
    if (address.getVersion() == 0) {
      return DBResponse.notfound().build();
    }

    ProvideData provideData =
        new ProvideData(
            new ServerDataBox(address.getClientOffAddress()),
            ValueConstants.CLIENT_OFF_ADDRESS_DATA_ID,
            address.getVersion());
    return DBResponse.ok(provideData).build();
  }

  @Override
  public boolean reduce(Set<String> ipSet) {
    return clientManagerAddressRepository.reduce(ipSet);
  }

  @Override
  public void waitSynced() {
    clientManagerAddressRepository.waitSynced();
  }

  /**
   * Setter method for property <tt>clientManagerAddressRepository</tt>.
   *
   * @param clientManagerAddressRepository value to be assigned to property
   *     ClientManagerAddressRepository
   */
  @VisibleForTesting
  public void setClientManagerAddressRepository(
      ClientManagerAddressRepository clientManagerAddressRepository) {
    this.clientManagerAddressRepository = clientManagerAddressRepository;
  }
}
