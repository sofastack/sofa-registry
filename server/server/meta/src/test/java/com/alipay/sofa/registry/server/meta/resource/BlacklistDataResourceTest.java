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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BlacklistDataResourceTest extends AbstractMetaServerTestBase {

  private BlacklistDataResource resource;

  private ProvideDataService provideDataService = spy(new InMemoryProvideDataRepo());

  private DefaultProvideDataNotifier dataNotifier;

  @Before
  public void before() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    resource =
        new BlacklistDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService);
  }

  @Test
  public void testBlacklistPush() {
    resource.blacklistPush(
        "{\"FORBIDDEN_PUB\":{\"IP_FULL\":[\"1.1.1.1\",\"10.15.233.150\"]},\"FORBIDDEN_SUB_BY_PREFIX\":{\"IP_FULL\":[\"1.1.1.1\"]}}");
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    Assert.assertNotNull(provideDataService.queryProvideData(ValueConstants.BLACK_LIST_DATA_ID));
  }
}
