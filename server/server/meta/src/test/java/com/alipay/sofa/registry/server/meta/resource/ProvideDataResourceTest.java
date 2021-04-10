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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhuchen
 * @date Apr 6, 2021, 7:45:13 PM
 */
public class ProvideDataResourceTest extends AbstractMetaServerTestBase {

  private ProvideDataResource provideDataResource;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataService provideDataService = spy(new InMemoryProvideDataRepo());

  @Before
  public void beforeProvideDataResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    provideDataResource =
        new ProvideDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService);
  }

  @Test
  public void testPut() {
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setData("SampleWord");
    persistenceData.setDataId("dataId");
    persistenceData.setGroup("group");
    persistenceData.setInstanceId("InstanceId");
    persistenceData.setVersion(1000L);
    provideDataResource.put(persistenceData);
    String dataInfoId =
        DataInfo.toDataInfoId(
            persistenceData.getDataId(),
            persistenceData.getInstanceId(),
            persistenceData.getGroup());
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    DBResponse response = provideDataService.queryProvideData(dataInfoId);
    Assert.assertEquals(OperationStatus.SUCCESS, response.getOperationStatus());
    Assert.assertEquals(persistenceData, response.getEntity());
  }

  @Test
  public void testRemove() {
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setData("SampleWord");
    persistenceData.setDataId("dataId");
    persistenceData.setGroup("group");
    persistenceData.setInstanceId("InstanceId");
    persistenceData.setVersion(1000L);
    provideDataResource.put(persistenceData);
    String dataInfoId =
        DataInfo.toDataInfoId(
            persistenceData.getDataId(),
            persistenceData.getInstanceId(),
            persistenceData.getGroup());
    DBResponse response = provideDataService.queryProvideData(dataInfoId);
    Assert.assertEquals(OperationStatus.SUCCESS, response.getOperationStatus());

    PersistenceData other = new PersistenceData();
    other.setData(persistenceData.getData());
    other.setDataId(persistenceData.getDataId());
    other.setGroup(persistenceData.getGroup());
    other.setInstanceId(persistenceData.getInstanceId());
    other.setVersion(persistenceData.getVersion());
    provideDataResource.remove(other);
    response = provideDataService.queryProvideData(dataInfoId);
    verify(dataNotifier, atLeast(1)).notifyProvideDataChange(any());
    Assert.assertEquals(OperationStatus.NOTFOUND, response.getOperationStatus());
  }

  @Test(expected = RuntimeException.class)
  public void testWhenProvideDataAccessFail() {

    when(provideDataService.saveProvideData(mockPersistenceData()))
        .thenThrow(new TimeoutException("expected exception"));
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setData("SampleWord");
    persistenceData.setDataId("dataId");
    persistenceData.setGroup("group");
    persistenceData.setInstanceId("InstanceId");
    persistenceData.setVersion(1000L);
    provideDataResource.put(persistenceData);
  }
}
