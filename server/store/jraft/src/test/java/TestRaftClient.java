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
import java.util.concurrent.TimeoutException;

import com.alipay.sofa.jraft.*;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.jraft.rpc.impl.cli.BoltCliClientService;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author shangyu.wh
 * @version 1.0: TestServiceStateMachine.java, v 0.1 2019-08-01 15:16 shangyu.wh Exp $
 */

@RunWith(PowerMockRunner.class)
public class TestRaftClient {

    @Test
    @PrepareForTest(RouteTable.class)
    public void testRefreshConfiguration() throws TimeoutException, InterruptedException {
        mockStatic(RouteTable.class);
        RouteTable mockTable = mock(RouteTable.class);
        when(RouteTable.getInstance()).thenReturn(mockTable);
        Configuration conf1 = JRaftUtils.getConfiguration("127.0.0.1:9615,127.0.0.2:9615");
        Configuration conf2 = JRaftUtils.getConfiguration("127.0.0.2:9615,127.0.0.1:9615");
        Configuration conf3 = JRaftUtils.getConfiguration("127.0.0.2:9615,127.0.0.3:9615");
        String groupID = "default";
        int timeout = 10;
        CliClientService cliClientService = mock(BoltCliClientService.class);
        when(mockTable.getConfiguration(groupID)).thenReturn(conf1);
        when(mockTable.refreshConfiguration(cliClientService, groupID, timeout)).thenReturn(
            Status.OK());
        RaftClient.refreshConfiguration(cliClientService, groupID, conf2, timeout);
        verify(mockTable, never()).refreshConfiguration(cliClientService, groupID, timeout);

        RaftClient.refreshConfiguration(cliClientService, groupID, conf3, timeout);
        verify(mockTable, times(1)).refreshConfiguration(cliClientService, groupID, timeout);
    }
}
