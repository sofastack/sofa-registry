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
package com.alipay.sofa.registry.server.session.metadata;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : MetadataCacheRegistryTest.java, v 0.1 2022年08月03日 18:02 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataCacheRegistryTest extends AbstractSessionServerTestBase {

  @InjectMocks private static MetadataCacheRegistry metadataCacheRegistry;

  @Spy private InMemoryAppRevisionRepository appRevisionRepository;

  @Spy private InMemoryInterfaceAppsRepository interfaceAppsRepository;

  @Mock private ExecutorManager executorManager;

  private static List<AppRevision> appRevisionList;

  private static final Integer APP_REVISION_SIZE = 10;

  private static final ThreadPoolExecutor EXECUTOR =
      new MetricsableThreadPoolExecutor(
          "APP_REVISION_REGISTER_EXECUTOR",
          100,
          100,
          60,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<>(1),
          new NamedThreadFactory("APP_REVISION_REGISTER_EXECUTOR", true),
          new ThreadPoolExecutor.CallerRunsPolicy());

  @Before
  public void before() {
    appRevisionList = buildAppRevisions(APP_REVISION_SIZE, "MetadataCacheRegistryTest-testRegister-");
    appRevisionRepository.setInterfaceAppsRepository(interfaceAppsRepository);

    when(executorManager.getAppRevisionRegisterExecutor()).thenReturn(EXECUTOR);
    metadataCacheRegistry.init();
    metadataCacheRegistry.waitSynced();
  }

  @Test
  public void testRegister() throws InterruptedException, TimeoutException {
    for (AppRevision revision : appRevisionList) {
      metadataCacheRegistry.register(revision);
    }

    waitConditionUntilTimeOut(MetadataCacheRegistryTest::check, 3000);

    for (AppRevision revision : appRevisionList) {
      AppRevision rev = metadataCacheRegistry.getRevision(revision.getRevision());
      Assert.assertNotNull(rev);
    }
  }

  private static boolean check() {

    try {
      for (AppRevision appRevisionRegister : appRevisionList) {
        for (Map.Entry<String, AppRevisionInterface> entry :
            appRevisionRegister.getInterfaceMap().entrySet()) {
          String dataInfoId = entry.getKey();
          InterfaceMapping appNames = metadataCacheRegistry.getAppNames(dataInfoId);
          Assert.assertTrue(appNames.getNanosVersion() > 0);
          Assert.assertTrue(appNames.getApps().size() == 1);
          Assert.assertTrue(appNames.getApps().contains(appRevisionRegister.getAppName()));
        }
      }
      return true;
    } catch (Throwable th) {
      return false;
    }
  }

  @Test
  public void testRegisterException() throws Exception {
    MetadataCacheRegistry metadataCacheRegistry = new MetadataCacheRegistry();

    AppRevisionRepository mockAppRevisionRepository = mock(AppRevisionRepository.class);

    metadataCacheRegistry
        .setAppRevisionRepository(mockAppRevisionRepository)
        .setInterfaceAppsRepository(interfaceAppsRepository)
        .setExecutorManager(executorManager);

    metadataCacheRegistry.init();
    doThrow(new SofaRegistryRuntimeException("expected exception"))
        .when(mockAppRevisionRepository)
        .register(any());

    for (AppRevision revision : appRevisionList) {
      metadataCacheRegistry.register(revision);
    }

    Thread.sleep(1000);
    for (AppRevision appRevisionRegister : appRevisionList) {
      for (Map.Entry<String, AppRevisionInterface> entry :
          appRevisionRegister.getInterfaceMap().entrySet()) {
        String dataInfoId = entry.getKey();
        InterfaceMapping appNames = metadataCacheRegistry.getAppNames(dataInfoId);
        Assert.assertNull(appNames);
      }
    }

    metadataCacheRegistry.setAppRevisionRepository(appRevisionRepository);

    waitConditionUntilTimeOut(MetadataCacheRegistryTest::check, 3000);
  }
}
