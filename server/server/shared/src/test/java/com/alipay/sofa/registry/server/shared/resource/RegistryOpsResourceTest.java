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
package com.alipay.sofa.registry.server.shared.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class RegistryOpsResourceTest {
  @Test
  public void test() {
    MetaServerService metaServerService = Mockito.mock(MetaServerService.class);
    RegistryOpsResource resource = new RegistryOpsResource();

    resource.metaServerService = metaServerService;
    CommonResponse response = resource.kickOffMyself();
    Assert.assertTrue(response.isSuccess());
    Mockito.verify(metaServerService, Mockito.times(1)).addSelfToMetaBlacklist();

    response = resource.putMyselfBack();
    Assert.assertTrue(response.isSuccess());
    Mockito.verify(metaServerService, Mockito.times(1)).removeSelfFromMetaBlacklist();
  }

  @Test
  public void testException() {
    RegistryOpsResource resource = new RegistryOpsResource();
    // npe
    CommonResponse response = resource.kickOffMyself();
    Assert.assertFalse(response.isSuccess());
    response = resource.putMyselfBack();
    Assert.assertFalse(response.isSuccess());
  }
}
