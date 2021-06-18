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
package com.alipay.sofa.registry.test.resource.session;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.resource.ClientManagerResource;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class ClientsManagerOpenResourceTest extends BaseIntegrationTest {
  private String dataId = "test-dataId-ZoneClientOff-" + System.currentTimeMillis();
  private String value = "test client off";

  @Test
  public void testZoneClientOff() throws Exception {
    startServerIfNecessary();
    ClientManagerResource clientManagerResource =
        (ClientManagerResource) sessionApplicationContext.getBean("clientManagerResource");
    ClientManagerResource mockedResource = spy(clientManagerResource);
    when(mockedResource.getOtherConsoleServersCurrentZone())
        .thenReturn(Arrays.asList(new URL(LOCAL_ADDRESS, consolePort)));

    CommonResponse response =
        mockedResource.clientOffInZone(sessionChannel.getLocalAddress().getHostString());
    assertTrue(response.getMessage(), response.isSuccess());

    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(3000L);

    long count =
        sessionDataStore.getDataList().stream().filter(p -> p.getDataId().equals(dataId)).count();
    Assert.assertEquals(count, 1);

    response = mockedResource.clientOffInZone(sessionChannel.getLocalAddress().getHostString());
    assertTrue(response.isSuccess());

    count =
        sessionDataStore.getDataList().stream().filter(p -> p.getDataId().equals(dataId)).count();
    Assert.assertEquals(count, 0);

    // clientOn
    response = mockedResource.clientOn(sessionChannel.getLocalAddress().getHostString());
    assertTrue(response.getMessage(), response.isSuccess());
    Thread.sleep(5000L);
    count =
        sessionDataStore.getDataList().stream().filter(p -> p.getDataId().equals(dataId)).count();
    Assert.assertEquals(count, 1);
  }

  @After
  public void clean() {
    registryClient1.unregister(dataId, ValueConstants.DEFAULT_GROUP, RegistryType.PUBLISHER);
    ConcurrentUtils.sleepUninterruptibly(2, TimeUnit.SECONDS);
  }
}
