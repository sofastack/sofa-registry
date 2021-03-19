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
package com.alipay.sofa.registry.client.base;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.client.MockServer;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClient;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfig;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import com.alipay.sofa.registry.client.util.HttpClientUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author zhuoyu.sjw
 * @version $Id: BaseTest.java, v 0.1 2018-03-24 14:58 zhuoyu.sjw Exp $$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
public abstract class BaseTest {

  protected DefaultRegistryClient registryClient;

  protected String appName = "registry-test";

  protected String dataCenter = "HZ";

  protected String instanceId = "testInstanceId";

  protected MockServer mockServer;

  @Before
  public void setUp() throws Exception {
    mockServer = new MockServer();
    mockServer.start();

    PowerMockito.mockStatic(HttpClientUtils.class);
    when(HttpClientUtils.get(
            anyString(), anyMapOf(String.class, String.class), any(RegistryClientConfig.class)))
        .thenReturn(mockServer.getIp() + ":" + mockServer.getPort());

    DefaultRegistryClientConfig config =
        DefaultRegistryClientConfigBuilder.start()
            .setAppName(appName)
            .setDataCenter(dataCenter)
            .setInstanceId(instanceId)
            .setRegistryEndpoint(mockServer.getIp())
            .build();
    registryClient = new DefaultRegistryClient(config);
    registryClient.init();
  }
}
