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
package com.alipay.sofa.registry.test.resource.meta;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertTrue;

import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class MetaDigestResourceTest extends BaseIntegrationTest {

  @Test
  public void testGetRegisterNodeByType() {
    Map map =
        getMetaChannel()
            .getWebTarget()
            .path("digest/data/node/query")
            .request(APPLICATION_JSON)
            .get(Map.class);
    assertTrue(map.size() == 1);
    assertTrue(map.toString(), ((Map) map.get(LOCAL_DATACENTER)).containsKey(LOCAL_ADDRESS));

    map =
        getMetaChannel()
            .getWebTarget()
            .path("digest/meta/node/query")
            .request(APPLICATION_JSON)
            .get(Map.class);
    assertTrue(map.size() == 1);
    assertTrue(map.toString(), ((Map) map.get(LOCAL_DATACENTER)).containsKey(LOCAL_ADDRESS));

    map =
        getMetaChannel()
            .getWebTarget()
            .path("digest/session/node/query")
            .request(APPLICATION_JSON)
            .get(Map.class);
    assertTrue(map.size() == 1);
    assertTrue(map.toString(), ((Map) map.get(LOCAL_DATACENTER)).containsKey(LOCAL_ADDRESS));
  }

  @Test
  public void testGetPushSwitch() {
    Result result =
        getMetaChannel()
            .getWebTarget()
            .path("stopPushDataSwitch/close")
            .request(APPLICATION_JSON)
            .get(Result.class);
    Assert.assertTrue(result.isSuccess());
    Map map =
        getMetaChannel()
            .getWebTarget()
            .path("digest/pushSwitch")
            .request(APPLICATION_JSON)
            .get(Map.class);

    Assert.assertEquals(map.get("stopPush"), "false");
  }
}
