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
package com.alipay.sofa.registry.compress;

import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CompressKeyTest {
  @Test
  public void testDatumKey() {
    CompressDatumKey key1 = new CompressDatumKey("zstd", "dataInfoId", "dc", 1, 1024);
    CompressDatumKey key2 = new CompressDatumKey("zstd", "dataInfoId", "dc", 1, 1024);
    CompressDatumKey key3 = new CompressDatumKey("zstd", "dataInfoId", "dc", 2, 1024);
    Assert.assertNotNull(key1.toString());

    Assert.assertEquals(key1.hashCode(), key2.hashCode());
    Assert.assertEquals(key1, key2);
    Assert.assertNotEquals(key1, key3);
    Assert.assertEquals(32, key1.size());
    Assert.assertNotNull(key1.toString());
  }

  @Test
  public void testPushKey() {
    ReceivedData receivedData = new ReceivedData();
    receivedData.setDataId("dataId");
    receivedData.setInstanceId("instanceId");
    receivedData.setGroup("group");
    receivedData.setSegment("segment");
    receivedData.setVersion(1L);
    receivedData.setData(
        new HashMap<String, List<DataBox>>() {
          {
            put("RZ00A", Lists.newArrayList(new DataBox("url"), new DataBox("url")));
            put("RZ00B", Lists.newArrayList(new DataBox("url"), new DataBox("url")));
          }
        });
    CompressPushKey key1 =
        CompressPushKey.of(
            receivedData.getSegment(),
            receivedData.getDataId(),
            receivedData.getInstanceId(),
            receivedData.getGroup(),
            receivedData.getVersion(),
            receivedData.getData(),
            "zstd");
    CompressPushKey key2 =
        CompressPushKey.of(
            receivedData.getSegment(),
            receivedData.getDataId(),
            receivedData.getInstanceId(),
            receivedData.getGroup(),
            receivedData.getVersion(),
            receivedData.getData(),
            "zstd");
    Assert.assertEquals(key1, key2);
    Assert.assertEquals(key1.hashCode(), key2.hashCode());
    Assert.assertNotNull(key1.toString());
    Assert.assertEquals(100, key1.size());

    receivedData.setData(
        new HashMap<String, List<DataBox>>() {
          {
            put("RZ00A", Lists.newArrayList(new DataBox("url"), new DataBox("url")));
            put("RZ00B", Lists.newArrayList(new DataBox("url")));
          }
        });
    CompressPushKey key3 =
        CompressPushKey.of(
            receivedData.getSegment(),
            receivedData.getDataId(),
            receivedData.getInstanceId(),
            receivedData.getGroup(),
            receivedData.getVersion(),
            receivedData.getData(),
            "zstd");
    Assert.assertNotEquals(key1, key3);
  }
}
