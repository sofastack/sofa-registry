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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.cache.CacheGenerator;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.SessionCacheService;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataCacheTest.java, v 0.1 2017-12-06 19:42 shangyu.wh Exp $
 */

public class DataCacheTest extends BaseTest {

    private AtomicLong version  = new AtomicLong(1);

    private AtomicLong registId = new AtomicLong(1000);

    @Test
    public void testGetSub() {
        SessionInterests sessionInterests = new SessionInterests();
        sessionInterests.setSessionServerConfig(new SessionServerConfigBean(null));

        String dataId = "dataid";
        String connectId = "192.168.1.2:9000";

        for (int i = 0; i < 100; i++) {
            sessionInterests.add(getSub(dataId, ScopeEnum.zone, null, null));
        }
        //add other ip
        sessionInterests.add(getSub(dataId, ScopeEnum.zone, null, new URL("192.168.1.9", 8000)));

        Map<InetSocketAddress, Map<String, Subscriber>> map = getCacheSub(
            DataInfo.toDataInfoId(dataId, "instance2", "rpc"), ScopeEnum.zone, sessionInterests);
        Assert.assertTrue(getCacheSub(sessionInterests, connectId));

        sessionInterests.deleteByConnectId(connectId);

        Map<InetSocketAddress, Map<String, Subscriber>> map2 = getCacheSub(
            DataInfo.toDataInfoId(dataId, "instance2", "rpc"), ScopeEnum.zone, sessionInterests);

        Assert.assertFalse(getCacheSub(sessionInterests, connectId));

        //map no change
        Assert.assertEquals(2, map.size());

        //get cache change,just remain 192.168.1.9:8000
        Assert.assertEquals(map2.keySet().size(), 1);
        Assert.assertEquals(NetUtil.toAddressString(map2.keySet().iterator().next()),
            "192.168.1.9:8000");
    }

    @Test
    public void testDeleteSubById() {
        SessionInterests sessionInterests = new SessionInterests();
        sessionInterests.setSessionServerConfig(new SessionServerConfigBean(null));

        String dataId = "dataid";

        for (int i = 0; i < 100; i++) {

            sessionInterests.add(getSub(dataId, ScopeEnum.zone, null, null));
        }
        //add other ip
        sessionInterests.add(getSub(dataId, ScopeEnum.zone, "xxregist123", new URL("192.168.1.9",
            8000)));
        //sessionInterests.add(getSub(dataId,ScopeEnum.zone,"xxregist456",new URL("192.168.1.10", 7000)));

        Map<InetSocketAddress, Map<String, Subscriber>> map = getCacheSub(
            DataInfo.toDataInfoId(dataId, "instance2", "rpc"), ScopeEnum.zone, sessionInterests);
        Assert.assertTrue(getCacheSub(sessionInterests, "192.168.1.9:8000"));

        sessionInterests.deleteById("xxregist123",
            DataInfo.toDataInfoId(dataId, "instance2", "rpc"));

        Map<InetSocketAddress, Map<String, Subscriber>> map2 = getCacheSub(
            DataInfo.toDataInfoId(dataId, "instance2", "rpc"), ScopeEnum.zone, sessionInterests);

        Assert.assertFalse(getCacheSub(sessionInterests, "192.168.1.9:8000"));
        //map no change
        Assert.assertEquals(2, map.size());

        //remain 100
        Assert.assertEquals(map2.keySet().size(), 2);

        InetSocketAddress address = new InetSocketAddress("192.168.1.2", 9000);
        InetSocketAddress addressDel = new InetSocketAddress("192.168.1.9", 8000);
        Assert.assertFalse(map2.get(address).isEmpty());

        Assert.assertFalse(!map2.get(addressDel).isEmpty());

        //Assert.assertEquals(NetUtil.toAddressString(map2.keySet().iterator().next()),"192.168.1.2:9000");
        Assert.assertEquals(map2.get(address).size(), 100);
    }

    @Test
    public void testGetPub() {
        SessionDataStore sessionDataStore = new SessionDataStore();
        CacheService cacheService = new SessionCacheService();

        Map<String, CacheGenerator> cacheGenerators = new HashMap<>();
        ((SessionCacheService) cacheService).setCacheGenerators(cacheGenerators);

        String dataId = "dataid";
        String connectId = "192.168.1.2:9000";
        for (int i = 0; i < 10; i++) {

            sessionDataStore.add(getPub(dataId, null, null));
        }

        Assert.assertTrue(getCachePub(sessionDataStore, connectId));
        sessionDataStore.deleteByConnectId(connectId);
        Assert.assertFalse(getCachePub(sessionDataStore, connectId));
    }

    @Test
    public void testGetPubRefresh() {
        SessionDataStore sessionDataStore = new SessionDataStore();
        CacheService cacheService = new SessionCacheService();

        Map<String, CacheGenerator> cacheGenerators = new HashMap<>();
        ((SessionCacheService) cacheService).setCacheGenerators(cacheGenerators);

        String dataId = "dataid";
        String connectId = "192.168.1.2:9000";
        int number = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(number);

        for (int i = 0; i < number; i++) {
            String connectIdss = "192.111.0.1:" + (8000 + i);
            executorService.submit(() -> {
                sessionDataStore.add(getPub(dataId, null, URL.valueOf(connectIdss)));
                Assert.assertTrue(getCachePub(sessionDataStore, connectIdss));
            });
        }

        Assert.assertFalse(getCachePub(sessionDataStore, connectId));
    }

    @Test
    public void testDelPubById() {
        SessionDataStore sessionDataStore = new SessionDataStore();
        CacheService cacheService = new SessionCacheService();

        Map<String, CacheGenerator> cacheGenerators = new HashMap<>();
        ((SessionCacheService) cacheService).setCacheGenerators(cacheGenerators);

        String dataId = "dataid";
        String connectId = "192.168.1.2:9000";
        for (int i = 0; i < 10; i++) {

            sessionDataStore.add(getPub(dataId, null, null));
        }

        sessionDataStore.add(getPub(dataId, "XXXX", new URL("192.168.1.9", 8000)));

        Assert.assertTrue(getCachePub(sessionDataStore, connectId));
        Assert.assertTrue(getCachePub(sessionDataStore, "192.168.1.9:8000"));
        sessionDataStore.deleteById("XXXX", DataInfo.toDataInfoId(dataId, "instance2", "rpc"));
        Assert.assertTrue(getCachePub(sessionDataStore, connectId));
        Assert.assertFalse(getCachePub(sessionDataStore, "192.168.1.9:8000"));
    }

    private boolean getCachePub(SessionDataStore sessionDataStore, String connectId) {
        Map map = sessionDataStore.queryByConnectId(connectId);
        return map != null && !map.isEmpty();
    }

    private boolean getCacheSub(SessionInterests sessionInterests, String connectId) {
        Map map = sessionInterests.queryByConnectId(connectId);
        return map != null && !map.isEmpty();
    }

    private Map<InetSocketAddress, Map<String, Subscriber>> getCacheSub(String dataInfoId,
                                                                        ScopeEnum scopeEnum,
                                                                        SessionInterests sessionInterests) {

        return sessionInterests.querySubscriberIndex(dataInfoId, scopeEnum);
    }

    private Subscriber getSub(String dataId, ScopeEnum scopeEnum, String registerId, URL url) {

        String processid = "4466";

        Subscriber subscriberRegister = new Subscriber();
        subscriberRegister.setAppName("app");
        subscriberRegister.setCell("My zone");
        subscriberRegister.setClientId("clientid" + version.get());
        subscriberRegister.setDataId(dataId);
        subscriberRegister.setGroup("rpc");
        subscriberRegister.setInstanceId("instance2");
        subscriberRegister.setRegisterId(registerId == null ? String.valueOf(registId
            .incrementAndGet()) : registerId);
        subscriberRegister.setProcessId(processid);
        subscriberRegister.setVersion(version.get());
        subscriberRegister.setRegisterTimestamp(System.currentTimeMillis());
        subscriberRegister.setScope(scopeEnum);
        subscriberRegister.setDataInfoId(DataInfo.toDataInfoId(dataId, "instance2", "rpc"));

        subscriberRegister.setSourceAddress(url == null ? new URL("192.168.1.2", 9000) : url);

        return subscriberRegister;
    }

    private Publisher getPub(String dataId, String registerId, URL url) {

        String processid = "4466";

        Publisher publisher = new Publisher();
        publisher.setAppName("app");
        publisher.setCell("My zone");
        publisher.setClientId("clientid" + version.get());
        publisher.setDataId(dataId);
        publisher.setGroup("rpc");
        publisher.setInstanceId("instance2");
        publisher.setRegisterId(registerId == null ? String.valueOf(registId.incrementAndGet())
            : registerId);
        publisher.setProcessId(processid);
        publisher.setVersion(version.get());
        publisher.setRegisterTimestamp(System.currentTimeMillis());
        publisher.setDataInfoId(DataInfo.toDataInfoId(dataId, "instance2", "rpc"));

        publisher.setSourceAddress(url == null ? new URL("192.168.1.2", 9000) : url);

        return publisher;
    }
}