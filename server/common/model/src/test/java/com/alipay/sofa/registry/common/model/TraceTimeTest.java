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
package com.alipay.sofa.registry.common.model;

import org.junit.Assert;
import org.junit.Test;

public class TraceTimeTest {
  @Test
  public void testCopy() throws InterruptedException {
    TraceTimes times = new TraceTimes();
    times.setDataChangeType(1);
    times.setFirstDataChange(3);
    times.setDatumNotifyCreate(5);
    times.setDatumNotifySend(6);
    times.setTriggerSession(7);
    times = times.copy();
    Assert.assertEquals(1, times.getDataChangeType());
    Assert.assertEquals(3, times.getFirstDataChange());
    Assert.assertEquals(5, times.getDatumNotifyCreate());
    Assert.assertEquals(6, times.getDatumNotifySend());
    Assert.assertEquals(7, times.getTriggerSession());
    times.format(10);
    Thread.sleep(10);
    Assert.assertTrue(times.beforeThan(new TraceTimes()));
  }
}
