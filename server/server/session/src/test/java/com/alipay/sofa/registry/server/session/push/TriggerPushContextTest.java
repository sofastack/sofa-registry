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

import org.junit.Assert;
import org.junit.Test;

public class TriggerPushContextTest {
  @Test
  public void test() {
    TriggerPushContext ctx = new TriggerPushContext("testDc", 100, "testData", 200);
    Assert.assertTrue(ctx.toString(), ctx.toString().contains("100"));

    Assert.assertEquals(ctx.dataCenter, "testDc");
    Assert.assertEquals(ctx.dataNode, "testData");
    Assert.assertEquals(ctx.getExpectDatumVersion(), 100);
    Assert.assertEquals(ctx.getTriggerSessionTimestamp(), 200);

    ctx.setExpectDatumVersion(300);
    ctx.setTriggerSessionTimestamp(500);

    Assert.assertEquals(ctx.getExpectDatumVersion(), 300);
    Assert.assertEquals(ctx.getTriggerSessionTimestamp(), 500);
  }
}
