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
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : BaseClientManagerService.java, v 0.1 2022年01月20日 14:42 xiaojian.xj Exp $
 */
public class BaseClientManagerService implements ClientManagerService {

  @Autowired ClientManagerAddressRepository clientManagerAddressRepository;

  /**
   * client open
   *
   * @param ipSet
   * @return
   */
  @Override
  public ClientManagerResult clientOpen(Set<String> ipSet) {
    Set<AddressVersion> addressSet = buildDefaultAddressVersions(ipSet);
    if (CollectionUtils.isEmpty(addressSet)) {
      return ClientManagerResult.buildFailRet();
    }
    return clientManagerAddressRepository.clientOpen(addressSet);
  }

  /**
   * client off
   *
   * @param ipSet
   * @return
   */
  @Override
  public ClientManagerResult clientOff(Set<String> ipSet) {
    Set<AddressVersion> addressSet = buildDefaultAddressVersions(ipSet);
    if (CollectionUtils.isEmpty(addressSet)) {
      return ClientManagerResult.buildFailRet();
    }
    return clientManagerAddressRepository.clientOff(addressSet);
  }

  private Set<AddressVersion> buildDefaultAddressVersions(Set<String> ipSet) {
    if (CollectionUtils.isEmpty(ipSet)) {
      return Collections.EMPTY_SET;
    }
    Set<AddressVersion> addressSet = Sets.newHashSetWithExpectedSize(ipSet.size());
    for (String ip : ipSet) {
      addressSet.add(new AddressVersion(ip, true));
    }
    return addressSet;
  }

  @Override
  public ClientManagerResult clientOffWithSub(Set<AddressVersion> address) {
    return clientManagerAddressRepository.clientOff(address);
  }

  @Override
  public DBResponse<ClientManagerAddress> queryClientOffAddress() {
    ClientManagerAddress address = clientManagerAddressRepository.queryClientOffData();
    if (address.getVersion() == 0) {
      return DBResponse.notfound().build();
    }
    return DBResponse.ok(address).build();
  }

  @Override
  public ClientManagerResult reduce(Set<String> ipSet) {
    Set<AddressVersion> addressSet = buildDefaultAddressVersions(ipSet);
    if (CollectionUtils.isEmpty(addressSet)) {
      return ClientManagerResult.buildFailRet();
    }
    return clientManagerAddressRepository.reduce(addressSet);
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
