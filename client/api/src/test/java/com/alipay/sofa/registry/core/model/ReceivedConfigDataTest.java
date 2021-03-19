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
import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class ReceivedConfigDataTest {

  @Test
  public void testAll() {
    ReceivedConfigData data = new ReceivedConfigData();
    data.setDataId("dataIdd");
    data.setGroup("groupp");
    data.setInstanceId("instanceIdd");
    data.setConfiguratorRegistIds(Arrays.asList("id1", "id2"));
    data.setDataBox(new DataBox());
    data.setVersion(1234L);
    Assert.assertEquals("dataIdd", data.getDataId());
    Assert.assertEquals("groupp", data.getGroup());
    Assert.assertEquals("instanceIdd", data.getInstanceId());
    Assert.assertEquals(2, data.getConfiguratorRegistIds().size());
    Assert.assertNotNull(data.getDataBox());
    Assert.assertEquals(1234L, (long) data.getVersion());
    Assert.assertTrue(data.toString().contains("instanceIdd"));
  }
}
