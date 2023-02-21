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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.TestUtils;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class RegProcessorTest {
  @Test
  public void test() {
    RegProcessor processor = new RegProcessor(1, mock(RegProcessor.RegHandler.class));
    processor.suspend();
    RegProcessor.BufferWorker w = processor.workers[0];

    // test base
    Assert.assertEquals(0, w.watchBuffer());
    Assert.assertTrue(w.getWaitingMillis() < 500);

    Subscriber sub = TestUtils.newZoneSubscriber("testDataId", "testDc");
    Assert.assertTrue(processor.fireOnReg(sub));
    Assert.assertFalse(processor.fireOnReg(sub));
    Subscriber sub2 = TestUtils.newZoneSubscriber("testDataId", "testDc");
    sub2.setRegisterId(sub.getRegisterId());
    sub2.setVersion(sub.getVersion() + 1);
    Assert.assertTrue(processor.fireOnReg(sub2));

    Assert.assertEquals(1, w.watchBuffer());
    verify(processor.regHandler, times(1)).onReg(anyString(), anyList());

    sub2.checkAndUpdateCtx(
        Collections.singletonMap("testDc", 100L), Collections.singletonMap("testDc", 10));
    Assert.assertTrue(processor.fireOnReg(sub2));
    Assert.assertEquals(0, w.watchBuffer());
    verify(processor.regHandler, times(1)).onReg(anyString(), anyList());
  }
}
