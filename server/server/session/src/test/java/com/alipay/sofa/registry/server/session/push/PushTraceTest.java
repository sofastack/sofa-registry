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

import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.SubPublisher;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class PushTraceTest {

  @Test
  public void test() throws Exception {
    List<SubPublisher> list = Lists.newArrayList();
    list.add(TestUtils.newSubPublisher(0, System.currentTimeMillis()));
    Thread.sleep(5);
    final long middle = DatumVersionUtil.nextId();
    Thread.sleep(5);
    list.add(TestUtils.newSubPublisher(0, System.currentTimeMillis()));
    Thread.sleep(5);
    list.add(TestUtils.newSubPublisher(0, System.currentTimeMillis()));

    SubDatum subDatum = TestUtils.newSubDatum("testDataId", DatumVersionUtil.nextId(), list);
    long now1 = System.currentTimeMillis();
    TriggerPushContext ctx = new TriggerPushContext("testDc", 100, null, now1);
    PushTrace trace =
        PushTrace.trace(
            subDatum,
            NetUtil.getLocalSocketAddress(),
            "subApp",
            new PushCause(ctx, PushType.Sub, now1),
            1,
            System.currentTimeMillis() - 100);
    long now2 = System.currentTimeMillis();

    // new.sub=2
    trace.startPush(middle, now2);
    long finish = now2 + 100;
    trace.finishPush(PushTrace.PushStatus.OK, finish);

    trace.print();

    Assert.assertEquals(trace.datumTotalDelayMillis, finish - trace.pushCause.datumTimestamp);
    Assert.assertEquals(
        trace.datumPushCommitSpanMillis,
        trace.pushCommitTimestamp - trace.pushCause.datumTimestamp);
    Assert.assertEquals(trace.datumPushStartSpanMillis, now2 - trace.pushCommitTimestamp);
    Assert.assertEquals(trace.datumPushFinishSpanMillis, finish - now2);

    Assert.assertEquals(trace.newPublisherNum, 2);
    Assert.assertEquals(trace.firstPubPushDelayMillis, finish - list.get(1).getRegisterTimestamp());
    Assert.assertEquals(trace.lastPubPushDelayMillis, finish - list.get(2).getRegisterTimestamp());
  }

  @Test
  public void testFind() {
    Assert.assertTrue(PushTrace.findNewPublishers(Collections.emptyList(), 100).isEmpty());

    List<SubPublisher> list = Lists.newArrayList();
    list.add(TestUtils.newSubPublisher(0, 100));
    list.add(TestUtils.newSubPublisher(0, 200));
    list.add(TestUtils.newSubPublisher(0, 50));

    List<SubPublisher> find = PushTrace.findNewPublishers(list, 10);
    Assert.assertEquals(3, find.size());
    Assert.assertEquals(find.get(0).getRegisterTimestamp(), 50);
    Assert.assertEquals(find.get(1).getRegisterTimestamp(), 100);
    Assert.assertEquals(find.get(2).getRegisterTimestamp(), 200);

    find = PushTrace.findNewPublishers(list, 80);
    Assert.assertEquals(2, find.size());
    Assert.assertEquals(find.get(0).getRegisterTimestamp(), 100);
    Assert.assertEquals(find.get(1).getRegisterTimestamp(), 200);

    find = PushTrace.findNewPublishers(list, 200);
    Assert.assertEquals(0, find.size());
  }
}
