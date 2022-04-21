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
package com.alipay.sofa.registry.test.pubsub;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.resource.ClientManagerResource;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** @Author dzdx @Date 2022/4/24 6:11 下午 @Version 1.0 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ClientOffTest extends BaseIntegrationTest {

  @Test
  public void testSetClientIpZoneClientOff() throws Exception {

    String dataId = "test-dataId-clientIpTest-" + System.nanoTime();
    String content = "data";
    PublisherRegistration registration = new PublisherRegistration(dataId);
    registration.setIp("33.33.33.33");
    registryClient1.register(registration, content);
    Thread.sleep(2000L);
    Map<String, List<Publisher>> publisherMap = queryPubs(dataId);
    assertEquals(1, publisherMap.size());
    assertEquals(1, publisherMap.get("PUB").size());

    ClientManagerResource clientManagerResource =
        (ClientManagerResource) sessionApplicationContext.getBean("clientManagerResource");
    ClientManagerResource mockedResource = spy(clientManagerResource);
    when(mockedResource.getOtherConsoleServersCurrentZone())
        .thenReturn(Arrays.asList(new URL(LOCAL_ADDRESS, consolePort)));
    CommonResponse response = mockedResource.clientOffInZone("33.33.33.33");
    Assert.assertTrue(response.isSuccess());
    publisherMap = queryPubs(dataId);
    assertEquals(0, publisherMap.size());
  }

  @Test
  public void testSetClientIpPersistClientOff() throws Exception {
    String dataId = "test-dataId-persistClientOffTest-" + System.nanoTime();
    String content = "data123";
    PublisherRegistration registration = new PublisherRegistration(dataId);
    registration.setIp("33.33.33.34");
    registryClient1.register(registration, content);
    Thread.sleep(2000L);
    Map<String, List<Publisher>> publisherMap = queryPubs(dataId);
    assertEquals(1, publisherMap.size());
    assertEquals(1, publisherMap.get("PUB").size());

    com.alipay.sofa.registry.server.meta.resource.ClientManagerResource clientManagerResource =
        (com.alipay.sofa.registry.server.meta.resource.ClientManagerResource)
            metaApplicationContext.getBean("clientManagerResource");
    com.alipay.sofa.registry.server.meta.resource.ClientManagerResource mockedResource =
        spy(clientManagerResource);
    CommonResponse response = mockedResource.clientOff("33.33.33.34");
    Assert.assertTrue(response.isSuccess());
    Thread.sleep(5000L);
    publisherMap = queryPubs(dataId);
    assertEquals(0, publisherMap.size());
  }

  private Map<String, List<Publisher>> queryPubs(String dataId) {
    return sessionChannel
        .getWebTarget()
        .path("digest/pub/data/query")
        .queryParam("dataInfoId", DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP))
        .request(APPLICATION_JSON)
        .get(new GenericType<Map<String, List<Publisher>>>() {});
  }

  @Test
  public void testSetClientIpClientOpen() throws Exception {
    String dataId = "test-dataId-clientOpenTest-" + System.nanoTime();
    String content = "data123";
    String ip = "33.33.33.35";
    PublisherRegistration registration = new PublisherRegistration(dataId);
    registration.setIp(ip);
    com.alipay.sofa.registry.server.meta.resource.ClientManagerResource clientManagerResource =
        (com.alipay.sofa.registry.server.meta.resource.ClientManagerResource)
            metaApplicationContext.getBean("clientManagerResource");
    com.alipay.sofa.registry.server.meta.resource.ClientManagerResource mockedResource =
        spy(clientManagerResource);
    CommonResponse response = mockedResource.clientOff(ip);
    Assert.assertTrue(response.isSuccess());
    Thread.sleep(3000L);
    registryClient1.register(registration, content);
    Thread.sleep(2000L);
    Map<String, List<Publisher>> publisherMap = queryPubs(dataId);
    Assert.assertEquals(0, publisherMap.size());
    Assert.assertTrue(clientManagerResource.clientOpen(ip).isSuccess());
    Thread.sleep(4000L);
    publisherMap = queryPubs(dataId);
    Assert.assertEquals(1, publisherMap.size());
  }
}
