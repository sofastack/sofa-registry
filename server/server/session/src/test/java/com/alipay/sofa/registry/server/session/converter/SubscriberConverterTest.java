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
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import com.alipay.sofa.registry.server.session.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class SubscriberConverterTest {
  @Test
  public void testSubscriber() {
    SubscriberRegister register = new SubscriberRegister();
    TestUtils.setField(register);
    register.setScope(ScopeEnum.dataCenter.name());
    long now1 = System.currentTimeMillis();
    register.setAcceptEncoding("zstd,gzip");
    Subscriber subscriber = SubscriberConverter.convert(register);
    long now2 = System.currentTimeMillis();
    TestUtils.assertBetween(subscriber.getRegisterTimestamp(), now1, now2);
    TestUtils.assertEquals(register, subscriber);
    Assert.assertEquals(2, subscriber.getAcceptEncodes().length);
    Assert.assertEquals(ScopeEnum.dataCenter, subscriber.getScope());
  }

  @Test
  public void testWatcher() {
    ConfiguratorRegister register = new ConfiguratorRegister();
    TestUtils.setField(register);
    long now1 = System.currentTimeMillis();
    Watcher watcher = SubscriberConverter.convert(register);
    long now2 = System.currentTimeMillis();
    TestUtils.assertBetween(watcher.getRegisterTimestamp(), now1, now2);
    TestUtils.assertEquals(register, watcher);
  }
}
