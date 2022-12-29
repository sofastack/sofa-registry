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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import com.alipay.sofa.registry.server.session.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class SubscriberRegisterConvertorTest {
  @Test
  public void test() {
    Assert.assertNull(SubscriberRegisterConvertor.convert2Pb(null));
    Assert.assertNull(SubscriberRegisterConvertor.convert2Java(null));
    SubscriberRegister registerJava = new SubscriberRegister();
    TestUtils.setField(registerJava);
    registerJava.setScope("testScope");
    SubscriberRegisterPb pb = SubscriberRegisterConvertor.convert2Pb(registerJava);
    SubscriberRegister convertJava = SubscriberRegisterConvertor.convert2Java(pb);
    TestUtils.assertEquals(registerJava, convertJava);
    Assert.assertEquals(registerJava.getScope(), convertJava.getScope());
    Assert.assertEquals(registerJava.acceptMulti(), convertJava.acceptMulti());
  }
}
