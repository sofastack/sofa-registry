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
package com.alipay.sofa.registry.test.sync;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.dataserver.NotifyDataSyncRequest;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.DataSyncServerConnectionHandler;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xuanbei
 * @since 2019/1/16
 */
@RunWith(SpringRunner.class)
public class DataSyncTest extends BaseIntegrationTest {
    private static final int                   TEST_SYNC_PORT = 9677;
    private static DataServerConnectionFactory dataServerConnectionFactory;
    private static Server                      dataSyncServer;
    private static String                      remoteIP;

    @BeforeClass
    public static void beforeClass() throws Exception {
        startServerIfNecessary();
        BoltExchange boltExchange = (BoltExchange) dataApplicationContext.getBean("boltExchange");
        dataServerConnectionFactory = dataApplicationContext.getBean("dataServerConnectionFactory",
            DataServerConnectionFactory.class);
        DataNodeExchanger dataNodeExchanger = (DataNodeExchanger) dataApplicationContext
            .getBean("dataNodeExchanger");

        // open sync port and connect it
        dataSyncServer = boltExchange.open(new URL(NetUtil.getLocalAddress().getHostAddress(),
            TEST_SYNC_PORT), new ChannelHandler[] { new MockSyncDataHandler(),
                dataApplicationContext.getBean(DataSyncServerConnectionHandler.class) });
        remoteIP = ((BoltChannel) dataNodeExchanger.connect(new URL(LOCAL_ADDRESS, TEST_SYNC_PORT)))
            .getConnection().getLocalIP();
        Thread.sleep(500);
    }

    @Test
    public void doTest() throws Exception {
        // post sync data request
        Connection connection = dataServerConnectionFactory.getConnection(remoteIP);
        NotifyDataSyncRequest request = new NotifyDataSyncRequest(DataInfo.toDataInfoId(
            MockSyncDataHandler.dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP), LOCAL_DATACENTER,
            MockSyncDataHandler.version, DataSourceTypeEnum.SYNC.toString());
        CommonResponse commonResponse = (CommonResponse) dataSyncServer.sendSync(
            dataSyncServer.getChannel(connection.getRemoteAddress()), request, 1000);
        assertTrue(commonResponse.isSuccess());

        // register Subscriber
        SubscriberRegistration subReg = new SubscriberRegistration(MockSyncDataHandler.dataId,
            new MySubscriberDataObserver());
        subReg.setScopeEnum(ScopeEnum.global);
        registryClient1.register(subReg);

        // assert result
        Thread.sleep(1000L);
        assertEquals(MockSyncDataHandler.dataId, BaseIntegrationTest.dataId);
        assertEquals(LOCAL_REGION, userData.getLocalZone());
        assertEquals(1, userData.getZoneData().size());
        assertEquals(1, userData.getZoneData().values().size());
        assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
        assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
        assertEquals(MockSyncDataHandler.value, userData.getZoneData().get(LOCAL_REGION).get(0));

        // clear data
        clearData();
    }
}
