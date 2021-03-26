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
package com.alipay.sofa.registry.server.meta.lease.data;

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultDataServerManagerTest extends AbstractMetaServerTestBase {

  private DefaultDataServerManager dataServerManager;

  @Mock private MetaServerConfig metaServerConfig;

  @Before
  public void beforeDefaultdataServerManagerTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    dataServerManager =
        new DefaultDataServerManager(metaServerConfig) {

          @Override
          protected boolean amILeader() {
            return true;
          }
        };
    dataServerManager.setMetaServerConfig(metaServerConfig);
    when(metaServerConfig.getExpireCheckIntervalMilli()).thenReturn(60);
    dataServerManager.postConstruct();
  }

  @After
  public void afterDefaultdataServerManagerTest() throws Exception {
    dataServerManager.preDestory();
  }

  @Test
  public void testGetEpoch() {
    //        dataServerManager.setRaftDataLeaseManager((LeaseManager<DataNode>)
    // Proxy.newProxyInstance(
    //            Thread.currentThread().getContextClassLoader(), new Class[] { LeaseManager.class
    // },
    //            new InvocationHandler() {
    //                @Override
    //                public Object invoke(Object proxy, Method method, Object[] args) throws
    // Throwable {
    //                    return method.invoke(dataServerManager.getLocalLeaseManager(), args);
    //                }
    //            }));
    Assert.assertEquals(0, dataServerManager.getEpoch());
    dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 1000);
    dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 1000);
    Assert.assertNotEquals(0, dataServerManager.getEpoch());
  }

  @Test
  public void testIsLeaderGetClusterMembers() throws TimeoutException, InterruptedException {
    DataNode node = new DataNode(randomURL(randomIp()), getDc());
    //        dataServerManager.getLocalLeaseManager().renew(node, 1000);
    //        makeRaftLeader();
    //        dataServerManager.renew(node, 1000);
    //        Assert.assertEquals(1, dataServerManager.getClusterMembers().size());
    //        verify(dataServerManager.getLocalLeaseManager(), atLeast(1)).getClusterMembers();
  }

  @Test
  public void testEvitTime() {
    Assert.assertEquals(60, dataServerManager.getEvictBetweenMilli());
  }

  @Test
  public void testDataServerManagerRefreshEpochOnlyOnceWhenNewRegistered()
      throws TimeoutException, InterruptedException {
    makeMetaLeader();
    DataNode node = new DataNode(randomURL(randomIp()), getDc());
    //        DataLeaseManager leaseManager = spy(new DataLeaseManager());
    //
    // dataServerManager.setDataLeaseManager(leaseManager).setRaftDataLeaseManager(leaseManager);
    //        dataServerManager.renew(node, 1000);
    //        Assert.assertEquals(1, dataServerManager.getClusterMembers().size());
    //        verify(leaseManager, times(1)).refreshEpoch(anyLong());
  }
}
