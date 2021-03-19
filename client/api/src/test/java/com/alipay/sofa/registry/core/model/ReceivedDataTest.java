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
package com.alipay.sofa.registry.core.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class ReceivedDataTest {

  @Test
  public void testAll() {

    ReceivedData data = new ReceivedData();
    data.setDataId("dataIdd");
    data.setGroup("groupp");
    data.setInstanceId("instanceIdd");
    data.setSubscriberRegistIds(Arrays.asList("id1", "id2"));
    data.setLocalZone("local");
    data.setScope("zone");
    data.setData(new HashMap<String, List<DataBox>>());
    data.setSegment("seg1");
    data.setVersion(1234L);
    Assert.assertEquals("dataIdd", data.getDataId());
    Assert.assertEquals("groupp", data.getGroup());
    Assert.assertEquals("instanceIdd", data.getInstanceId());
    Assert.assertEquals(2, data.getSubscriberRegistIds().size());
    Assert.assertEquals("zone", data.getScope());
    Assert.assertEquals("seg1", data.getSegment());
    Assert.assertEquals("local", data.getLocalZone());
    Assert.assertEquals(1234L, (long) data.getVersion());
    Assert.assertEquals(0, data.getData().size());
    Assert.assertTrue(data.toString().contains("instanceIdd"));

    ReceivedData data1 = new ReceivedData(null, null, null, null, null, null, null);
    Assert.assertNull(data1.getVersion());
  }
}
