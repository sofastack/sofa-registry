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

import com.alipay.sofa.registry.common.model.sessionserver.SimpleSubscriber;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class SubscriberUtilsTest {
  @Test
  public void testGetMinRegisterTimestamp() {
    Assert.assertEquals(
        Long.MAX_VALUE, SubscriberUtils.getMinRegisterTimestamp(Collections.emptyList()));
    Subscriber s1 = new Subscriber();
    s1.setRegisterTimestamp(System.currentTimeMillis());
    Assert.assertEquals(
        s1.getRegisterTimestamp(), SubscriberUtils.getMinRegisterTimestamp(Lists.newArrayList(s1)));
    Subscriber s2 = new Subscriber();
    s2.setRegisterTimestamp(s1.getRegisterTimestamp() + 1);

    Assert.assertEquals(
        s1.getRegisterTimestamp(),
        SubscriberUtils.getMinRegisterTimestamp(Lists.newArrayList(s1, s2)));
  }

  @Test
  public void testSimpleSub() {
    Subscriber s1 = new Subscriber();
    s1.setAppName("testApp");
    s1.setClientId("testClientId");
    s1.setSourceAddress(new URL("192.168.1.1", 8888));

    SimpleSubscriber simple1 = SubscriberUtils.convert(s1);
    Assert.assertEquals(s1.getAppName(), simple1.getAppName());
    Assert.assertEquals(s1.getClientId(), simple1.getClientId());
    Assert.assertEquals(s1.getSourceAddress().buildAddressString(), simple1.getSourceAddress());

    Subscriber s2 = new Subscriber();
    s2.setAppName("testApp2");
    s2.setClientId("testClientId2");
    s2.setSourceAddress(new URL("192.168.1.2", 8888));

    Assert.assertTrue(SubscriberUtils.convert((List) null).isEmpty());

    List<SimpleSubscriber> ss = SubscriberUtils.convert(Lists.newArrayList(s1, s2));
    Assert.assertEquals(ss.size(), 2);

    Assert.assertEquals(s1.getAppName(), ss.get(0).getAppName());
    Assert.assertEquals(s1.getClientId(), ss.get(0).getClientId());
    Assert.assertEquals(s1.getSourceAddress().buildAddressString(), ss.get(0).getSourceAddress());

    Assert.assertEquals(s2.getAppName(), ss.get(1).getAppName());
    Assert.assertEquals(s2.getClientId(), ss.get(1).getClientId());
    Assert.assertEquals(s2.getSourceAddress().buildAddressString(), ss.get(1).getSourceAddress());
  }
}
