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
import com.alipay.sofa.registry.server.session.metadata.MetadataCacheRegistry;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsJdbcRepositoryTest.java, v 0.1 2021年04月12日 10:31 xiaojian.xj Exp $
 */
public class InterfaceAppsJdbcRepositoryTest extends BaseIntegrationTest {
  private InterfaceAppsRepository interfaceAppsJdbcRepository;

  private MetadataCacheRegistry metadataCacheRegistry;

  @Before
  public void buildAppRevision() {
    interfaceAppsJdbcRepository =
        sessionApplicationContext.getBean(
            "interfaceAppsJdbcRepository", InterfaceAppsRepository.class);

    metadataCacheRegistry =
        sessionApplicationContext.getBean("metadataCacheRegistry", MetadataCacheRegistry.class);

    interfaceAppsJdbcRepository.startSynced();
  }

  @Test
  public void batchSaveTest() throws InterruptedException {

    String app = "batchSaveApp";
    List<String> services = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      services.add(i + "batchSaveService-" + System.currentTimeMillis());
    }

    HashSet<String> sets = new HashSet<>(services.subList(0, 50));
    for (String service : sets) {
      interfaceAppsJdbcRepository.register(app, Collections.singleton(service));
    }
    interfaceAppsJdbcRepository.waitSynced();
    for (String service : services) {
      InterfaceMapping appNames = metadataCacheRegistry.getAppNames(service);
      if (sets.contains(service)) {
        Assert.assertEquals(1, appNames.getApps().size());
        Assert.assertTrue(appNames.getApps().contains(app));
      } else {
        Assert.assertEquals(appNames.getNanosVersion(), -1);
      }
    }
    for (String service : services) {
      interfaceAppsJdbcRepository.register(app, Collections.singleton(service));
    }
    interfaceAppsJdbcRepository.waitSynced();
    for (String service : services) {
      InterfaceMapping appNames = metadataCacheRegistry.getAppNames(service);

      Assert.assertEquals(1, appNames.getApps().size());
      Assert.assertTrue(appNames.getApps().contains(app));
    }
  }
}
