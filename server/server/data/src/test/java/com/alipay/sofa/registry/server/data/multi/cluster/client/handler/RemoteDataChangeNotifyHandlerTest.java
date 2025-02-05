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
package com.alipay.sofa.registry.server.data.multi.cluster.client.handler;

import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.TraceTimes;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManager;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : RemoteDataChangeNotifyHandlerTest.java, v 0.1 2023年02月07日 19:48 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteDataChangeNotifyHandlerTest {

  @InjectMocks private RemoteDataChangeNotifyHandler remoteDataChangeNotifyHandler;

  private String testDc = "testDc";

  @Mock private DataServerConfig dataServerConfig;

  @Mock private MultiClusterSlotManager multiClusterSlotManager;

  @Test
  public void testCheckParam() {
    Assert.assertEquals(DataChangeRequest.class, remoteDataChangeNotifyHandler.interest());
    Assert.assertEquals(NodeType.DATA, remoteDataChangeNotifyHandler.getConnectNodeType());

    DataChangeRequest request =
        new DataChangeRequest(
            testDc,
            Collections.singletonMap(
                "RemoteDataChangeNotifyHandlerTest-datainfoid",
                new DatumVersion(System.currentTimeMillis())),
            new TraceTimes());
    remoteDataChangeNotifyHandler.checkParam(request);
  }

  @Test
  public void testHandle() {

    DataChangeRequest request =
        new DataChangeRequest(
            testDc,
            Collections.singletonMap(
                "RemoteDataChangeNotifyHandlerTest-datainfoid",
                new DatumVersion(System.currentTimeMillis())),
            new TraceTimes());

    when(dataServerConfig.isLocalDataCenter(anyString())).thenReturn(true);
    Assert.assertNull(remoteDataChangeNotifyHandler.doHandle(null, request));
    verify(multiClusterSlotManager, times(0)).dataChangeNotify(anyString(), anySet());

    when(dataServerConfig.isLocalDataCenter(anyString())).thenReturn(false);
    Assert.assertNull(remoteDataChangeNotifyHandler.doHandle(null, request));
    verify(multiClusterSlotManager, times(1)).dataChangeNotify(anyString(), anySet());
  }
}
