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

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.server.session.TestUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class PublisherConverterTest {

  @Test
  public void test() throws Exception {
    PublisherRegister register = new PublisherRegister();
    TestUtils.setField(register);
    register.setDataList(Lists.newArrayList(new DataBox("testDataBox")));
    long now1 = System.currentTimeMillis();
    Publisher publisher = PublisherConverter.convert(register);
    long now2 = System.currentTimeMillis();
    TestUtils.assertBetween(publisher.getRegisterTimestamp(), now1, now2);
    TestUtils.assertEquals(register, publisher);

    Assert.assertEquals(PublishType.NORMAL, publisher.getPublishType());
    Assert.assertEquals(PublishSource.CLIENT, publisher.getPublishSource());

    Assert.assertEquals(register.getDataList().size(), publisher.getDataList().size());
    Assert.assertEquals(register.getDataList().size(), 1);
    ServerDataBox box = publisher.getDataList().get(0);
    box.extract();
    Assert.assertEquals(box.getObject(), "testDataBox");
  }
}
