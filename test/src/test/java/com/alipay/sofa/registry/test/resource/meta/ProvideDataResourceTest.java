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

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static org.junit.Assert.assertTrue;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class ProvideDataResourceTest extends BaseIntegrationTest {

  @Test
  public void testPersistentDataResource() throws Exception {
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setData("PersistentDataResourceTest");
    persistenceData.setVersion(1024L);
    persistenceData.setDataId("PersistentDataResourceTest");
    persistenceData.setGroup(DEFAULT_GROUP);
    persistenceData.setInstanceId(DEFAULT_INSTANCE_ID);

    Result response =
        getMetaChannel()
            .getWebTarget()
            .path("persistentData/put")
            .request()
            .post(Entity.entity(persistenceData, MediaType.APPLICATION_JSON), Result.class);
    assertTrue(response.isSuccess());
    Thread.sleep(500);

    response =
        getMetaChannel()
            .getWebTarget()
            .path("persistentData/remove")
            .request()
            .post(Entity.entity(persistenceData, MediaType.APPLICATION_JSON), Result.class);
    assertTrue(response.isSuccess());
  }
}
