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
package com.alipay.sofa.registry.common.model.multi;

import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : DataCenterMetadataTest.java, v 0.1 2023年02月02日 19:32 xiaojian.xj Exp $
 */
public class DataCenterMetadataTest {

  @Test
  public void test() {
    DataCenterMetadata metadata1 = new DataCenterMetadata("dc1", Sets.newHashSet("zone1", "zone2"));
    DataCenterMetadata metadata2 = new DataCenterMetadata("dc1", Sets.newHashSet("zone1", "zone2"));
    DataCenterMetadata metadata3 = new DataCenterMetadata("dc2", Sets.newHashSet("zone1", "zone2"));
    Assert.assertEquals(metadata1, metadata2);
    Assert.assertNotEquals(metadata1, metadata3);
    Assert.assertEquals(metadata1.toString(), metadata2.toString());
  }
}
