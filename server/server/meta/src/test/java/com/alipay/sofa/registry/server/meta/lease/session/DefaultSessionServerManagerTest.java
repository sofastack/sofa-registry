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
package com.alipay.sofa.registry.server.meta.lease.session;

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultSessionServerManagerTest extends AbstractMetaServerTestBase {

  private DefaultSessionServerManager sessionManager;

  @Mock private MetaServerConfig metaServerConfig;

  @Before
  public void beforeDefaultSessionManagerTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    sessionManager =
        new DefaultSessionServerManager() {
          @Override
          protected boolean amILeader() {
            return true;
          }
        };
    //        SessionLeaseManager sessionLeaseManager = new SessionLeaseManager();
    sessionManager.setMetaServerConfig(metaServerConfig)
    //            .setSessionLeaseManager(sessionLeaseManager)
    //            .setRaftSessionLeaseManager(sessionLeaseManager)
    ;
    when(metaServerConfig.getExpireCheckIntervalMilli()).thenReturn(60);
    sessionManager.postConstruct();
  }

  @After
  public void afterDefaultSessionManagerTest() throws Exception {
    sessionManager.preDestory();
  }

  @Test
  public void testGetEpoch() throws TimeoutException, InterruptedException {
    Assert.assertEquals(0, sessionManager.getEpoch());
    sessionManager.renew(new SessionNode(randomURL(randomIp()), getDc()), 1000);
    waitConditionUntilTimeOut(() -> sessionManager.getEpoch() > 0, 100);
    Assert.assertNotEquals(0, sessionManager.getEpoch());
  }

  @Test
  public void testGetClusterMembers() {
    Assert.assertTrue(sessionManager.getSessionServerMetaInfo().getClusterMembers().isEmpty());
  }

  @Test
  public void testRenew() throws TimeoutException, InterruptedException {
    //        SessionLeaseManager leaseManager = spy(new SessionLeaseManager());
    //        sessionManager.setRaftSessionLeaseManager(leaseManager)
    //            .setSessionLeaseManager(leaseManager);
    SessionNode sessionNode = new SessionNode(randomURL(randomIp()), getDc());
    long timestamp = System.currentTimeMillis();
    sessionNode.setProcessId(new ProcessId(sessionNode.getIp(), timestamp, 1, random.nextInt()));
    NotifyObserversCounter counter = new NotifyObserversCounter();
    sessionManager.addObserver(counter);

    makeMetaLeader();

    sessionManager.renew(sessionNode, 1);
    //        verify(leaseManager, times(1)).register(any());
    Assert.assertEquals(1, counter.getCounter());

    sessionManager.renew(sessionNode, 1);
    //        verify(leaseManager, times(1)).register(any());
    Assert.assertEquals(1, counter.getCounter());

    SessionNode sessionNode2 = new SessionNode(sessionNode.getNodeUrl(), getDc());
    sessionNode2.setProcessId(new ProcessId(sessionNode.getIp(), timestamp, 2, random.nextInt()));
    Assert.assertFalse(sessionManager.renew(sessionNode2, 1));
    //        verify(leaseManager, times(2)).register(any());
    //        verify(leaseManager, times(1)).renew(any(), anyInt());
    Assert.assertEquals(2, counter.getCounter());
  }

  @Test
  public void testDataServerManagerRefreshEpochOnlyOnceWhenNewRegistered()
      throws TimeoutException, InterruptedException {
    makeMetaLeader();
    SessionNode node = new SessionNode(randomURL(randomIp()), getDc());
    //        SessionLeaseManager leaseManager = spy(new SessionLeaseManager());
    //        sessionManager.setSessionLeaseManager(leaseManager)
    //            .setRaftSessionLeaseManager(leaseManager);
    sessionManager.renew(node, 1000);
    Assert.assertEquals(1, sessionManager.getSessionServerMetaInfo().getClusterMembers().size());
    //        verify(leaseManager, times(1)).refreshEpoch(anyLong());
  }
}
