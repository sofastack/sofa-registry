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
package com.alipay.sofa.registry.client.provider;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.remoting.ServerManager;
import com.alipay.sofa.registry.client.remoting.ServerNode;
import com.alipay.sofa.registry.client.util.HttpClientUtils;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author zhuoyu.sjw
 * @version $Id: DefaultServerManagerTest.java, v 0.1 2018-03-23 14:54 zhuoyu.sjw Exp $$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
public class DefaultServerManagerTest {

  @Test
  public void initServerList() throws Exception {
    // given
    PowerMockito.mockStatic(HttpClientUtils.class);
    RegistryClientConfig config = mock(RegistryClientConfig.class);

    // when
    when(config.getSyncConfigRetryInterval()).thenReturn(100);
    when(HttpClientUtils.get(
            anyString(), anyMapOf(String.class, String.class), any(RegistryClientConfig.class)))
        .thenReturn("127.0.0.1:9600;127.0.0.2:9600");

    // then
    ServerManager serverManager = new DefaultServerManager(config);

    List<ServerNode> serverList = serverManager.getServerList();

    assertNotNull(serverList);

    Thread.sleep(450);

    // verify
    PowerMockito.verifyStatic(times(4));
  }
}
