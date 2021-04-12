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
package com.alipay.sofa.registry.client.provider;

import org.junit.Assert;
import org.junit.Test;

public class DefaultRegistryClientConfigTest {
  @Test
  public void test() {
    DefaultRegistryClientConfig config1 = DefaultRegistryClientConfigBuilder.start().build();
    Assert.assertFalse(config1.equals(null));

    Assert.assertTrue(config1.equals(config1));
    Assert.assertEquals(config1.hashCode(), config1.hashCode());

    DefaultRegistryClientConfig config2 = DefaultRegistryClientConfigBuilder.start().build();

    config2.setAlgorithm("test");
    Assert.assertEquals(config2.getAlgorithm(), "test");

    config2.setEventBusEnable(false);
    Assert.assertFalse(config2.isEventBusEnable());

    config2.setAuthCacheInterval(100);
    Assert.assertEquals(config2.getAuthCacheInterval(), 100);
    // the setting props not impact equals
    Assert.assertEquals(config1, config2);
    Assert.assertEquals(config1.hashCode(), config2.hashCode());

    DefaultRegistryClientConfig config3 =
        DefaultRegistryClientConfigBuilder.start().setZone("xxx").build();
    Assert.assertNotEquals(config1, config3);

    Assert.assertTrue(config3.toString(), config3.toString().contains("xxx"));
  }
}
