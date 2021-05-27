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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.resource.ClientManagerResource;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class ClientsManagerOpenResourceTest extends BaseIntegrationTest {

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
    String dataId = "test-dataId-" + System.currentTimeMillis();
    String value = "test client off";
    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, value);
    Thread.sleep(2000L);

    String countResult =
        dataChannel
            .getWebTarget()
            .path("digest/datum/count")
            .request(APPLICATION_JSON)
            .get(String.class);
    assertTrue(countResult.contains("[Publisher] size of publisher in DefaultDataCenter is 1"));
    response = mockedResource.clientOffInZone(sessionChannel.getLocalAddress().getHostString());
    assertTrue(response.isSuccess());
    countResult =
        dataChannel
            .getWebTarget()
            .path("digest/datum/count")
            .request(APPLICATION_JSON)
            .get(String.class);
    assertTrue(countResult.contains("[Publisher] size of publisher in DefaultDataCenter is 0"));
  }
}
