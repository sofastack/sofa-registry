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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

public class SubPublisherListTest {

  @Test
  public void test() {
    SubPublisher pub =
        new SubPublisher(
            "registerId",
            "cell",
            Lists.newArrayList(new ServerDataBox(new byte[] {1, 2, 3})),
            "clientId",
            1,
            "srcAddr",
            1234,
            PublishSource.CLIENT);
    SubPublisherList l = new SubPublisherList(Lists.newArrayList(pub));
    Assert.assertEquals(105, l.size());
  }
}
