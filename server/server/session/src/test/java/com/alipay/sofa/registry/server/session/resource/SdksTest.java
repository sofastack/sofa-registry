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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.CollectionSdks;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.sessionserver.ClientOffRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.remoting.console.SessionConsoleExchanger;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SdksTest {
  @Test
  public void toIpListTest() {
    List<String> list = CollectionSdks.toIpList("111 ; 222,dd ");
    Assert.assertEquals(2, list.size());
    Assert.assertEquals(list.get(0), "111");
    Assert.assertEquals(list.get(1), "222,dd");
  }

  @Test
  public void toIpSetTest() {
    Set<String> set = CollectionSdks.toIpSet("111 ; 222,dd ");
    Assert.assertEquals(2, set.size());
    Assert.assertTrue(set.contains("111"));
    Assert.assertTrue(set.contains("222,dd"));
  }

  @Test
  public void getFailedResponseIfAbsent() {
    List<CommonResponse> responses =
        Lists.newArrayList(
            CommonResponse.buildSuccessResponse(), CommonResponse.buildSuccessResponse());
    Assert.assertTrue(Sdks.getFailedResponseIfAbsent(responses).isSuccess());

    responses =
        Lists.newArrayList(
            CommonResponse.buildSuccessResponse(), CommonResponse.buildFailedResponse("test"));
    Assert.assertFalse(Sdks.getFailedResponseIfAbsent(responses).isSuccess());
  }

  @Test
  public void getOtherConsoleServers() {
    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc", "testRegion");
    MetaServerService metaServerService = Mockito.mock(MetaServerService.class);
    Mockito.when(metaServerService.getSessionServerList(Mockito.anyString()))
        .thenReturn(Lists.newArrayList(ServerEnv.IP, "192.168.1.1"));
    List<URL> urls = Sdks.getOtherConsoleServers(null, configBean, metaServerService);
    Assert.assertEquals(urls.size(), 1);
    Assert.assertEquals(urls.get(0).getIpAddress(), "192.168.1.1");
    Assert.assertEquals(urls.get(0).getPort(), configBean.getConsolePort());
  }

  @Test
  public void testExec() {
    // test the exception handle
    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc", "testRegion");
    SessionConsoleExchanger exchanger = TestUtils.newSessionConsoleExchanger(configBean);
    List<String> ips = Lists.newArrayList("1,2");
    ClientOffRequest req = new ClientOffRequest(ips);
    Assert.assertEquals(req.getIps(), ips);
    Sdks.SdkExecutor exec =
        new Sdks.SdkExecutor() {
          @Override
          public CommonResponse execute(URL url) throws Exception {
            exchanger.requestRaw("192.168.168.1", req).getResult();
            Assert.fail();
            return null;
          }
        };
    Assert.assertTrue(Sdks.exec(exec, null).isSuccess());

    exec =
        new Sdks.SdkExecutor() {
          @Override
          public CommonResponse execute(URL url) throws Exception {
            throw new RuntimeException();
          }
        };
    Assert.assertFalse(Sdks.exec(exec, null).isSuccess());
    ExecutorService pool = Executors.newSingleThreadExecutor();
    Map<URL, CommonResponse> map =
        Sdks.concurrentSdkSend(pool, Lists.newArrayList(new URL("192.168.1.1", 2299)), exec, 5000);
    Assert.assertTrue(map.size() > 0);
    for (CommonResponse resp : map.values()) {
      Assert.assertFalse(resp.isSuccess());
    }
  }
}
