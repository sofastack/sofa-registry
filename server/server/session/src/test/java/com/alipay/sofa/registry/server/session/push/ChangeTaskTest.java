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
package com.alipay.sofa.registry.server.session.push;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author huicha
 * @date 2025/11/5
 */
public class ChangeTaskTest {

  @Test
  public void testChangKeyEqAndCompare_sameDataInfoId() {
    Set<String> dcs1 = Sets.newHashSet("dc1");
    Set<String> dcs12 = Sets.newHashSet("dc1", "dc2");
    Set<String> dcs21 = Sets.newHashSet("dc2", "dc1");
    Set<String> dcs2 = Sets.newHashSet("dc2");

    ChangeKey key1 = new ChangeKey(dcs1, "dataInfoId");
    ChangeKey key12 = new ChangeKey(dcs12, "dataInfoId");
    ChangeKey key21 = new ChangeKey(dcs21, "dataInfoId");
    ChangeKey key2 = new ChangeKey(dcs2, "dataInfoId");

    // test equals
    Assert.assertNotEquals(key1, key12);
    Assert.assertNotEquals(key1, key21);
    Assert.assertNotEquals(key1, key2);

    Assert.assertNotEquals(key12, key1);
    Assert.assertEquals(key12, key21);
    Assert.assertNotEquals(key12, key2);

    Assert.assertNotEquals(key21, key1);
    Assert.assertEquals(key21, key12);
    Assert.assertNotEquals(key21, key2);

    Assert.assertNotEquals(key2, key1);
    Assert.assertNotEquals(key2, key12);
    Assert.assertNotEquals(key2, key21);

    // test compare
    Assert.assertTrue(key1.compareTo(key12) < 0);
    Assert.assertTrue(key1.compareTo(key21) < 0);
    Assert.assertTrue(key1.compareTo(key2) < 0);

    Assert.assertTrue(key12.compareTo(key1) > 0);
    Assert.assertTrue(key12.compareTo(key21) == 0);
    Assert.assertTrue(key12.compareTo(key2) > 0);

    Assert.assertTrue(key21.compareTo(key1) > 0);
    Assert.assertTrue(key21.compareTo(key12) == 0);
    Assert.assertTrue(key21.compareTo(key2) > 0);

    Assert.assertTrue(key2.compareTo(key1) > 0);
    Assert.assertTrue(key2.compareTo(key12) < 0);
    Assert.assertTrue(key2.compareTo(key21) < 0);
  }

  @Test
  public void testChangKeyEqAndCompare_sameDataInfoId_sameDCs() {
    Set<String> dcs = Sets.newHashSet("dc1", "dc2");
    Set<String> sameDCs = Sets.newHashSet("dc1", "dc2");

    ChangeKey key1 = new ChangeKey(dcs, "dataInfoId1");
    ChangeKey key2 = new ChangeKey(dcs, "dataInfoId2");
    ChangeKey otherKey1 = new ChangeKey(sameDCs, "dataInfoId1");

    // test equals
    Assert.assertNotEquals(key1, key2);
    Assert.assertEquals(key1, otherKey1);

    Assert.assertNotEquals(key2, key1);
    Assert.assertNotEquals(key2, otherKey1);

    Assert.assertEquals(otherKey1, key1);
    Assert.assertNotEquals(otherKey1, key2);

    // test compare
    Assert.assertTrue(key1.compareTo(key2) < 0);
    Assert.assertTrue(key1.compareTo(otherKey1) == 0);

    Assert.assertTrue(key2.compareTo(key1) > 0);
    Assert.assertTrue(key2.compareTo(otherKey1) > 0);

    Assert.assertTrue(otherKey1.compareTo(key1) == 0);
    Assert.assertTrue(otherKey1.compareTo(key2) < 0);
  }

  @Test
  public void testChangeTaskEqAndCompare_sameKey() {
    Set<String> dcs = Sets.newHashSet("dc1", "dc2");
    Set<String> sameDCs = Sets.newHashSet("dc1", "dc2");

    Map<String, Long> dcVersions = Maps.newHashMap();
    dcVersions.put("dc1", 1L);
    dcVersions.put("dc2", 2L);

    ChangeKey key = new ChangeKey(dcs, "dataInfoId");
    ChangeKey otherKey = new ChangeKey(sameDCs, "dataInfoId");

    TriggerPushContext ctx = new TriggerPushContext(dcVersions, "data", System.currentTimeMillis());
    ChangeHandler handler = Mockito.mock(ChangeHandler.class);

    ChangeTaskImpl task1 = new ChangeTaskImpl(key, ctx, handler, 100);
    task1.expireDeadlineTimestamp = 1100;

    ChangeTaskImpl task2 = new ChangeTaskImpl(key, ctx, handler, 200);
    task2.expireDeadlineTimestamp = 1200;

    ChangeTaskImpl task3 = new ChangeTaskImpl(otherKey, ctx, handler, 100);
    task3.expireDeadlineTimestamp = 1100;

    // test equals
    Assert.assertEquals(task1, task2);
    Assert.assertEquals(task1, task3);

    Assert.assertEquals(task2, task1);
    Assert.assertEquals(task2, task3);

    Assert.assertEquals(task3, task1);
    Assert.assertEquals(task3, task2);

    // test compare
    Assert.assertTrue(task1.compareTo(task2) == 0);
    Assert.assertTrue(task1.compareTo(task3) == 0);

    Assert.assertTrue(task2.compareTo(task1) == 0);
    Assert.assertTrue(task2.compareTo(task3) == 0);

    Assert.assertTrue(task3.compareTo(task1) == 0);
    Assert.assertTrue(task3.compareTo(task2) == 0);
  }

  @Test
  public void testChangeTaskEqAndCompare_differentKey() {
    Set<String> dcs12 = Sets.newHashSet("dc1", "dc2");
    Set<String> sameDCs12 = Sets.newHashSet("dc1", "dc2");
    Set<String> dcs3 = Sets.newHashSet("dc3");

    ChangeKey key12 = new ChangeKey(dcs12, "dataInfoId");
    ChangeKey otherKey12 = new ChangeKey(sameDCs12, "dataInfoId");
    ChangeKey differentKey12 = new ChangeKey(dcs12, "dataInfoId2");
    ChangeKey key3 = new ChangeKey(dcs3, "dataInfoId");

    Map<String, Long> dcVs12 = Maps.newHashMap();
    dcVs12.put("dc1", 1L);
    dcVs12.put("dc2", 2L);
    Map<String, Long> dcVs3 = Maps.newHashMap();
    dcVs3.put("dc3", 3L);

    TriggerPushContext ctx12 = new TriggerPushContext(dcVs12, "data", System.currentTimeMillis());
    TriggerPushContext ctx3 = new TriggerPushContext(dcVs3, "data", System.currentTimeMillis());
    ChangeHandler handler = Mockito.mock(ChangeHandler.class);

    ChangeTaskImpl task12 = new ChangeTaskImpl(key12, ctx12, handler, 100);
    task12.expireDeadlineTimestamp = 1100;

    ChangeTaskImpl otherTask12 = new ChangeTaskImpl(otherKey12, ctx12, handler, 200);
    otherTask12.expireDeadlineTimestamp = 1200;

    ChangeTaskImpl differentTask12 = new ChangeTaskImpl(differentKey12, ctx12, handler, 100);
    differentTask12.expireDeadlineTimestamp = 1100;

    ChangeTaskImpl task3 = new ChangeTaskImpl(key3, ctx3, handler, 300);
    task3.expireDeadlineTimestamp = 1300;

    // test equals
    Assert.assertEquals(task12, otherTask12);
    Assert.assertNotEquals(task12, differentTask12);
    Assert.assertNotEquals(task12, task3);

    Assert.assertEquals(otherTask12, task12);
    Assert.assertNotEquals(otherTask12, differentTask12);
    Assert.assertNotEquals(otherTask12, task3);

    Assert.assertNotEquals(differentTask12, task12);
    Assert.assertNotEquals(differentTask12, otherTask12);
    Assert.assertNotEquals(differentTask12, task3);

    Assert.assertNotEquals(task3, task12);
    Assert.assertNotEquals(task3, otherTask12);
    Assert.assertNotEquals(task3, differentTask12);

    // test compare
    Assert.assertTrue(task12.compareTo(otherTask12) == 0);
    Assert.assertTrue(task12.compareTo(differentTask12) < 0);
    Assert.assertTrue(task12.compareTo(task3) < 0);

    Assert.assertTrue(otherTask12.compareTo(task12) == 0);
    Assert.assertTrue(otherTask12.compareTo(differentTask12) > 0);
    Assert.assertTrue(otherTask12.compareTo(task3) < 0);

    Assert.assertTrue(differentTask12.compareTo(task12) > 0);
    Assert.assertTrue(differentTask12.compareTo(otherTask12) < 0);
    Assert.assertTrue(differentTask12.compareTo(task3) < 0);

    Assert.assertTrue(task3.compareTo(task12) > 0);
    Assert.assertTrue(task3.compareTo(otherTask12) > 0);
    Assert.assertTrue(task3.compareTo(differentTask12) > 0);
  }
}
