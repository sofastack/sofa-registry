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

import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.PushData;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.TestUtils;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class PushTaskBufferTest {
  @Test
  public void test() throws InterruptedException {
    PushTaskBuffer buffer = new PushTaskBuffer(2);
    Assert.assertEquals(2, buffer.workers.length);

    String dataId = "testDataId";
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, "region");
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());

    MockTask task =
        new MockTask(
            new PushCause(
                null,
                PushType.Sub,
                Collections.singletonMap(datum.getDataCenter(), System.currentTimeMillis())),
            NetUtil.getLocalSocketAddress(),
            Collections.singletonMap(subscriber.getRegisterId(), subscriber),
            datum);
    task.expireTimestamp = 1;
    Assert.assertTrue(buffer.buffer(task));

    datum = TestUtils.newSubDatum(subscriber.getDataId(), 101, Collections.emptyList());
    MockTask task1 =
        new MockTask(
            new PushCause(
                null,
                PushType.Sub,
                Collections.singletonMap(datum.getDataCenter(), System.currentTimeMillis() + 1)),
            NetUtil.getLocalSocketAddress(),
            Collections.singletonMap(subscriber.getRegisterId(), subscriber),
            datum);
    task1.expireTimestamp = 2;
    Assert.assertTrue(buffer.buffer(task1));
  }

  private static final class MockTask extends PushTask {

    MockTask(
        PushCause pushCause,
        InetSocketAddress addr,
        Map<String, Subscriber> subscriberMap,
        SubDatum datum) {
      super(pushCause, addr, subscriberMap, MultiSubDatum.of(datum));
    }

    @Override
    protected boolean commit() {
      return false;
    }

    @Override
    protected PushData createPushData() {
      return null;
    }
  }
}
