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
package com.alipay.sofa.registry.test.metadata;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.mapper.InterfaceAppsIndexMapper;
import com.alipay.sofa.registry.server.session.metadata.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.metadata.AppRevisionHeartbeatRegistry;
import com.alipay.sofa.registry.server.session.strategy.AppRevisionHandlerStrategy;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author xiaojian.xj
 * @version $Id: MetadataTest.java, v 0.1 2021年02月03日 19:50 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class MetadataTest extends BaseIntegrationTest {

    private AppRevisionHandlerStrategy   appRevisionHandlerStrategy;

    private AppRevisionCacheRegistry     appRevisionCacheRegistry;

    private AppRevisionHeartbeatRegistry appRevisionHeartbeatRegistry;

    private List<AppRevision>            appRevisionList;

    private InterfaceAppsIndexMapper     interfaceAppsIndexMapper;

    @Before
    public void buildAppRevision() {
        appRevisionHandlerStrategy = sessionApplicationContext.getBean(
            "appRevisionHandlerStrategy", AppRevisionHandlerStrategy.class);
        appRevisionCacheRegistry = sessionApplicationContext.getBean("appRevisionCacheRegistry",
            AppRevisionCacheRegistry.class);
        appRevisionHeartbeatRegistry = sessionApplicationContext.getBean(
            "appRevisionHeartbeatRegistry", AppRevisionHeartbeatRegistry.class);
        interfaceAppsIndexMapper = sessionApplicationContext.getBean("interfaceAppsIndexMapper",
            InterfaceAppsIndexMapper.class);

        appRevisionList = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            long l = System.currentTimeMillis();
            String suffix = l + "—" + i;

            String appname = "foo" + suffix;
            String revision = "1111" + suffix;

            AppRevision appRevision = new AppRevision();
            appRevision.setAppName(appname);
            appRevision.setRevision(revision);
            appRevision.setClientVersion("1.0");

            Map<String, List<String>> baseParams = Maps.newHashMap();
            baseParams.put("metaBaseParam1", new ArrayList<String>() {
                {
                    add("metaBaseValue1");
                }
            });
            appRevision.setBaseParams(baseParams);

            Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();
            String dataInfo1 = DataInfo.toDataInfoId("func1" + suffix,
                ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);
            String dataInfo2 = DataInfo.toDataInfoId("func2" + suffix,
                ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);

            AppRevisionInterface inf1 = new AppRevisionInterface();
            AppRevisionInterface inf2 = new AppRevisionInterface();
            interfaceMap.put(dataInfo1, inf1);
            interfaceMap.put(dataInfo2, inf2);
            appRevision.setInterfaceMap(interfaceMap);

            inf1.setId("1");
            inf1.setDataInfoId(dataInfo1);
            Map<String, List<String>> serviceParams1 = new HashMap<String, List<String>>();
            serviceParams1.put("metaParam2", new ArrayList<String>() {
                {
                    add("metaValue2");
                }
            });
            inf1.setServiceParams(serviceParams1);

            inf2.setId("2");
            inf2.setDataInfoId(dataInfo2);
            Map<String, List<String>> serviceParams2 = new HashMap<String, List<String>>();
            serviceParams1.put("metaParam3", new ArrayList<String>() {
                {
                    add("metaValue3");
                }
            });
            inf1.setServiceParams(serviceParams2);

            appRevisionList.add(appRevision);
        }

    }

    @Test
    public void register() throws ExecutionException, InterruptedException {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
        List<Future<RegisterResponse>> responses = new ArrayList<>();

        // register
        for (AppRevision appRevisionRegister : appRevisionList) {
            Future<RegisterResponse> response = fixedThreadPool.submit((Callable) () -> {
                RegisterResponse result = new RegisterResponse();
                appRevisionHandlerStrategy.handleAppRevisionRegister(appRevisionRegister, result);
                return result;
            });
            responses.add(response);
        }

        for (Future<RegisterResponse> future : responses) {
            Assert.assertTrue(future.get().isSuccess());
        }

        // query app_revision
        List<Future<AppRevision>> appRevisions = new ArrayList<>();
        for (AppRevision appRevisionRegister : appRevisionList) {
            Future appRevision = fixedThreadPool.submit((Callable) () -> {
                AppRevision revision = appRevisionCacheRegistry.getRevision(appRevisionRegister.getRevision());
                Assert.assertEquals(revision.getRevision(), appRevisionRegister.getRevision());
                return revision;
            });
            appRevisions.add(appRevision);
        }

        Map<String, AppRevision> revisionMap = new HashMap<>();
        for (Future<AppRevision> future : appRevisions) {
            AppRevision appRevision = future.get();
            revisionMap.put(appRevision.getRevision(), appRevision);
        }

        // query by interface
        List<Future<Set<String>>> appsFuture = new ArrayList<>();
        for (AppRevision appRevisionRegister : appRevisionList) {
            for (AppRevisionInterface appRevisionInterface : appRevisionRegister.getInterfaceMap().values()) {
                Future<Set<String>> submit = fixedThreadPool.submit((Callable) () -> {
                    String dataInfoId = appRevisionInterface.getDataInfoId();
                    InterfaceMapping appNames = appRevisionCacheRegistry.getAppNames(dataInfoId);
                    Assert.assertTrue(appNames.getNanosVersion() > 0);
                    Assert.assertTrue(appNames.getApps().size() == 1);
                    Assert.assertTrue(appNames.getApps().contains(appRevisionRegister.getAppName()));
                    return appNames;
                });

                appsFuture.add(submit);
            }
        }

        for (Future<Set<String>> future : appsFuture) {
            future.get();
        }


        // heartbeat
        Thread.sleep(3000);
        for (AppRevision appRevisionRegister : appRevisionList) {
            fixedThreadPool.submit(() -> {
                appRevisionHeartbeatRegistry.heartbeat(appRevisionRegister.getRevision());
            });
        }
        appRevisionHeartbeatRegistry.doRevisionHeartbeat();
    }
}