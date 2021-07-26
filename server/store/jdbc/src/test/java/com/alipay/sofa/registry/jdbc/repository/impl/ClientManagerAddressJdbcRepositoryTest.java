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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.domain.ClientManagerAddressDomain;
import com.alipay.sofa.registry.jdbc.mapper.ClientManagerAddressMapper;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressJdbcRepositoryTest.java, v 0.1 2021年05月29日 15:25 xiaojian.xj
 *     Exp $
 */
public class ClientManagerAddressJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Resource private ClientManagerAddressJdbcRepository clientManagerAddressJdbcRepository;

  @Autowired private ClientManagerAddressMapper clientManagerAddressMapper;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  public static final Set<String> clientOffSet = Sets.newHashSet("1.1.1.1", "2.2.2.2");
  public static final Set<String> clientOpenSet = Sets.newHashSet("2.2.2.2", "3.3.3.3");

  private ClientManagerAddressMapper mapper = mock(ClientManagerAddressMapper.class);

  @Test
  public void testClientManager() {
    boolean clientOff =
        clientManagerAddressJdbcRepository.clientOff(
            ClientManagerAddressJdbcRepositoryTest.clientOffSet);
    Assert.assertTrue(clientOff);

    boolean clientOpen =
        clientManagerAddressJdbcRepository.clientOpen(
            ClientManagerAddressJdbcRepositoryTest.clientOpenSet);
    Assert.assertTrue(clientOpen);

    clientManagerAddressJdbcRepository.waitSynced();

    ClientManagerAddress query = clientManagerAddressJdbcRepository.queryClientOffData();
    SetView<String> difference = Sets.difference(clientOffSet, clientOpenSet);
    Assert.assertEquals(query.getClientOffAddress().keySet(), difference);

    List<ClientManagerAddressDomain> clientManagerAddress =
        clientManagerAddressMapper.queryAfterThanByLimit(
            defaultCommonConfig.getClusterId(), -1L, 100);

    for (ClientManagerAddressDomain address : clientManagerAddress) {
      if (clientOpenSet.contains(address.getAddress())) {
        Assert.assertEquals(ValueConstants.CLIENT_OPEN, address.getOperation());
      } else {
        Assert.assertEquals(ValueConstants.CLIENT_OFF, address.getOperation());
      }
    }
  }

  @Test
  public void testClientManagerError() {
    clientManagerAddressJdbcRepository.setClientManagerAddressMapper(mapper);
    when(mapper.update(anyObject())).thenThrow(new RuntimeException("expected exception"));
    boolean clientOff =
        clientManagerAddressJdbcRepository.clientOff(
            ClientManagerAddressJdbcRepositoryTest.clientOffSet);
    Assert.assertTrue(!clientOff);

    boolean clientOpen =
        clientManagerAddressJdbcRepository.clientOpen(
            ClientManagerAddressJdbcRepositoryTest.clientOpenSet);
    Assert.assertTrue(!clientOpen);
  }

  @Test
  public void testExpired() {
    testClientManager();
    List<String> expireAddress =
        clientManagerAddressJdbcRepository.getExpireAddress(new Date(), 100);
    Assert.assertEquals(Sets.newHashSet(expireAddress), clientOpenSet);

    int count = clientManagerAddressJdbcRepository.cleanExpired(expireAddress);
    Assert.assertEquals(count, expireAddress.size());
    expireAddress = clientManagerAddressJdbcRepository.getExpireAddress(new Date(), 100);
    Assert.assertEquals(0, expireAddress.size());

    SetView<String> difference = Sets.difference(clientOffSet, clientOpenSet);
    int expireClientOffSize = clientManagerAddressJdbcRepository.getClientOffSizeBefore(new Date());
    Assert.assertEquals(expireClientOffSize, difference.size());
  }
}
