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

import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class BaseRegistrationTest {

  @Test
  public void testAll() {
    BaseRegistration registration = new BaseRegistration();
    registration.setAppName("app");
    registration.setDataId("dataId");
    registration.setGroup("group");
    registration.setInstanceId("instanceId");
    registration.setIp("ip");
    Assert.assertEquals("app", registration.getAppName());
    Assert.assertEquals("dataId", registration.getDataId());
    Assert.assertEquals("group", registration.getGroup());
    Assert.assertEquals("instanceId", registration.getInstanceId());
    Assert.assertEquals("ip", registration.getIp());
    Assert.assertEquals(
        "BaseRegistration{dataId='dataId', group='group', appName='app', instanceId='instanceId', ip='ip'}",
        registration.toString());
  }

  @Test
  public void testSetAttrs() {
    BaseRegistration registration = new BaseRegistration();
    registration.setIp("127.0.0.1");
    Assert.assertEquals(registration.getIp(), "127.0.0.1");
    registration.setInstanceId("instanceId");
    Assert.assertEquals(registration.getInstanceId(), "instanceId");
  }
}
