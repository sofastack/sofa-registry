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

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.google.common.collect.Lists;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class SubscriberUtilsTest {
  @Test
  public void testGetMaxRegisterTimestamp() {
    Assert.assertEquals(0, SubscriberUtils.getMaxRegisterTimestamp(Collections.emptyList()));
    Subscriber s1 = new Subscriber();
    s1.setRegisterTimestamp(System.currentTimeMillis());
    Assert.assertEquals(
        s1.getRegisterTimestamp(), SubscriberUtils.getMaxRegisterTimestamp(Lists.newArrayList(s1)));
    Subscriber s2 = new Subscriber();
    s2.setRegisterTimestamp(s1.getRegisterTimestamp() + 1);

    Assert.assertEquals(
        s2.getRegisterTimestamp(),
        SubscriberUtils.getMaxRegisterTimestamp(Lists.newArrayList(s1, s2)));
  }
}
