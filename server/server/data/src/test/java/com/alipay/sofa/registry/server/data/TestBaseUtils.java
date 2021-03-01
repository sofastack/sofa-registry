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
package com.alipay.sofa.registry.server.data;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import org.junit.Assert;

import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestBaseUtils {
    private static final AtomicLong REGISTER_ID_SEQ = new AtomicLong();
    private static final AtomicLong CLIENT_VERSION  = new AtomicLong();

    public final static String      TEST_DATA_ID    = "testDataId";
    public final static String      TEST_DATA_INFO_ID;

    static {
        Publisher p = TestBaseUtils.createTestPublisher(TEST_DATA_ID);
        TEST_DATA_INFO_ID = p.getDataInfoId();
    }

    private TestBaseUtils() {
    }

    public static Publisher createTestPublisher(String dataId) {
        Publisher publisher = new Publisher();
        DataInfo dataInfo = DataInfo.valueOf(DataInfo.toDataInfoId(dataId, "I", "G"));
        publisher.setDataInfoId(dataInfo.getDataInfoId());
        publisher.setDataId(dataInfo.getDataId());
        publisher.setInstanceId(dataInfo.getInstanceId());
        publisher.setGroup(dataInfo.getGroup());
        publisher.setRegisterId("testRegisterId" + REGISTER_ID_SEQ.incrementAndGet());
        publisher.setSessionProcessId(ServerEnv.PROCESS_ID);
        publisher.setRegisterTimestamp(System.currentTimeMillis());
        publisher.setVersion(100 + CLIENT_VERSION.incrementAndGet());
        ConnectId connectId = ConnectId.of(ServerEnv.PROCESS_ID.getHostAddress() + ":9999",
            ServerEnv.PROCESS_ID.getHostAddress() + ":9998");
        publisher.setSourceAddress(URL.valueOf(connectId.clientAddress()));
        publisher.setTargetAddress(URL.valueOf(connectId.sessionAddress()));
        return publisher;
    }

    public static Publisher cloneBase(Publisher publisher) {
        Publisher clone = TestBaseUtils.createTestPublisher(publisher.getDataId());
        clone.setRegisterId(publisher.getRegisterId());
        clone.setVersion(publisher.getVersion());
        clone.setRegisterTimestamp(publisher.getRegisterTimestamp());
        clone.setSessionProcessId(publisher.getSessionProcessId());
        return clone;
    }

    public static void assertEquals(Datum datum, Publisher publisher) {
        Assert.assertEquals(publisher.getDataInfoId(), datum.getDataInfoId());
        Assert.assertEquals(publisher.getDataId(), datum.getDataId());
        Assert.assertEquals(publisher.getInstanceId(), datum.getInstanceId());
        Assert.assertEquals(publisher.getGroup(), datum.getGroup());
        Assert.assertTrue(datum.getPubMap().containsKey(publisher.getRegisterId()));
    }

    public static ConnectId notExistConnectId() {
        return ConnectId.of("notExist:9999", "notExist:9998");
    }

    public static LocalDatumStorage getLocalStorage(String dataCenter) {
        DataServerConfig dataServerConfig = mock(DataServerConfig.class);
        when(dataServerConfig.getLocalDataCenter()).thenReturn(dataCenter);
        LocalDatumStorage storage = new LocalDatumStorage();
        storage.setDataServerConfig(dataServerConfig);
        return storage;
    }
}
