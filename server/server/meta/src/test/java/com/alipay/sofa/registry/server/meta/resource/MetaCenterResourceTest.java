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

import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** @Author dzdx @Date 2022/8/8 16:45 @Version 1.0 */
public class MetaCenterResourceTest {
  private MetaCenterResource metaCenterResource;
  private DefaultProvideDataNotifier dataNotifier;
  private ProvideDataService provideDataService =
      spy(new AbstractMetaServerTestBase.InMemoryProvideDataRepo());

  @Before
  public void before() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    metaCenterResource =
        new MetaCenterResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService);
  }

  @Test
  public void testSetAppRevisionWriteSwitch() {
    Result ret =
        metaCenterResource.setAppRevisionWriteSwitch(
            new AppRevisionDomainConvertor.EnableConfig(false, true));
    Assert.assertTrue(ret.isSuccess());
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    doThrow(new RuntimeException()).when(provideDataService).saveProvideData(any());
    ret =
        metaCenterResource.setAppRevisionWriteSwitch(
            new AppRevisionDomainConvertor.EnableConfig(false, true));
    Assert.assertFalse(ret.isSuccess());
  }
}
