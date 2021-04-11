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

import org.junit.Assert;
import org.junit.Test;

public class BaseRegisterTest {
  @Test
  public void test() {
    BaseRegister register = new BaseRegister();
    register.setInstanceId("x1");
    Assert.assertEquals(register.getInstanceId(), "x1");

    register.setZone("x2");
    Assert.assertEquals(register.getZone(), "x2");

    register.setGroup("x3");
    Assert.assertEquals(register.getGroup(), "x3");

    register.setProcessId("x4");
    Assert.assertEquals(register.getProcessId(), "x4");

    register.setClientId("x5");
    Assert.assertEquals(register.getClientId(), "x5");

    register.setIp("x6");
    Assert.assertEquals(register.getIp(), "x6");

    register.setPort(999);
    Assert.assertEquals(register.getPort().intValue(), 999);

    register.setDataInfoId("x7");
    Assert.assertEquals(register.getDataInfoId(), "x7");

    Assert.assertNotNull(register.toString());
  }
}
