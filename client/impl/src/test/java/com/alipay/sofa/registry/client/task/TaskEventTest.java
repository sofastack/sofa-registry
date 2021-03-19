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
package com.alipay.sofa.registry.client.task;

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.client.api.Register;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class TaskEventTest {

  @Test
  public void delayTime() {
    TaskEvent event1 = new TaskEvent(null);
    Assert.assertTrue(event1.delayTime() <= 200);
    event1.incSendCount();
    Assert.assertTrue(event1.delayTime() <= 400);
    event1.incSendCount();
    Assert.assertTrue(event1.delayTime() <= 600);
    event1.incSendCount();
    Assert.assertTrue(event1.delayTime() <= 800);
    event1.incSendCount();
    Assert.assertTrue(event1.delayTime() <= 1000);
    event1.incSendCount();
    Assert.assertTrue(event1.delayTime() <= 1000);
  }

  @Test
  public void testCompare() {
    TaskEvent event1 = new TaskEvent(null);
    TaskEvent event2 = new TaskEvent(null);
    Assert.assertEquals(0, event1.compareTo(event2));

    event1.incSendCount();
    Assert.assertEquals(1, event1.compareTo(event2));
    Assert.assertEquals(-1, event2.compareTo(event1));
    event2.incSendCount();

    Register register = Mockito.mock(Register.class);
    TaskEvent event3 = new TaskEvent(register);
    TaskEvent event4 = new TaskEvent(register);
    TaskEvent event5 = new TaskEvent(null);
    Assert.assertEquals(1, event4.compareTo(event5));
    Assert.assertEquals(-1, event5.compareTo(event3));

    Register register6 = Mockito.mock(Register.class);
    when(register6.getTimestamp()).thenReturn(123L);
    Register register7 = Mockito.mock(Register.class);
    when(register7.getTimestamp()).thenReturn(234L);
    TaskEvent event6 = new TaskEvent(register6);
    TaskEvent event7 = new TaskEvent(register7);
    TaskEvent event8 = new TaskEvent(register7);
    Assert.assertEquals(1, event7.compareTo(event6));
    Assert.assertEquals(-1, event6.compareTo(event7));
    Assert.assertEquals(0, event8.compareTo(event7));
  }

  @Test
  public void testEquals() {
    TaskEvent event1 = new TaskEvent(null);
    TaskEvent event2 = new TaskEvent(null);

    Assert.assertTrue(event1.equals(event1));
    Assert.assertFalse(event1.equals("xxxx"));
    Assert.assertTrue(event1.equals(event2));

    event1.incSendCount();
    Assert.assertFalse(event1.equals(event2));
    event2.incSendCount();

    event1.setTriggerTime(123);
    Assert.assertFalse(event1.equals(event2));
    event2.setTriggerTime(123);

    Register register = Mockito.mock(Register.class);
    TaskEvent event3 = new TaskEvent(register);
    TaskEvent event4 = new TaskEvent(register);
    Assert.assertTrue(event3.equals(event4));
  }

  @Test
  public void testHashCode() {
    TaskEvent event1 = new TaskEvent(null);
    TaskEvent event2 = new TaskEvent(null);
    Assert.assertEquals(event1.hashCode(), event2.hashCode());
    event1.setTriggerTime(1234);
    Assert.assertNotEquals(event1.hashCode(), event2.hashCode());
  }
}
