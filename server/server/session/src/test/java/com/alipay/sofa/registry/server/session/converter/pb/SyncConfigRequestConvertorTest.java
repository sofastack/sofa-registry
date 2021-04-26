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

import com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb;
import com.alipay.sofa.registry.core.model.SyncConfigRequest;
import org.junit.Assert;
import org.junit.Test;

public class SyncConfigRequestConvertorTest {
  @Test
  public void test() {
    Assert.assertNull(SyncConfigRequestConvertor.convert2Pb(null));
    Assert.assertNull(SyncConfigRequestConvertor.convert2Java(null));
    SyncConfigRequest registerJava = new SyncConfigRequest();

    registerJava.setDataCenter("testDataCenter");
    registerJava.setZone("testZone");

    SyncConfigRequestPb pb = SyncConfigRequestConvertor.convert2Pb(registerJava);
    SyncConfigRequest convertJava = SyncConfigRequestConvertor.convert2Java(pb);

    Assert.assertEquals(registerJava.getDataCenter(), convertJava.getDataCenter());
    Assert.assertEquals(registerJava.getZone(), convertJava.getZone());
    Assert.assertEquals(registerJava.toString(), convertJava.toString());
  }
}
