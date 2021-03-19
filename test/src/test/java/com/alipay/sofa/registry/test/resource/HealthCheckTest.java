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
package com.alipay.sofa.registry.test.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertTrue;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/14
 */
@RunWith(SpringRunner.class)
public class HealthCheckTest extends BaseIntegrationTest {
  @Test
  public void metaHealthCheckTest() {
    assertTrue(
        metaChannel
            .getWebTarget()
            .path("health/check")
            .request(APPLICATION_JSON)
            .get(CommonResponse.class)
            .isSuccess());
  }

  @Test
  public void dataHealthCheckTest() {
    assertTrue(
        dataChannel
            .getWebTarget()
            .path("health/check")
            .request(APPLICATION_JSON)
            .get(CommonResponse.class)
            .isSuccess());
  }

  @Test
  public void sessionHealthCheckTest() {
    assertTrue(
        sessionChannel
            .getWebTarget()
            .path("health/check")
            .request(APPLICATION_JSON)
            .get(CommonResponse.class)
            .isSuccess());
  }
}
