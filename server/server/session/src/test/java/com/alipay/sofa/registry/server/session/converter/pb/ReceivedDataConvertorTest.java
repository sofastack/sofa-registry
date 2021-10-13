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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb;
import com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb;
import com.alipay.sofa.registry.compress.CompressConstants;
import com.alipay.sofa.registry.compress.CompressUtils;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import java.util.Collections;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class ReceivedDataConvertorTest {
  @Test
  public void testReceivedData() {
    Assert.assertNull(ReceivedDataConvertor.convert2Pb((ReceivedData) null));
    Assert.assertNull(ReceivedDataConvertor.convert2Java(null));

    ReceivedData registerJava = new ReceivedData();
    registerJava.setScope("testScope");
    registerJava.setDataId("testDataId");
    registerJava.setGroup("testGroup");
    registerJava.setInstanceId("testInstanceId");
    registerJava.setLocalZone("testLocalZone");
    registerJava.setSegment("testSegment");
    registerJava.setVersion(System.currentTimeMillis());
    registerJava.setSubscriberRegistIds(Lists.newArrayList("testRegisterId"));
    registerJava.setData(
        Collections.singletonMap("testZone", Lists.newArrayList(new DataBox("testDataBox"))));

    ReceivedDataPb pb = ReceivedDataConvertor.convert2Pb(registerJava);
    ReceivedData convertJava = ReceivedDataConvertor.convert2Java(pb);
    assertReceivedData(registerJava, convertJava);

    Assert.assertTrue(
        registerJava.toString(), registerJava.toString().contains(registerJava.getDataId()));
  }

  @Test
  public void testReceivedConfigData() {
    Assert.assertNull(ReceivedDataConvertor.convert2Pb((ReceivedConfigData) null));

    ReceivedConfigData registerJava = new ReceivedConfigData();
    registerJava.setDataId("testDataId");
    registerJava.setGroup("testGroup");
    registerJava.setInstanceId("testInstanceId");
    registerJava.setVersion(System.currentTimeMillis());
    registerJava.setConfiguratorRegistIds(Lists.newArrayList("testRegisterId"));
    registerJava.setDataBox(new DataBox("testDataBox"));

    ReceivedConfigDataPb pb = ReceivedDataConvertor.convert2Pb(registerJava);

    Assert.assertEquals(pb.getDataId(), registerJava.getDataId());
    Assert.assertEquals(pb.getGroup(), registerJava.getGroup());
    Assert.assertEquals(pb.getInstanceId(), registerJava.getInstanceId());
    Assert.assertEquals(pb.getVersion(), registerJava.getVersion().longValue());
    Assert.assertEquals(pb.getDataBox().getData(), registerJava.getDataBox().getData());
    Assert.assertEquals(pb.getConfiguratorRegistIdsList(), registerJava.getConfiguratorRegistIds());

    Assert.assertTrue(
        registerJava.toString(), registerJava.toString().contains(registerJava.getDataId()));
  }

  private void assertReceivedData(ReceivedData left, ReceivedData right) {
    Assert.assertEquals(left.getScope(), right.getScope());
    Assert.assertEquals(left.getDataId(), right.getDataId());
    Assert.assertEquals(left.getGroup(), right.getGroup());
    Assert.assertEquals(left.getInstanceId(), right.getInstanceId());
    Assert.assertEquals(left.getLocalZone(), right.getLocalZone());
    Assert.assertEquals(left.getSegment(), right.getSegment());
    Assert.assertEquals(left.getVersion(), right.getVersion());
    Assert.assertEquals(left.getSubscriberRegistIds(), right.getSubscriberRegistIds());
    Assert.assertEquals(left.getData().keySet(), right.getData().keySet());
    Assert.assertEquals(left.getData().values().size(), right.getData().values().size());
    Assert.assertEquals(left.getData().values().size(), 1);
    Assert.assertEquals(
        left.getData().values().iterator().next().get(0).getData(),
        right.getData().values().iterator().next().get(0).getData());
  }

  @Test
  public void testCompress() {
    ReceivedData registerJava = new ReceivedData();
    registerJava.setScope("testScope");
    registerJava.setDataId("testDataId");
    registerJava.setGroup("testGroup");
    registerJava.setInstanceId("testInstanceId");
    registerJava.setLocalZone("testLocalZone");
    registerJava.setSegment("testSegment");
    registerJava.setVersion(System.currentTimeMillis());
    registerJava.setSubscriberRegistIds(Lists.newArrayList("testRegisterId"));
    registerJava.setData(
        Collections.singletonMap("testZone", Lists.newArrayList(new DataBox("testDataBox"))));
    ReceivedDataPb dataPb =
        ReceivedDataConvertor.convert2CompressedPb(
            registerJava, CompressUtils.find(new String[] {CompressConstants.encodingZstd}));
    Assert.assertEquals(0, dataPb.getDataMap().size());
    Assert.assertNotEquals(0, dataPb.getOriginBodySize());
    Assert.assertNotEquals(0, dataPb.getBody().size());
  }
}
