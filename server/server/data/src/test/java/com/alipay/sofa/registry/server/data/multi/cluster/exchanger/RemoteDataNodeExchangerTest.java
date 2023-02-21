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
package com.alipay.sofa.registry.server.data.multi.cluster.exchanger;

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : RemoteDataNodeExchangerTest.java, v 0.1 2023年02月09日 16:42 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteDataNodeExchangerTest {

  @InjectMocks private RemoteDataNodeExchanger remoteDataNodeExchanger;

  @Mock private MultiClusterDataServerConfig multiClusterDataServerConfig;

  @Test
  public void test() {
    when(multiClusterDataServerConfig.getSyncRemoteSlotLeaderTimeoutMillis()).thenReturn(1);
    when(multiClusterDataServerConfig.getSyncRemoteSlotLeaderPort()).thenReturn(2);
    when(multiClusterDataServerConfig.getSyncRemoteSlotLeaderConnNum()).thenReturn(3);
    Assert.assertEquals(1, remoteDataNodeExchanger.getRpcTimeoutMillis());
    Assert.assertEquals(2, remoteDataNodeExchanger.getServerPort());
    Assert.assertEquals(3, remoteDataNodeExchanger.getConnNum());
  }
}
