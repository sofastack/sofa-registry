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

import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.meta.AbstractH2DbTestBase;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Date;
import java.util.List;
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

    Thread.sleep(2000);
    DBResponse<ProvideData> clientOffResponse = clientManagerService.queryClientOffSet();
    Assert.assertEquals(clientOffResponse.getOperationStatus(), OperationStatus.SUCCESS);
    ProvideData clientOffData = clientOffResponse.getEntity();
    Long v1 = clientOffData.getVersion();
    Set<String> set1 = (Set<String>) clientOffData.getProvideData().getObject();
    Assert.assertTrue(v1 > -1L);
    Assert.assertEquals(clientOffSet, set1);

    clientManagerService.clientOpen(clientOpenSet);
    Thread.sleep(2000);
    DBResponse<ProvideData> clientOpenResponse = clientManagerService.queryClientOffSet();
    Assert.assertEquals(clientOpenResponse.getOperationStatus(), OperationStatus.SUCCESS);
    ProvideData clientOpenData = clientOpenResponse.getEntity();
    Long v2 = clientOpenData.getVersion();
    Set<String> set2 = (Set<String>) clientOpenData.getProvideData().getObject();
    Assert.assertTrue(v2 > v1);
    Assert.assertEquals(Sets.difference(clientOffSet, clientOpenSet), set2);

    /** check expire before clean */
    List<String> expireAddress = clientManagerAddressRepository.getExpireAddress(new Date(), 100);
    Assert.assertEquals(Sets.newHashSet(expireAddress), clientOpenSet);

    SetView<String> difference = Sets.difference(clientOffSet, clientOpenSet);
    int expireClientOffSize = clientManagerAddressRepository.getClientOffSizeBefore(new Date());
    Assert.assertEquals(expireClientOffSize, difference.size());

    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);
    clientManagerService.start();
    Thread.sleep(2000);

    /** check expire after clean */
    expireAddress = clientManagerAddressRepository.getExpireAddress(new Date(), 100);
    Assert.assertEquals(0, expireAddress.size());

    expireClientOffSize = clientManagerAddressRepository.getClientOffSizeBefore(new Date());
    Assert.assertEquals(expireClientOffSize, difference.size());
  }
}
