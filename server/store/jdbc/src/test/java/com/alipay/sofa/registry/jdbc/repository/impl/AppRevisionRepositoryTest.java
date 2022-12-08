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
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.TestUtils;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionRepositoryTest.java, v 0.1 2021年04月16日 17:18 xiaojian.xj Exp $
 */
public class AppRevisionRepositoryTest extends AbstractH2DbTestBase {

  @Autowired private AppRevisionRepository appRevisionJdbcRepository;

  @Autowired private InterfaceAppsRepository interfaceAppsJdbcRepository;

  @Autowired private AppRevisionMapper appRevisionMapper;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  private List<AppRevision> appRevisionList;

  private static final int APP_REVISION_SIZE = 100;
  private Set<String> dataCenters = Sets.newHashSet();

  @Before
  public void buildAppRevision() {
    ((AppRevisionJdbcRepository) appRevisionJdbcRepository).init();
    appRevisionList = buildAppRevisions(APP_REVISION_SIZE);

    dataCenters.add(defaultCommonConfig.getDefaultClusterId());
    appRevisionJdbcRepository.setDataCenters(dataCenters);
    interfaceAppsJdbcRepository.setDataCenters(dataCenters);

    appRevisionJdbcRepository.startSynced();
    interfaceAppsJdbcRepository.startSynced();
  }

  private void register() throws Exception {
    // register
    for (AppRevision appRevisionRegister : appRevisionList) {
      appRevisionJdbcRepository.register(appRevisionRegister);
    }
  }

  private void queryAndCheck() {
    // query app_revision
    for (AppRevision appRevisionRegister : appRevisionList) {
      AppRevision revision =
          appRevisionJdbcRepository.queryRevision(appRevisionRegister.getRevision());
      Assert.assertEquals(appRevisionRegister.getAppName(), revision.getAppName());
    }
    interfaceAppsJdbcRepository.waitSynced();
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
  public void registerAndQuery() throws Exception {
    register();
    queryAndCheck();
  }

  @Test
  public void revisionLoad() throws Exception {
    AppRevisionJdbcRepository repository = (AppRevisionJdbcRepository) appRevisionJdbcRepository;

    register();
    queryAndCheck();

    LoadingCache<String, AppRevision> cache = repository.getRevisions();
    Assert.assertEquals(cache.asMap().size(), APP_REVISION_SIZE);

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

    Assert.assertEquals(cache.asMap().size(), APP_REVISION_SIZE);
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
    appRevisionJdbcRepository.waitSynced();
    ((AppRevisionJdbcRepository) appRevisionJdbcRepository).cleanCache();

    for (AppRevision appRevision : appRevisionList) {
      boolean before = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
      Assert.assertTrue(before);
      List<AppRevisionDomain> querys =
          appRevisionMapper.queryRevision(dataCenters, appRevision.getRevision());
      Assert.assertTrue(!CollectionUtils.isEmpty(querys));
    }

    for (AppRevision appRevision : appRevisionList) {
      AppRevisionDomain domain =
          AppRevisionDomainConvertor.convert2Domain(
              defaultCommonConfig.getDefaultClusterId(), appRevision);
      domain.setDeleted(true);
      appRevisionMapper.replace(domain);
    }
    appRevisionJdbcRepository.waitSynced();
    for (AppRevision appRevision : appRevisionList) {
      boolean after = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
      Assert.assertFalse(after);
      TestUtils.assertException(
          UncheckedExecutionException.class,
          () -> appRevisionJdbcRepository.queryRevision(appRevision.getRevision()));
    }
    ConcurrentUtils.createDaemonThread("heartbeatClean-test", new HeartbeatRunner()).start();
    ((AppRevisionJdbcRepository) appRevisionJdbcRepository).cleanCache();
    Thread.sleep(3000);
    for (AppRevision appRevision : appRevisionList) {
      boolean success = appRevisionJdbcRepository.heartbeat(appRevision.getRevision());
      Assert.assertTrue(success);
      AppRevisionDomain domain =
          AppRevisionDomainConvertor.convert2Domain(
              defaultCommonConfig.getDefaultClusterId(), appRevision);
      domain.setDeleted(true);
      appRevisionMapper.replace(domain);
    }
  }

  @Test
  public void testCountByApp() throws Exception {
    register();
    appRevisionJdbcRepository.waitSynced();
    Map<String, Integer> counts = appRevisionJdbcRepository.countByApp();
    Assert.assertEquals(APP_REVISION_SIZE, counts.size());
  }
}
