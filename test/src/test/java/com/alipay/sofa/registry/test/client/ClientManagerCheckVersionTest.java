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
package com.alipay.sofa.registry.test.client;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.sessionserver.CheckClientManagerRequest;
import com.alipay.sofa.registry.common.model.sessionserver.CheckClientManagerResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version : ClientManagerCheckVersionTest.java, v 0.1 2022年01月20日 14:47 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class ClientManagerCheckVersionTest extends BaseIntegrationTest {
  private final String localAddress = sessionChannel.getLocalAddress().getHostString();
  private final String CLIENT_OFF_STR = "41.1.1.1;42.2.2.2";

  @Test
  public void testQueryClientOff() {
    /** client off */
    GenericResponse<Long> response = persistenceClientManagerResource.clientOff(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getData() > 0);

    String expectedVersion = String.valueOf(response.getData().longValue());
    CommonResponse commonResponse = persistenceClientManagerResource.checkVersion(expectedVersion);
    Assert.assertTrue(commonResponse.isSuccess());

    /** check */
    GenericResponse result =
        (GenericResponse)
            sessionConsoleExchanger
                .request(
                    new SimpleRequest(
                        new CheckClientManagerRequest(response.getData().longValue()),
                        new URL(localAddress, sessionServerConfig.getConsolePort())))
                .getResult();

    Assert.assertTrue(result.isSuccess());
    CheckClientManagerResponse data = (CheckClientManagerResponse) result.getData();
    Assert.assertTrue(data.isPaasCheck());
    Assert.assertEquals(data.getActualVersion(), response.getData().longValue());
  }
}
