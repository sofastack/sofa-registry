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

import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class MetaStoreResourceTest extends BaseIntegrationTest {
    @Test
    public void testManageMetaStoreResource() {
        List list = metaChannel.getWebTarget().path("manage/queryPeer").request(APPLICATION_JSON)
            .get(List.class);
        assertTrue(list.size() == 1);
        assertEquals(LOCAL_ADDRESS, list.get(0));
    }

    @Test
    public void testChangePeer() {
        Form form = new Form();
        form.param("ipAddressList", LOCAL_ADDRESS);
        Result result = metaChannel.getWebTarget().path("manage/changePeer")
            .request(APPLICATION_JSON)
            .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Result.class);
        assertTrue(result.isSuccess());
    }

    @Test
    public void resetPeer() {
        Form form = new Form();
        form.param("ipAddressList", LOCAL_ADDRESS);
        Result result = metaChannel.getWebTarget().path("manage/resetPeer")
            .request(APPLICATION_JSON)
            .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Result.class);
        assertTrue(result.isSuccess());
    }
}
