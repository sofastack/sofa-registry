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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import java.sql.Timestamp;
import org.junit.Assert;
import org.junit.Test;

public class InterfaceAppsIndexContainerTest {
  @Test
  public void test() {
    InterfaceAppsIndexContainer container = new InterfaceAppsIndexContainer();
    InterfaceAppsIndexDomain domain1 = new InterfaceAppsIndexDomain("dc", "service1", "app1");
    domain1.setGmtCreate(new Timestamp(10));
    InterfaceAppsIndexDomain domain2 = new InterfaceAppsIndexDomain("dc", "service1", "app2");
    domain2.toString();
    domain2.setGmtCreate(new Timestamp(20));
    container.onEntry(domain1);
    container.onEntry(domain2);
    Assert.assertTrue(container.containsName("dc", "service1", "app1"));
    Assert.assertTrue(container.containsName("dc", "service1", "app2"));

    InterfaceAppsIndexDomain domain3 = new InterfaceAppsIndexDomain("dc", "service2", "app1");
    domain3.setGmtCreate(new Timestamp(30));
    Assert.assertEquals(2, container.getAppMapping("service1").get("dc").getApps().size());
    Assert.assertEquals(20000000, container.getAppMapping("service1").get("dc").getNanosVersion());
    Assert.assertFalse(
        container.containsName(
            domain3.getDataCenter(), domain3.getInterfaceName(), domain3.getAppName()));
    container.onEntry(domain3);
    Assert.assertTrue(
        container.containsName(
            domain3.getDataCenter(), domain3.getInterfaceName(), domain3.getAppName()));
    Assert.assertEquals(20000000, container.getAppMapping("service1").get("dc").getNanosVersion());
    Assert.assertEquals(30000000, container.getAppMapping("service2").get("dc").getNanosVersion());
    domain3.setReference(false);
    container.onEntry(domain3);
    Assert.assertEquals(0, container.getAppMapping("service2").get("dc").getApps().size());
    Assert.assertEquals(30000000, container.getAppMapping("service2").get("dc").getNanosVersion());
    Assert.assertEquals(0, container.getAppMapping("service3").size());
    Assert.assertEquals(container.interfaces().size(), 2);
  }
}
