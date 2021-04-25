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

import com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

public class SyncConfigResponseConvertorTest {
  @Test
  public void test() {
    Assert.assertNull(SyncConfigResponseConvertor.convert2Pb(null));
    Assert.assertNull(SyncConfigResponseConvertor.convert2Java(null));
    SyncConfigResponse registerJava = new SyncConfigResponse();

    registerJava.setRetryInterval(100);
    registerJava.setAvailableSegments(Lists.newArrayList("testSeg"));
    registerJava.setMessage("testMsg");
    registerJava.setSuccess(true);

    SyncConfigResponsePb pb = SyncConfigResponseConvertor.convert2Pb(registerJava);
    SyncConfigResponse convertJava = SyncConfigResponseConvertor.convert2Java(pb);

    Assert.assertEquals(registerJava.getRetryInterval(), convertJava.getRetryInterval());
    Assert.assertEquals(registerJava.getMessage(), convertJava.getMessage());
    Assert.assertEquals(registerJava.isSuccess(), convertJava.isSuccess());
    Assert.assertEquals(registerJava.getAvailableSegments(), convertJava.getAvailableSegments());
  }
}
