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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.UnPublisher;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.junit.Assert;
import org.junit.Test;

public class PublisherEnvelopeTest {

  @Test(expected = IllegalArgumentException.class)
  public void testErrorType() {
    Publisher p =
        new Publisher() {
          public DataType getDataType() {
            return DataType.SUBSCRIBER;
          }

          public ProcessId getSessionProcessId() {
            return ServerEnv.PROCESS_ID;
          }
        };
    PublisherEnvelope.of(p);
  }

  @Test
  public void test() {
    Publisher publisher = TestBaseUtils.createTestPublisher("testDataInfoId");
    PublisherEnvelope envelope = PublisherEnvelope.of(publisher);
    ParaCheckUtil.checkNotBlank(envelope.toString(), "tostring");
    Assert.assertTrue(publisher == envelope.publisher);
    Assert.assertEquals(publisher.registerVersion(), envelope.registerVersion);
    Assert.assertEquals(publisher.registerVersion(), envelope.getVersionIfPub());
    Assert.assertEquals(publisher.getSessionProcessId(), ServerEnv.PROCESS_ID);
    Assert.assertEquals(Long.MAX_VALUE, envelope.tombstoneTimestamp);
    Assert.assertTrue(envelope.isPub());
    Assert.assertTrue(envelope.isConnectId(publisher.connectId()));
    Assert.assertFalse(envelope.isConnectId(ConnectId.of("127.0.0.1:9999", "127.0.0.2:9997")));

    UnPublisher unPublisher = UnPublisher.of(publisher);
    envelope = PublisherEnvelope.of(unPublisher);

    Assert.assertNull(envelope.publisher);
    Assert.assertEquals(unPublisher.registerVersion(), envelope.registerVersion);
    Assert.assertNull(envelope.getVersionIfPub());
    Assert.assertEquals(unPublisher.getSessionProcessId(), ServerEnv.PROCESS_ID);
    Assert.assertTrue(envelope.tombstoneTimestamp <= System.currentTimeMillis());
    Assert.assertFalse(envelope.isPub());
    Assert.assertFalse(envelope.isConnectId(publisher.connectId()));
    Assert.assertFalse(envelope.isConnectId(ConnectId.of("127.0.0.1:9999", "127.0.0.2:9997")));
  }
}
