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
package com.alipay.sofa.registry.server.meta.resource;

import static org.mockito.Mockito.spy;

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SlotSyncResourceTest extends AbstractMetaServerTestBase {

  private SlotSyncResource slotSyncResource;

  private ProvideDataService provideDataService = spy(new InMemoryProvideDataRepo());

  @Before
  public void before() {
    slotSyncResource = new SlotSyncResource().setProvideDataService(provideDataService);
  }

  @Test
  public void testGetSlotSync() throws Exception {
    Map<String, Object> result = slotSyncResource.getSlotSync();
    Assert.assertEquals("null", result.get("syncSessionIntervalSec"));
    Assert.assertEquals("null", result.get("dataDatumExpire"));
  }
}
