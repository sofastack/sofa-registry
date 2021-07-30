/** Alipay.com Inc. Copyright (c) 2004-2021 All Rights Reserved. */
package com.alipay.sofa.registry.test.client;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerQueryRequest;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerResp;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : QueryClientOffTest.java, v 0.1 2021年08月02日 10:12 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class QueryClientOffTest extends BaseIntegrationTest {
  private final String localAddress = sessionChannel.getLocalAddress().getHostString();
  private final String CLIENT_OFF_STR = "1.1.1.1;2.2.2.2;";
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
    Assert.assertEquals(clientManagerResp.getIps(), CLIENT_OFF_SET);

    CommonResponse result = (CommonResponse) sessionConsoleExchanger
            .request(
                    new SimpleRequest(
                            new ClientManagerQueryRequest(),
                            new URL(localAddress, sessionServerConfig.getConsolePort())))
            .getResult();
    Assert.assertTrue(result.isSuccess());
    GenericResponse resp = (GenericResponse) result;
    ClientManagerResp data = (ClientManagerResp) resp.getData();
    Assert.assertEquals(data.getIps(), CLIENT_OFF_SET);

  }
}
