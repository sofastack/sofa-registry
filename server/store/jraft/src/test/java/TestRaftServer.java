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
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.core.NodeImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.rpc.RaftClientService;
import com.alipay.sofa.jraft.rpc.impl.core.BoltRaftClientService;
import com.alipay.sofa.registry.jraft.bootstrap.RaftServer;
import com.alipay.sofa.registry.jraft.bootstrap.RaftServerConfig;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.bolt.BoltServer;
import com.alipay.sofa.registry.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author shangyu.wh
 * @version 1.0: TestServiceStateMachine.java, v 0.1 2019-08-01 15:16 shangyu.wh Exp $
 */

@RunWith(PowerMockRunner.class)
public class TestRaftServer {
    @Before
    public void setup() throws Exception {
        mockStatic(FileUtils.class);
        NodeImpl mockNode = mock(NodeImpl.class);
        RaftGroupService mockService = mock(RaftGroupService.class);
        RpcServer rpcServer = mock(RpcServer.class);
        BoltServer mockBoltServer = mock(BoltServer.class);
        BoltRaftClientService mockClientService = mock(BoltRaftClientService.class);
        RpcClient mockRpcClient = mock(RpcClient.class);
        BoltChannel mockChannel = mock(BoltChannel.class);

        whenNew(RaftGroupService.class).withAnyArguments().thenReturn(mockService);
        when(mockService.start()).thenReturn(mockNode);
        doNothing().when(FileUtils.class, "forceMkdir", any(File.class));
        whenNew(BoltServer.class).withAnyArguments().thenReturn(mockBoltServer);
        doNothing().when(mockBoltServer).initServer();
        when(mockBoltServer.getRpcServer()).thenReturn(rpcServer);
        when(mockNode.getRpcService()).thenReturn(mockClientService);
        when(mockClientService.getRpcClient()).thenReturn(mockRpcClient);

        Collection<Channel> channels = new ArrayList<>();
        channels.add(mockChannel);
        when(mockBoltServer.getChannels()).thenReturn(channels);
    }

    @Test
    @PrepareForTest({ FileUtils.class, RaftServer.class })
    public void testSendRequest() throws Exception {

        String path = "/tmp/123";
        RaftServer server = new RaftServer(path, "default", "127.0.0.1:8081", "127.0.0.1:8081");
        server.start(new RaftServerConfig());
        PeerId leader = PeerId.parsePeer("127.0.0.1:8081");

        server.sendLeaderChangeNotify(leader, "leader");
        server.sendConfigurationCommittedNotify(JRaftUtils.getConfiguration("127.0.0.1:8081"));
    }
}
