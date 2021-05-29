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

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.constants.ValueConstants;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class ClientsOpenResourceTest extends BaseIntegrationTest {
  private String dataId = "test-dataId-testClientOff" + System.currentTimeMillis();
  private String value = "test client off";

  @Test
  public void testClientOff() throws Exception {
    clientOff();

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

    clientOff();
    countResult =
        dataChannel
            .getWebTarget()
            .path("digest/datum/count")
            .request(APPLICATION_JSON)
            .get(String.class);
    assertTrue(countResult.contains("[Publisher] size of publisher in DefaultDataCenter is 0"));
  }

  @After
  public void clean() {
    registryClient1.unregister(dataId, ValueConstants.DEFAULT_GROUP, RegistryType.PUBLISHER);
  }
}
