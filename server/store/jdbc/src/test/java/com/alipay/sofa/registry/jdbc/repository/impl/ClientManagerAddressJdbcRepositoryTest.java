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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressJdbcRepositoryTest.java, v 0.1 2021年05月29日 15:25 xiaojian.xj
 *     Exp $
 */
public class ClientManagerAddressJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Autowired private ClientManagerAddressRepository clientManagerAddressRepository;

  public static final Set<String> clientOffSet = Sets.newHashSet("1.1.1.1", "2.2.2.2");
  public static final Set<String> clientOpenSet = Sets.newHashSet("2.2.2.2", "3.3.3.3");

  @Test
  public void testClientManager() {
    boolean clientOff =
        clientManagerAddressRepository.clientOff(
            ClientManagerAddressJdbcRepositoryTest.clientOffSet);
    Assert.assertTrue(clientOff);

    boolean clientOpen =
        clientManagerAddressRepository.clientOpen(
            ClientManagerAddressJdbcRepositoryTest.clientOpenSet);
    Assert.assertTrue(clientOpen);

    int total = clientManagerAddressRepository.queryTotalCount();
    Assert.assertEquals(3, total);

    List<ClientManagerAddress> clientManagerAddress =
        clientManagerAddressRepository.queryAfterThan(-1L);
    List<ClientManagerAddress> addresses = clientManagerAddressRepository.queryAfterThan(-1L, 1000);
    Assert.assertEquals(total, clientManagerAddress.size());
    Assert.assertEquals(total, addresses.size());

    for (ClientManagerAddress address : clientManagerAddress) {
      if (clientOpenSet.contains(address.getAddress())) {
        Assert.assertEquals(ValueConstants.CLIENT_OPEN, address.getOperation());
      } else {
        Assert.assertEquals(ValueConstants.CLIENT_OFF, address.getOperation());
      }
    }
  }
}
