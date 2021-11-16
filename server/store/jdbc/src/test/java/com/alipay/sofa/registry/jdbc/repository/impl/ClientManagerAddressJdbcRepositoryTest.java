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
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.domain.ClientManagerAddressDomain;
import com.alipay.sofa.registry.jdbc.mapper.ClientManagerAddressMapper;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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

  public static final Set<AddressVersion> clientOffSet =
      Sets.newHashSet(new AddressVersion("1.1.1.1", true), new AddressVersion("2.2.2.2", false));

  public static final Set<AddressVersion> clientOffWithSubSet =
      Sets.newHashSet(new AddressVersion("1.1.1.1", true), new AddressVersion("2.2.2.2", true));

  public static final Set<AddressVersion> clientOpenSet =
      Sets.newHashSet(new AddressVersion("2.2.2.2", true), new AddressVersion("3.3.3.3", true));

  public static final Set<AddressVersion> difference =
      Sets.newHashSet(new AddressVersion("1.1.1.1", true));

  private ClientManagerAddressMapper mapper = mock(ClientManagerAddressMapper.class);

  @Test
  public void testClientManagerWithSub() throws InterruptedException {
    boolean clientOff =
        clientManagerAddressJdbcRepository.clientOff(
            ClientManagerAddressJdbcRepositoryTest.clientOffSet);
    Assert.assertTrue(clientOff);
    TimeUnit.SECONDS.sleep(3);

    ClientManagerAddress query = clientManagerAddressJdbcRepository.queryClientOffData();
    for (AddressVersion addressVersion : clientOffSet) {
      AddressVersion queryVersion = query.getClientOffAddress().get(addressVersion.getAddress());
      Assert.assertEquals(addressVersion.isPub(), queryVersion.isPub());
      Assert.assertEquals(addressVersion.isSub(), queryVersion.isSub());
    }

    clientOff =
        clientManagerAddressJdbcRepository.clientOff(
            ClientManagerAddressJdbcRepositoryTest.clientOffWithSubSet);
    Assert.assertTrue(clientOff);
    TimeUnit.SECONDS.sleep(3);

    query = clientManagerAddressJdbcRepository.queryClientOffData();
    for (AddressVersion addressVersion : clientOffWithSubSet) {
      AddressVersion queryVersion = query.getClientOffAddress().get(addressVersion.getAddress());
      Assert.assertEquals(addressVersion.isPub(), queryVersion.isPub());
      Assert.assertEquals(addressVersion.isSub(), queryVersion.isSub());
    }

    clientOff =
        clientManagerAddressJdbcRepository.clientOff(
            ClientManagerAddressJdbcRepositoryTest.clientOffSet);
    Assert.assertTrue(clientOff);
    TimeUnit.SECONDS.sleep(3);

    query = clientManagerAddressJdbcRepository.queryClientOffData();
    for (AddressVersion addressVersion : clientOffSet) {
      AddressVersion queryVersion = query.getClientOffAddress().get(addressVersion.getAddress());
      Assert.assertEquals(addressVersion.isPub(), queryVersion.isPub());
      Assert.assertEquals(addressVersion.isSub(), queryVersion.isSub());
    }
  }

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
    Assert.assertEquals(
        query.getClientOffAddress().keySet(),
        difference.stream().map(AddressVersion::getAddress).collect(Collectors.toSet()));

    List<ClientManagerAddressDomain> clientManagerAddress =
        clientManagerAddressMapper.queryAfterThanByLimit(
            defaultCommonConfig.getClusterId(TableEnum.CLIENT_MANAGER_ADDRESS.getTableName()),
            -1L,
            100);

    Set<String> open =
        clientOpenSet.stream().map(AddressVersion::getAddress).collect(Collectors.toSet());
    for (ClientManagerAddressDomain address : clientManagerAddress) {
      if (open.contains(address.getAddress())) {
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
    Assert.assertEquals(
        Sets.newHashSet(expireAddress),
        clientOpenSet.stream().map(AddressVersion::getAddress).collect(Collectors.toSet()));

    int count = clientManagerAddressJdbcRepository.cleanExpired(expireAddress);
    Assert.assertEquals(count, expireAddress.size());
    expireAddress = clientManagerAddressJdbcRepository.getExpireAddress(new Date(), 100);
    Assert.assertEquals(0, expireAddress.size());

    int expireClientOffSize = clientManagerAddressJdbcRepository.getClientOffSizeBefore(new Date());
    Assert.assertEquals(expireClientOffSize, difference.size());
  }
}
