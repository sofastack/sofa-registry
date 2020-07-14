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
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.jraft.rpc.impl.cli.BoltCliClientService;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.jraft.command.ConfigurationCommitted;
import com.alipay.sofa.registry.jraft.handler.ConfigurationCommittedHandler;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author xiangxu
 * @version : TestConfigurationCommittedHandler.java, v 0.1 2020年07月15日 12:01 下午 xiangxu Exp $
 */

@RunWith(PowerMockRunner.class)
public class TestConfigurationCommittedHandler {

    @Test
    @PrepareForTest(RaftClient.class)
    public void testRefresh() throws Exception {
        mockStatic(RaftClient.class);
        doNothing().when(RaftClient.class, "refreshConfiguration", any(CliClientService.class),
            any(String.class), any(Configuration.class), anyInt());

        ConfigurationCommittedHandler handler = new ConfigurationCommittedHandler("default", null);
        BoltChannel channel = new BoltChannel();
        ConfigurationCommitted msg = new ConfigurationCommitted("127.0.0.1:8081");
        handler.reply(channel, msg);
        PowerMockito.verifyStatic(never());

        BoltCliClientService mockCliService = mock(BoltCliClientService.class);
        ConfigurationCommittedHandler handler2 = new ConfigurationCommittedHandler("default",
            mockCliService);
        handler2.reply(channel, msg);
        PowerMockito.verifyStatic(times(1));
    }
}