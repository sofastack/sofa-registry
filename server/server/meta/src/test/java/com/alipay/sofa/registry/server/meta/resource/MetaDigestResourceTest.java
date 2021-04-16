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
package com.alipay.sofa.registry.server.meta.resource;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultMetaServerManager;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetaDigestResourceTest {

  private MetaDigestResource metaDigestResource;

  private StopPushDataResource stopPushDataResource;

  private DefaultMetaServerManager metaServerManager;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataService provideDataService =
      spy(new AbstractMetaServerTestBase.InMemoryProvideDataRepo());

  @Before
  public void beforeMetaDigestResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    stopPushDataResource =
        new StopPushDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService);
    metaDigestResource =
        new MetaDigestResource()
            .setProvideDataService(provideDataService)
            .setMetaServerManager(metaServerManager);
  }

  @Test(expected = RuntimeException.class)
  public void testGetRegisterNodeByType() {
    metaDigestResource.getRegisterNodeByType(Node.NodeType.META.name());
    verify(metaServerManager, times(1)).getSummary(Node.NodeType.META);
    metaDigestResource.getRegisterNodeByType(Node.NodeType.DATA.name());
    verify(metaServerManager, times(1)).getSummary(Node.NodeType.DATA);
    metaDigestResource.getRegisterNodeByType(Node.NodeType.SESSION.name());
    verify(metaServerManager, times(1)).getSummary(Node.NodeType.SESSION);
    when(metaServerManager.getSummary(any()))
        .thenThrow(new SofaRegistryRuntimeException("expected exception"));
    metaDigestResource.getRegisterNodeByType(Node.NodeType.SESSION.name());
  }

  @Test
  public void testGetPushSwitch() {
    String key = "stopPush";
    String val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertNull(val);

    DataInfo dataInfo = DataInfo.valueOf(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setDataId(dataInfo.getDataId());
    persistenceData.setGroup(dataInfo.getGroup());
    persistenceData.setInstanceId(dataInfo.getInstanceId());
    persistenceData.setVersion(System.currentTimeMillis());
    provideDataService.saveProvideData(persistenceData);
    val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertNull(val);

    stopPushDataResource.closePush();
    val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertEquals("true", val);

    stopPushDataResource.openPush();
    val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertEquals("false", val);
  }
}
