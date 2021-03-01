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
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.data.bootstrap.CommonConfig;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestBaseUtils {
    private static final AtomicLong REGISTER_ID_SEQ  = new AtomicLong();
    private static final AtomicLong CLIENT_VERSION   = new AtomicLong();

    public final static String      TEST_DATA_ID     = "testDataId";
    public final static String      TEST_DATA_INFO_ID;

    private final static String     TEST_REGISTER_ID = "testRegisterId";

    private static final AtomicLong DATA_ID_SEQ      = new AtomicLong();

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
        publisher.setRegisterId(TEST_REGISTER_ID + REGISTER_ID_SEQ.incrementAndGet());
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

    public static LocalDatumStorage newLocalStorage(String dataCenter, boolean init) {
        CommonConfig commonConfig = mock(CommonConfig.class);
        when(commonConfig.getLocalDataCenter()).thenReturn(dataCenter);
        DataServerConfig dataServerConfig = new DataServerConfig(commonConfig);
        LocalDatumStorage storage = new LocalDatumStorage();
        storage.setDataServerConfig(dataServerConfig);
        if (init) {
            for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
                storage.getSlotChangeListener().onSlotAdd(i, Slot.Role.Leader);
            }
        }
        return storage;
    }

    public static DatumCache newLocalDatumCache(String localDataCenter, boolean init) {
        DatumCache cache = new DatumCache();
        LocalDatumStorage storage = TestBaseUtils.newLocalStorage(localDataCenter, init);
        cache.setLocalDatumStorage(storage);
        cache.setDataServerConfig(storage.getDataServerConfig());
        return cache;
    }

    public static DatumSummary newDatumSummary(int pubCount) {
        final String dataId = TEST_DATA_ID + "-" + DATA_ID_SEQ.incrementAndGet();
        return newDatumSummary(pubCount, dataId);
    }

    public static DatumSummary newDatumSummary(int pubCount, String dataInfoId) {
        Map<String, RegisterVersion> versions = Maps.newHashMap();
        for (int i = 0; i < pubCount; i++) {
            final String registerId = TEST_REGISTER_ID + "-" + REGISTER_ID_SEQ.incrementAndGet();
            versions.put(registerId,
                RegisterVersion.of(CLIENT_VERSION.incrementAndGet(), System.currentTimeMillis()));
        }
        return new DatumSummary(dataInfoId, versions);
    }

    public static List<Publisher> createTestPublishers(String dataId, int count) {
        List<Publisher> list = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < count; i++) {
            list.add(createTestPublisher(dataId));
        }
        return list;
    }

    public static List<Publisher> createTestPublishers(int slotId, int count) {
        List<Publisher> list = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            Publisher p = createTestPublisher(TEST_DATA_ID + "-" + DATA_ID_SEQ.incrementAndGet()
                                              + "-" + i);
            int id = SlotFunctionRegistry.getFunc().slotOf(p.getDataInfoId());
            if (id == slotId) {
                // find the slotId
                list.add(p);
                for (int j = 1; j < count; j++) {
                    list.add(createTestPublisher(p.getDataId()));
                }
                break;
            }
        }
        return list;
    }
}
