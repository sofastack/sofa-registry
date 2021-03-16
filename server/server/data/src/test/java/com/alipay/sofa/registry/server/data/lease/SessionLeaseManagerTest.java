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
package com.alipay.sofa.registry.server.data.lease;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactoryTest;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import org.junit.Assert;
import org.junit.Test;

public class SessionLeaseManagerTest {
    @Test(expected = IllegalArgumentException.class)
    public void testValidate() {
        SessionLeaseManager slm = new SessionLeaseManager();
        slm.validateSessionLeaseSec(1);
    }

    @Test
    public void test() throws Exception {
        SessionLeaseManager slm = new SessionLeaseManager();
        SessionServerConnectionFactory ssc = new SessionServerConnectionFactory();
        slm.setSessionServerConnectionFactory(ssc);
        LocalDatumStorage storage = TestBaseUtils.newLocalStorage("testDc", true);
        slm.setLocalDatumStorage(storage);
        DataServerConfig config = storage.getDataServerConfig();
        config.setDatumCompactDelaySecs(1);
        config.setSessionLeaseSecs(1);
        slm.setDataServerConfig(config);
        ssc.registerSession(ServerEnv.PROCESS_ID,
            new SessionServerConnectionFactoryTest.MockBlotChannel(ServerEnv.IP, 1000));
        slm.renewSession(ServerEnv.PROCESS_ID);
        Assert.assertTrue(slm.contains(ServerEnv.PROCESS_ID));
        Publisher p = TestBaseUtils.createTestPublisher("dataId");
        storage.put(p);
        Assert.assertEquals(storage.get(p.getDataInfoId()).getPubMap().get(p.getRegisterId()), p);
        //wait to clean, but connection remains
        Thread.sleep(1500);
        slm.clean();
        Assert.assertTrue(slm.contains(ServerEnv.PROCESS_ID));
        Assert.assertEquals(storage.tombstoneNum(), 0);
        Assert.assertEquals(storage.get(p.getDataInfoId()).getPubMap().get(p.getRegisterId()), p);

        // reset the connections
        slm.setSessionServerConnectionFactory(new SessionServerConnectionFactory());
        Thread.sleep(1500);
        // wait to clean
        slm.clean();
        Assert.assertFalse(slm.contains(ServerEnv.PROCESS_ID));
        Assert.assertEquals(storage.tombstoneNum(), 0);
        Assert.assertEquals(storage.get(p.getDataInfoId()).publisherSize(), 0);

        // wait to compact
        Thread.sleep(1500);
        slm.clean();
        Assert.assertEquals(storage.tombstoneNum(), 0);
        Assert.assertEquals(storage.get(p.getDataInfoId()).publisherSize(), 0);
    }

    @Test
    public void testLoop() throws Exception {
        SessionLeaseManager slm = new SessionLeaseManager();
        SessionServerConnectionFactory ssc = new SessionServerConnectionFactory();
        slm.setSessionServerConnectionFactory(ssc);
        LocalDatumStorage storage = TestBaseUtils.newLocalStorage("testDc", true);
        slm.setLocalDatumStorage(storage);
        DataServerConfig config = storage.getDataServerConfig();
        config.setDatumCompactDelaySecs(1);
        config.setSessionLeaseSecs(5);
        slm.setDataServerConfig(config);
        slm.init();
        slm.renewSession(ServerEnv.PROCESS_ID);
        Assert.assertTrue(slm.contains(ServerEnv.PROCESS_ID));
        Publisher p = TestBaseUtils.createTestPublisher("dataId");
        storage.put(p);
        Assert.assertEquals(storage.get(p.getDataInfoId()).getPubMap().get(p.getRegisterId()), p);
        //wait to clean
        config.setSessionLeaseSecs(1);
        slm.setSessionServerConnectionFactory(new SessionServerConnectionFactory());
        Thread.sleep(2000);
        Assert.assertEquals(storage.tombstoneNum(), 0);
        Assert.assertEquals(storage.get(p.getDataInfoId()).publisherSize(), 0);
    }
}
