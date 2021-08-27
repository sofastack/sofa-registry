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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyRequest;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.BlacklistDataResource;
import com.alipay.sofa.registry.test.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author xiaojian.xj
 * @version $Id: FetchSystemPropertyRequestHandlerTest.java, v 0.1 2021年06月03日 22:40 xiaojian.xj Exp
 *     $
 */
public class FetchSystemPropertyRequestHandlerTest extends AbstractMetaServerTestBase {

  private FetchSystemPropertyRequestHandler handler = new FetchSystemPropertyRequestHandler();

  @Mock private ProvideDataService provideDataService;

  private BlacklistDataResource resource;

  private DefaultProvideDataNotifier dataNotifier = mock(DefaultProvideDataNotifier.class);

  private FetchSystemPropertyRequest newBlacklistRequest() {
    return new FetchSystemPropertyRequest(ValueConstants.BLACK_LIST_DATA_ID, 1L);
  }

  @Before
  public void beforeFetchSystemPropertyRequestHandlerTest() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testBlacklistHandler() {
    FetchSystemPropertyRequest blacklistRequest = newBlacklistRequest();

    handler.checkParam(blacklistRequest);
    TestUtils.assertException(
        RuntimeException.class, () -> handler.doHandle(null, blacklistRequest));

    provideDataService = spy(InMemoryProvideDataRepo.class);

    handler.setProvideDataService(provideDataService);
    FetchSystemPropertyResult result =
        (FetchSystemPropertyResult) handler.doHandle(null, blacklistRequest);
    Assert.assertEquals(result.isVersionUpgrade(), false);
    Assert.assertNull(result.getProvideData());

    resource =
        new BlacklistDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService);
    resource.blacklistPush(
        "{\"FORBIDDEN_PUB\":{\"IP_FULL\":[\"1.1.1.1\"]},\"FORBIDDEN_SUB_BY_PREFIX\":{\"IP_FULL\":[\"1.1.1.1\"]}}");
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    Assert.assertNotNull(provideDataService.queryProvideData(ValueConstants.BLACK_LIST_DATA_ID));
    FetchSystemPropertyResult resultAfterPut =
        (FetchSystemPropertyResult) handler.doHandle(null, blacklistRequest);
    Assert.assertEquals(resultAfterPut.isVersionUpgrade(), true);
    Assert.assertTrue(resultAfterPut.getProvideData().getVersion() > 0);
  }
}
