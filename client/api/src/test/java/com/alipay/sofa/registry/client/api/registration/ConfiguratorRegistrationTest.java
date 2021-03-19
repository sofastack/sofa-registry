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
package com.alipay.sofa.registry.client.api.registration;

import com.alipay.sofa.registry.client.api.ConfigDataObserver;
import com.alipay.sofa.registry.client.api.model.ConfigData;
import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class ConfiguratorRegistrationTest {

  @Test
  public void testAll() {
    ConfiguratorRegistration registration = new ConfiguratorRegistration("xxx", null);
    Assert.assertEquals("xxx", registration.getDataId());
    Assert.assertNull(registration.getConfigDataObserver());
    registration.setConfigDataObserver(
        new ConfigDataObserver() {
          @Override
          public void handleData(String dataId, ConfigData configData) {}
        });
    Assert.assertNotNull(registration.getConfigDataObserver());
    Assert.assertTrue(registration.toString().contains("xxx"));
  }
}
