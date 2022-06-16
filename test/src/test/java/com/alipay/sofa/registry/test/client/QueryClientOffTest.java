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
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerQueryRequest;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerResp;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version : QueryClientOffTest.java, v 0.1 2021年08月02日 10:12 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class QueryClientOffTest extends BaseIntegrationTest {
  private final String localAddress = sessionChannel.getLocalAddress().getHostString();
  private final String CLIENT_OFF_STR = "31.1.1.1;32.2.2.2";
  private final Set<String> CLIENT_OFF_SET = Sets.newHashSet(CLIENT_OFF_STR.split(";"));

  @Test
  public void testQueryClientOff() throws InterruptedException {
    /** client off */
    CommonResponse response = clientManagerResource.clientOff(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());
    Thread.sleep(3000);
    /** query */
    GenericResponse<Map<String, ClientManagerResp>> queryResp =
        sessionClientManagerResource.queryClientOff();
    Assert.assertTrue(queryResp.isSuccess());
    ClientManagerResp clientManagerResp = queryResp.getData().get(localAddress);
    Assert.assertTrue(clientManagerResp.isSuccess());
    Assert.assertTrue(clientManagerResp.getIps().containsAll(CLIENT_OFF_SET));

    CommonResponse result =
        (CommonResponse)
            sessionConsoleExchanger
                .request(
                    new SimpleRequest(
                        new ClientManagerQueryRequest(),
                        new URL(localAddress, sessionServerConfig.getConsolePort())))
                .getResult();
    Assert.assertTrue(result.isSuccess());
    GenericResponse resp = (GenericResponse) result;
    ClientManagerResp data = (ClientManagerResp) resp.getData();
    Assert.assertTrue(data.getIps().containsAll(CLIENT_OFF_SET));

    /** client open */
    response = clientManagerResource.clientOpen(CLIENT_OFF_STR);
    Assert.assertTrue(response.isSuccess());
  }
}
