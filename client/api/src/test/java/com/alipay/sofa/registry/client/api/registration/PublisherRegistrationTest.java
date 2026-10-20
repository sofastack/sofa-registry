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

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class PublisherRegistrationTest {

  @Test
  public void testAll() {
    PublisherRegistration registration = new PublisherRegistration("xxx");
    Assert.assertEquals("xxx", registration.getDataId());
    Assert.assertTrue(registration.toString().contains("xxx"));
  }

  @Test
  public void testEqualsAndHashCode() {
    PublisherRegistration registration = new PublisherRegistration("dataId");
    registration.setGroup("group");
    registration.setAppName("appName");
    registration.setInstanceId("instanceId");
    registration.setIp("127.0.0.1");

    PublisherRegistration sameRegistration = new PublisherRegistration("dataId");
    sameRegistration.setGroup("group");
    sameRegistration.setAppName("appName");
    sameRegistration.setInstanceId("instanceId");
    sameRegistration.setIp("127.0.0.1");

    Assert.assertEquals(registration, sameRegistration);
    Assert.assertEquals(registration.hashCode(), sameRegistration.hashCode());

    sameRegistration.setGroup("otherGroup");
    Assert.assertNotEquals(registration, sameRegistration);
  }

  @Test
  public void rejectsNullAndOtherRegistrationTypes() {
    PublisherRegistration registration = new PublisherRegistration("dataId");
    Assert.assertTrue(registration.equals(registration));
    Assert.assertFalse(registration.equals(null));
    Assert.assertFalse(registration.equals(new BaseRegistration()));
  }

  @Test
  public void equalIdentitiesDeduplicateWithNullableFields() {
    String[][] identities = {
      {"dataId", "group", "app", "instance", "127.0.0.1"},
      {null, null, null, null, null},
      {null, "group", "app", "instance", "127.0.0.1"},
      {"dataId", null, "app", "instance", "127.0.0.1"},
      {"dataId", "group", null, "instance", "127.0.0.1"},
      {"dataId", "group", "app", null, "127.0.0.1"},
      {"dataId", "group", "app", "instance", null}
    };
    for (String[] identity : identities) {
      PublisherRegistration first = registration(identity);
      PublisherRegistration second = registration(identity);
      Assert.assertEquals(first, second);
      Assert.assertEquals(second, first);
      Assert.assertEquals(first.hashCode(), second.hashCode());
      Set<PublisherRegistration> registrations = new HashSet<>();
      Assert.assertTrue(registrations.add(first));
      Assert.assertFalse(registrations.add(second));
      Assert.assertTrue(registrations.remove(second));
      Assert.assertTrue(registrations.isEmpty());
    }
  }

  @Test
  public void eachIdentityFieldDistinguishesRegistrations() {
    String[] identity = {"dataId", "group", "app", "instance", "127.0.0.1"};
    PublisherRegistration original = registration(identity);
    for (int field = 0; field < identity.length; field++) {
      for (String differentValue : new String[] {null, "different"}) {
        String[] differentIdentity = identity.clone();
        differentIdentity[field] = differentValue;
        PublisherRegistration different = registration(differentIdentity);
        Assert.assertNotEquals("Identity field " + field, original, different);
        Assert.assertNotEquals("Identity field " + field, different, original);
      }
    }
  }

  private static PublisherRegistration registration(String[] identity) {
    PublisherRegistration registration = new PublisherRegistration(identity[0]);
    registration.setGroup(identity[1]);
    registration.setAppName(identity[2]);
    registration.setInstanceId(identity[3]);
    registration.setIp(identity[4]);
    return registration;
  }
}
