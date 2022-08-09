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

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.server.meta.AbstractH2DbTestBase;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerServiceTest.java, v 0.1 2021年05月31日 10:23 xiaojian.xj Exp $
 */
public class ClientManagerServiceTest extends AbstractH2DbTestBase {

  @Resource private DefaultClientManagerService clientManagerService;

  @Autowired private ClientManagerAddressRepository clientManagerAddressRepository;

  private MetaLeaderService metaLeaderService = Mockito.mock(MetaLeaderService.class);
  private MetaServerConfig metaServerConfig = Mockito.mock(MetaServerConfig.class);

  private final Set<String> clientOffSet = Sets.newHashSet("1.1.1.1", "2.2.2.2");
  private final Set<String> clientOpenSet = Sets.newHashSet("2.2.2.2", "3.3.3.3");

  @Test
  public void testClientManager() throws InterruptedException {

    when(metaLeaderService.amIStableAsLeader()).thenReturn(false);
    when(metaServerConfig.getClientManagerExpireDays()).thenReturn(0);
    when(metaServerConfig.getClientManagerCleanSecs()).thenReturn(1000);
    clientManagerService.metaLeaderService = metaLeaderService;
    clientManagerService.metaServerConfig = metaServerConfig;

    clientManagerService.clientOff(clientOffSet);

    Thread.sleep(4000);
    DBResponse<ClientManagerAddress> clientOffResponse =
        clientManagerService.queryClientOffAddress();
    Assert.assertEquals(clientOffResponse.getOperationStatus(), OperationStatus.SUCCESS);
    ClientManagerAddress clientOffData = clientOffResponse.getEntity();
    Long v1 = clientOffData.getVersion();

    Assert.assertTrue(v1 > -1L);
    Assert.assertEquals(clientOffSet, clientOffData.getClientOffAddress().keySet());
    for (Entry<String, AddressVersion> entry : clientOffData.getClientOffAddress().entrySet()) {
      Assert.assertTrue(entry.getValue().isPub());
      Assert.assertTrue(entry.getValue().isSub());
    }

    Set<AddressVersion> address = Sets.newHashSet();
    for (String s : clientOffSet) {
      address.add(new AddressVersion(s, false));
    }
    clientManagerService.clientOffWithSub(address);

    Thread.sleep(4000);
    clientOffResponse = clientManagerService.queryClientOffAddress();
    Assert.assertEquals(clientOffResponse.getOperationStatus(), OperationStatus.SUCCESS);
    clientOffData = clientOffResponse.getEntity();
    v1 = clientOffData.getVersion();

    Assert.assertTrue(v1 > -1L);
    Assert.assertEquals(clientOffSet, clientOffData.getClientOffAddress().keySet());
    for (Entry<String, AddressVersion> entry : clientOffData.getClientOffAddress().entrySet()) {
      Assert.assertTrue(entry.getValue().isPub());
      Assert.assertFalse(entry.getValue().isSub());
    }

    address = Sets.newHashSet(new AddressVersion("4.4.4.4", false));
    clientOffSet.add("4.4.4.4");
    clientManagerService.clientOffWithSub(address);

    Thread.sleep(4000);
    clientOffResponse = clientManagerService.queryClientOffAddress();
    Assert.assertEquals(clientOffResponse.getOperationStatus(), OperationStatus.SUCCESS);
    clientOffData = clientOffResponse.getEntity();
    v1 = clientOffData.getVersion();

    Assert.assertTrue(v1 > -1L);
    AddressVersion addressVersion = clientOffData.getClientOffAddress().get("4.4.4.4");
    Assert.assertTrue(addressVersion.isPub());
    Assert.assertFalse(addressVersion.isSub());

    clientManagerService.clientOff(Collections.singleton("4.4.4.4"));
    Thread.sleep(4000);
    clientOffResponse = clientManagerService.queryClientOffAddress();
    Assert.assertEquals(clientOffResponse.getOperationStatus(), OperationStatus.SUCCESS);
    clientOffData = clientOffResponse.getEntity();
    long pre = v1;
    v1 = clientOffData.getVersion();

    Assert.assertTrue(v1 > pre);
    addressVersion = clientOffData.getClientOffAddress().get("4.4.4.4");
    Assert.assertTrue(addressVersion.isPub());
    Assert.assertTrue(addressVersion.isSub());

    clientManagerService.clientOpen(clientOpenSet);
    Thread.sleep(4000);
    DBResponse<ClientManagerAddress> clientOpenResponse =
        clientManagerService.queryClientOffAddress();
    Assert.assertEquals(clientOpenResponse.getOperationStatus(), OperationStatus.SUCCESS);
    ClientManagerAddress clientOpenData = clientOpenResponse.getEntity();
    Long v2 = clientOpenData.getVersion();
    Assert.assertTrue(v2 > v1);
    Assert.assertEquals(
        Sets.difference(clientOffSet, clientOpenSet),
        clientOpenData.getClientOffAddress().keySet());

    /** check expire before clean */
    List<String> expireAddress = clientManagerAddressRepository.getExpireAddress(new Date(), 100);
    Assert.assertEquals(Sets.newHashSet(expireAddress), clientOpenSet);

    SetView<String> difference = Sets.difference(clientOffSet, clientOpenSet);
    int expireClientOffSize = clientManagerAddressRepository.getClientOffSizeBefore(new Date());
    Assert.assertEquals(expireClientOffSize, difference.size());

    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);
    clientManagerService.start();
    Thread.sleep(4000);

    /** check expire after clean */
    expireAddress = clientManagerAddressRepository.getExpireAddress(new Date(), 100);
    Assert.assertEquals(0, expireAddress.size());

    expireClientOffSize = clientManagerAddressRepository.getClientOffSizeBefore(new Date());
    Assert.assertEquals(expireClientOffSize, difference.size());
  }
}
