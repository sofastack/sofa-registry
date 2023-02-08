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
package com.alipay.sofa.registry.server.data.resource;

import static org.mockito.Matchers.anyString;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.cache.CleanContinues;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.BatchPutDataHandler;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DatumApiResourceTest {

  private DatumApiResource newResource() {
    DatumApiResource resource = new DatumApiResource();
    resource.dataServerConfig = TestBaseUtils.newDataConfig("testDc");
    DatumStorageDelegate datumStorageDelegate = TestBaseUtils.newLocalDatumDelegate("testDc", true);
    resource.datumStorageDelegate = datumStorageDelegate;
    resource.slotManager = Mockito.mock(SlotManager.class);
    resource.batchPutDataHandler = Mockito.mock(BatchPutDataHandler.class);
    return resource;
  }

  private DatumParam newParam(String dataInfoId) {
    DatumParam param = new DatumParam();
    param.setDataInfoId(dataInfoId);
    param.setPublisherRegisterId("testRegisterId");
    param.setPublisherCell("rz00a");
    param.setPublisherDataBox("testDataBox");
    param.setPublisherRegisterTimestamp(System.currentTimeMillis());
    param.setPublisherVersion(1);
    return param;
  }

  @Test
  public void testGetNotEmpty() {
    String datacenter = "testdc";
    DatumApiResource resource = newResource();
    Mockito.when(resource.slotManager.slotOf(anyString())).thenReturn(10);
    Mockito.when(resource.slotManager.getSlot(anyString(), Mockito.anyInt()))
        .thenReturn(new Slot(10, "xxx", 1, Collections.emptyList()));

    Publisher pub = TestBaseUtils.createTestPublishers(10, 1).get(0);
    DatumVersion v = resource.datumStorageDelegate.putPublisher(datacenter, pub);

    DatumParam param = new DatumParam();
    param.setDataCenter("testDc");
    param.setDataInfoId(pub.getDataInfoId());

    // get version
    String msg = (String) resource.getDatumVersion(param);
    Assert.assertTrue(msg, msg.contains("version:" + v.getValue()));

    Map<String, Long> versions = (Map<String, Long>) resource.getDatumVersions(param);
    Assert.assertEquals(versions.size(), 1);
    Assert.assertEquals(versions.get(pub.getDataInfoId()).longValue(), v.getValue());

    Map<String, Integer> sizes = (Map<String, Integer>) resource.getDatumSizes();
    Assert.assertEquals(1, sizes.size());
    Assert.assertEquals(sizes.get("testDc").intValue(), 1);
  }

  @Test
  public void testAddDelete() {
    String datacenter = "testdc";

    final DatumApiResource resource = newResource();
    Mockito.when(resource.slotManager.slotOf(anyString())).thenReturn(10);
    Mockito.when(resource.slotManager.getSlot(anyString(), Mockito.anyInt()))
        .thenReturn(new Slot(10, "xxx", 1, Collections.emptyList()));

    Publisher pub = TestBaseUtils.createTestPublishers(10, 1).get(0);
    resource.datumStorageDelegate.putPublisher(datacenter, pub);

    DatumParam param = newParam(pub.getDataInfoId());
    Datum datum = resource.datumStorageDelegate.get(datacenter, pub.getDataInfoId());
    Publisher pubAdd = resource.buildPublisher(datum, param);

    CommonResponse response = resource.get(param);
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getMessage(), response.getMessage().contains("size=1"));

    Map<String, Integer> summary = resource.summary("");
    Assert.assertEquals(1, summary.get(pub.getDataInfoId()).intValue());

    resource.dataServerConfig.setEnableTestApi(true);
    // batchHandler is mock, put the pub directly
    resource.datumStorageDelegate.putPublisher(datacenter, pubAdd);
    response = resource.addPub(param);
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getMessage(), response.getMessage().contains("size=2"));

    // unable api
    resource.dataServerConfig.setEnableTestApi(false);
    response = resource.addPub(param);
    Assert.assertFalse(response.isSuccess());

    response = resource.delete(param);
    Assert.assertFalse(response.isSuccess());

    resource.dataServerConfig.setEnableTestApi(true);
    response = resource.delete(param);
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getMessage(), response.getMessage().contains("size=0"));

    param.setDataInfoId("testDataIdNotExist");
    response = resource.delete(param);
    Assert.assertFalse(response.isSuccess());
    Assert.assertTrue(response.getMessage().contains("not found"));

    response = resource.deletePub(param);
    Assert.assertFalse(response.isSuccess());
    Assert.assertTrue(response.getMessage().contains("not found"));

    param.setDataInfoId(pub.getDataInfoId());
    resource.datumStorageDelegate.putPublisher(datacenter, pub);
    response = resource.deletePub(param);
    Assert.assertFalse(response.isSuccess());
    Assert.assertTrue(response.getMessage(), response.getMessage().contains("No pub of"));

    param.setPublisherRegisterId(pub.getRegisterId());
    // mock batchHandler, not delete the pub
    response = resource.deletePub(param);
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getMessage(), response.getMessage().contains("size=1"));

    Mockito.when(resource.batchPutDataHandler.doHandle(Mockito.anyObject(), Mockito.anyObject()))
        .thenAnswer(
            new Answer<Object>() {
              public Object answer(InvocationOnMock var1) throws Throwable {
                for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
                  resource.datumStorageDelegate.cleanBySessionId(
                      datacenter, i, ServerEnv.PROCESS_ID, CleanContinues.ALWAYS);
                }
                return null;
              }
            });
    response = resource.deletePub(param);
    Assert.assertTrue(response.getMessage(), response.isSuccess());
    Assert.assertTrue(response.getMessage(), response.getMessage().contains("size=0"));

    summary = resource.summary("");
    Assert.assertEquals(0, summary.get(pub.getDataInfoId()).intValue());
  }

  @Test
  public void testGetEmpty() {
    DatumApiResource resource = newResource();

    // get null
    CommonResponse response = resource.get(null);
    Assert.assertFalse(response.isSuccess());

    // get notFound
    DatumParam param = new DatumParam();
    param.setDataInfoId("testDataInfoId");
    response = resource.get(param);
    Assert.assertFalse(response.isSuccess());
    Assert.assertTrue(response.getMessage().contains("not found"));

    // get version
    String msg = (String) resource.getDatumVersion(param);
    Assert.assertTrue(msg.contains("version:null"));

    param.setDataCenter("testDc");
    msg = (String) resource.getDatumVersion(param);
    Assert.assertTrue(msg.contains("version:null"));

    // get remote datum version
    param = new DatumParam();
    Map<String, Long> versions = (Map<String, Long>) resource.getDatumVersions(param);
    Assert.assertTrue(versions.isEmpty());

    param.setDataCenter("testDc");
    versions = (Map<String, Long>) resource.getDatumVersions(param);
    Assert.assertTrue(versions.isEmpty());

    // unsupported get remote
    TestBaseUtils.assertException(
        UnsupportedOperationException.class, () -> resource.getRemoteDatumVersions(null));

    Map<String, Integer> sizes = (Map<String, Integer>) resource.getDatumSizes();
    Assert.assertEquals(1, sizes.size());
    Assert.assertEquals(sizes.get("testDc").intValue(), 0);
  }

  @Test
  public void testBuild() {
    DatumApiResource resource = newResource();
    DatumParam param = newParam("testDataInfoId");
    param.setPublisherConnectId("192.168.1.1");
    TestBaseUtils.assertException(
        IllegalArgumentException.class, () -> resource.buildPublisher(null, param));

    param.setPublisherConnectId("192.168.1.1:123_192.168.1.2:456");
    Publisher publisher = resource.buildPublisher(null, param);
    Assert.assertEquals(param.getDataInfoId(), publisher.getDataInfoId());
    Assert.assertEquals("192.168.1.1:123", publisher.getSourceAddress().buildAddressString());
    Assert.assertEquals("192.168.1.2:456", publisher.getTargetAddress().buildAddressString());
  }
}
