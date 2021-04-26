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

import com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import org.junit.Assert;
import org.junit.Test;

public class RegisterResponseConvertorTest {
  @Test
  public void test() {
    Assert.assertNull(RegisterResponseConvertor.convert2Pb(null));
    Assert.assertNull(RegisterResponseConvertor.convert2Java(null));
    RegisterResponse registerJava = new RegisterResponse();

    registerJava.setVersion(100);
    registerJava.setRegistId("testRegisterId");
    registerJava.setMessage("testMsg");
    registerJava.setSuccess(true);
    registerJava.setRefused(true);

    RegisterResponsePb pb = RegisterResponseConvertor.convert2Pb(registerJava);
    RegisterResponse convertJava = RegisterResponseConvertor.convert2Java(pb);

    Assert.assertEquals(registerJava.getVersion(), convertJava.getVersion());
    Assert.assertEquals(registerJava.getMessage(), convertJava.getMessage());
    Assert.assertEquals(registerJava.isSuccess(), convertJava.isSuccess());
    Assert.assertEquals(registerJava.getRegistId(), convertJava.getRegistId());
    Assert.assertEquals(registerJava.isRefused(), convertJava.isRefused());

    Assert.assertEquals(registerJava.toString(), convertJava.toString());
  }
}
