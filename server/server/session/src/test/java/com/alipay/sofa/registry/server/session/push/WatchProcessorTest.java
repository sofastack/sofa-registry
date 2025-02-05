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
package com.alipay.sofa.registry.server.session.push;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.alipay.remoting.rpc.exception.InvokeTimeoutException;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import org.junit.Assert;
import org.junit.Test;

public class WatchProcessorTest {
  @Test
  public void test() {
    WatchProcessor processor = new WatchProcessor();
    SessionServerConfigBean sessionServerConfigBean = TestUtils.newSessionConfig("testDc");
    processor.sessionServerConfig = sessionServerConfigBean;
    processor.clientNodeService = mock(ClientNodeService.class);
    processor.pushDataGenerator = new PushDataGenerator();
    processor.pushDataGenerator.sessionServerConfig = sessionServerConfigBean;
    processor.pushSwitchService = mock(PushSwitchService.class);
    Watcher w = TestUtils.newWatcher("test-watch");
    ReceivedConfigData data =
        ReceivedDataConverter.createReceivedConfigData(
            w, new ProvideData(null, w.getDataInfoId(), 100L));

    Assert.assertTrue(w.updatePushedVersion(100));

    Assert.assertFalse(processor.doExecuteOnWatch(w, data, System.currentTimeMillis()));

    when(processor.pushSwitchService.canIpPushLocal(anyString())).thenReturn(true);
    Assert.assertFalse(processor.doExecuteOnWatch(w, data, System.currentTimeMillis()));

    data =
        ReceivedDataConverter.createReceivedConfigData(
            w, new ProvideData(null, w.getDataInfoId(), 101L));
    Assert.assertTrue(processor.doExecuteOnWatch(w, data, System.currentTimeMillis()));
    verify(processor.clientNodeService, times(1)).pushWithCallback(anyObject(), any(), any());

    // test callback
    long now = System.currentTimeMillis();
    WatchProcessor.WatchPushCallback callback = processor.new WatchPushCallback(now, w, 200);
    Assert.assertNotNull(callback.getExecutor());

    callback.onCallback(null, null);
    Assert.assertEquals(200, w.getPushedVersion());

    callback.onException(null, new InvokeTimeoutException());
    TestUtils.MockBlotChannel channel = TestUtils.newChannel(9999, "127.0.0.1", 8888);
    callback.onException(channel, new RuntimeException());

    channel.setActive(false);
    callback.onException(channel, new RuntimeException());
  }
}
