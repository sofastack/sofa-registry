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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionRepositoryTest.java, v 0.1 2021年04月16日 17:18 xiaojian.xj Exp $
 */
public class AppRevisionRepositoryTest extends AbstractH2DbTestBase {

  private AppRevisionJdbcRepository appRevisionJdbcRepository;

  private InterfaceAppsJdbcRepository interfaceAppsJdbcRepository;

  private AppRevisionHeartbeatJdbcRepository appRevisionHeartbeatJdbcRepository;

  private AppRevisionMapper appRevisionMapper;

  private DefaultCommonConfig defaultCommonConfig;

  private List<AppRevision> appRevisionList;

  private static final Integer APP_REVISION_SIZE = 1;

  @Before
  public void buildAppRevision() {
    appRevisionJdbcRepository = applicationContext.getBean(AppRevisionJdbcRepository.class);
    interfaceAppsJdbcRepository = applicationContext.getBean(InterfaceAppsJdbcRepository.class);
    appRevisionHeartbeatJdbcRepository =
        applicationContext.getBean(AppRevisionHeartbeatJdbcRepository.class);
    appRevisionMapper = applicationContext.getBean(AppRevisionMapper.class);
    defaultCommonConfig = applicationContext.getBean(DefaultCommonConfig.class);

    appRevisionList = new ArrayList<>();
    for (int i = 1; i <= APP_REVISION_SIZE; i++) {
      long l = System.currentTimeMillis();
      String suffix = l + "-" + i;

      String appname = "foo" + suffix;
      String revision = "1111" + suffix;

      AppRevision appRevision = new AppRevision();
      appRevision.setAppName(appname);
      appRevision.setRevision(revision);
      appRevision.setClientVersion("1.0");

      Map<String, List<String>> baseParams = Maps.newHashMap();
      baseParams.put(
          "metaBaseParam1",
          new ArrayList<String>() {
            {
              add("metaBaseValue1");
            }
          });
      appRevision.setBaseParams(baseParams);

      Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();
      String dataInfo1 =
          DataInfo.toDataInfoId(
              "func1" + suffix, ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);
      String dataInfo2 =
          DataInfo.toDataInfoId(
              "func2" + suffix, ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);

      AppRevisionInterface inf1 = new AppRevisionInterface();
      AppRevisionInterface inf2 = new AppRevisionInterface();
      interfaceMap.put(dataInfo1, inf1);
      interfaceMap.put(dataInfo2, inf2);
      appRevision.setInterfaceMap(interfaceMap);

      inf1.setId("1");
      Map<String, List<String>> serviceParams1 = new HashMap<String, List<String>>();
      serviceParams1.put(
          "metaParam2",
          new ArrayList<String>() {
            {
              add("metaValue2");
            }
          });
      inf1.setServiceParams(serviceParams1);

      inf2.setId("2");
      Map<String, List<String>> serviceParams2 = new HashMap<String, List<String>>();
      serviceParams1.put(
          "metaParam3",
          new ArrayList<String>() {
            {
              add("metaValue3");
            }
          });
      inf1.setServiceParams(serviceParams2);

      appRevisionList.add(appRevision);
    }
  }

  @Test
  public void registerAndQuery() throws Exception {

    // register
    for (AppRevision appRevisionRegister : appRevisionList) {
      appRevisionJdbcRepository.register(appRevisionRegister);
    }

    // query app_revision
    for (AppRevision appRevisionRegister : appRevisionList) {
      AppRevision revision =
          appRevisionJdbcRepository.queryRevision(appRevisionRegister.getRevision());
      Assert.assertEquals(appRevisionRegister.getAppName(), revision.getAppName());
    }

    // query by interface
    for (AppRevision appRevisionRegister : appRevisionList) {
      for (Map.Entry<String, AppRevisionInterface> entry :
          appRevisionRegister.getInterfaceMap().entrySet()) {
        String dataInfoId = entry.getKey();
        InterfaceMapping appNames = interfaceAppsJdbcRepository.getAppNames(dataInfoId);
        Assert.assertTrue(appNames.getNanosVersion() > 0);
        Assert.assertTrue(appNames.getApps().size() == 1);
        Assert.assertTrue(appNames.getApps().contains(appRevisionRegister.getAppName()));
      }
    }
  }

  @Test
  public void revisionLoad() throws Exception {
    appRevisionJdbcRepository
        .getRevisions()
        .asMap()
        .forEach((key, value) -> appRevisionJdbcRepository.getRevisions().invalidate(key));

    registerAndQuery();

    LoadingCache<String, AppRevision> cache = appRevisionJdbcRepository.getRevisions();
    Assert.assertEquals(cache.asMap().size(), APP_REVISION_SIZE.intValue());

    for (AppRevision appRevisionRegister : appRevisionList) {
      cache.invalidate(appRevisionRegister.getRevision());
    }
    Assert.assertEquals(cache.asMap().size(), 0);

    // query app_revision
    for (AppRevision appRevisionRegister : appRevisionList) {
      AppRevision revision =
          appRevisionJdbcRepository.queryRevision(appRevisionRegister.getRevision());
      Assert.assertEquals(appRevisionRegister.getAppName(), revision.getAppName());
    }

    Assert.assertEquals(cache.asMap().size(), APP_REVISION_SIZE.intValue());
  }

  class HeartbeatRunner extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      for (AppRevision appRevision : appRevisionList) {
        boolean success = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
        if (!success) {
          try {
            appRevisionJdbcRepository.register(appRevision);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Test
  public void heartbeatClean() throws Exception {

    registerAndQuery();

    for (AppRevision appRevision : appRevisionList) {

      boolean before = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
      Assert.assertTrue(before);
      appRevisionMapper.deleteAppRevision(
          defaultCommonConfig.getClusterId(), appRevision.getRevision());

      boolean after = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
      Assert.assertTrue(after);
      AppRevisionDomain query =
          appRevisionMapper.queryRevision(
              defaultCommonConfig.getClusterId(), appRevision.getRevision());
      Assert.assertTrue(query == null);
    }
    appRevisionHeartbeatJdbcRepository.doHeartbeatCacheChecker();

    for (AppRevision appRevision : appRevisionList) {
      boolean success = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
      Assert.assertFalse(success);
    }

    ConcurrentUtils.createDaemonThread("heartbeatClean-test", new HeartbeatRunner()).start();
    Thread.sleep(3000);
    for (AppRevision appRevision : appRevisionList) {

      boolean success = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
      Assert.assertTrue(success);
      appRevisionMapper.deleteAppRevision(
          defaultCommonConfig.getClusterId(), appRevision.getRevision());
    }
  }

  @Test
  public void revisionGc() throws Exception {
    registerAndQuery();

    appRevisionHeartbeatJdbcRepository.doAppRevisionGc(0);

    for (AppRevision appRevision : appRevisionList) {
      AppRevisionDomain query =
          appRevisionMapper.queryRevision(
              defaultCommonConfig.getClusterId(), appRevision.getRevision());
      Assert.assertTrue(query == null);
    }
  }
}
