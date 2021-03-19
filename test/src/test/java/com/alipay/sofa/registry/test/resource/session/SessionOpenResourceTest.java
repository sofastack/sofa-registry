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

import static org.junit.Assert.assertEquals;

import com.alipay.sofa.registry.test.BaseIntegrationTest;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class SessionOpenResourceTest extends BaseIntegrationTest {
  @Test
  public void testCheckAlive() {
    String result =
        sessionChannel.getWebTarget().path("api/servers/alive").request().get(String.class);
    assertEquals("OK", result);
  }

  @Test
  public void testGetSessionServerList() {
    String result =
        sessionChannel
            .getWebTarget()
            .path("api/servers/query")
            .queryParam("zone", LOCAL_REGION)
            .request()
            .get(String.class);
    assertEquals(LOCAL_ADDRESS + ":" + sessionServerPort, result);
  }

  @Test
  public void testGetSessionServerListJson() {
    List<String> result =
        sessionChannel
            .getWebTarget()
            .path("api/servers/query.json")
            .queryParam("zone", LOCAL_REGION)
            .request(MediaType.APPLICATION_JSON)
            .get(new GenericType<List<String>>() {});
    assertEquals(1, result.size());
    assertEquals(LOCAL_ADDRESS + ":" + sessionServerPort, result.get(0));
  }
}
