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
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.remoting.ServerManager;
import com.alipay.sofa.registry.client.remoting.ServerNode;
import com.alipay.sofa.registry.client.util.HttpClientUtils;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author zhiqiang.li
 * @version $Id: DirectServerManagerTest.java, v 0.1 2022-03-18 13:37 zhiqiang.li Exp $$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
public class DirectServerManagerTest {

  @Test
  public void initServerList() {
    // given
    PowerMockito.mockStatic(HttpClientUtils.class);
    RegistryClientConfig config = mock(RegistryClientConfig.class);

    // when
    when(config.getRegistryEndpoint()).thenReturn("127.0.0.1");

    // then
    ServerManager serverManager = new DirectServerManager(config);

    List<ServerNode> serverList = serverManager.getServerList();
    ServerNode node = serverManager.random();

    assertNotNull(serverList);
    assertNotNull(node);
    Assert.assertEquals(serverList.size(), 1);
    Assert.assertEquals("127.0.0.1:9600", node.getUrl());
  }
}
