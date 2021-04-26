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

import com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.server.session.TestUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class PublisherRegisterConvertorTest {
  @Test
  public void test() {
    Assert.assertNull(PublisherRegisterConvertor.convert2Pb(null));
    Assert.assertNull(PublisherRegisterConvertor.convert2Java(null));
    PublisherRegister registerJava = new PublisherRegister();
    TestUtils.setField(registerJava);
    registerJava.setDataList(Lists.newArrayList(new DataBox("testDataBox")));

    PublisherRegisterPb pb = PublisherRegisterConvertor.convert2Pb(registerJava);
    PublisherRegister convertJava = PublisherRegisterConvertor.convert2Java(pb);
    TestUtils.assertEquals(registerJava, convertJava);
    Assert.assertEquals(registerJava.toString(), convertJava.toString());

    Assert.assertEquals(registerJava.getDataList().size(), 1);
    Assert.assertEquals(registerJava.getDataList().size(), convertJava.getDataList().size());
    Assert.assertEquals(
        registerJava.getDataList().get(0).getData(), convertJava.getDataList().get(0).getData());
  }
}
