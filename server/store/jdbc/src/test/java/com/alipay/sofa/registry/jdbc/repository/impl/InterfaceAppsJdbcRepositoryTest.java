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
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsJdbcRepositoryTest.java, v 0.1 2021年04月12日 10:31 xiaojian.xj Exp $
 */
public class InterfaceAppsJdbcRepositoryTest extends AbstractH2DbTestBase {
  private InterfaceAppsJdbcRepository interfaceAppsJdbcRepository;

  @Before
  public void build() {
    interfaceAppsJdbcRepository = applicationContext.getBean(InterfaceAppsJdbcRepository.class);
  }

  @Test
  public void batchSaveTest() {

    String app = "batchSaveApp";
    List<String> services = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      services.add(i + "batchSaveService-" + System.currentTimeMillis());
    }

    interfaceAppsJdbcRepository.batchSave(app, new HashSet<>(services));
    for (String service : services) {
      InterfaceMapping appNames = interfaceAppsJdbcRepository.getAppNames(service);
      Assert.assertEquals(1, appNames.getApps().size());
      Assert.assertTrue(appNames.getApps().contains(app));
    }
  }

  @Test
  public void loadMetadataTest() {
    String app = "loadMetadataTest";
    List<String> services = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      services.add(i + "loadMetadataTest-" + System.currentTimeMillis());
    }

    Map<String, InterfaceMapping> interfaceApps = interfaceAppsJdbcRepository.getInterfaceApps();
    interfaceAppsJdbcRepository.batchSave(app, new HashSet<>(services));
    Assert.assertEquals(interfaceApps.size(), 0);

    interfaceAppsJdbcRepository.loadMetadata();

    for (String service : services) {
      InterfaceMapping interfaceMapping = interfaceApps.get(service);
      Assert.assertEquals(interfaceMapping.getApps().size(), 1);
      Assert.assertTrue(interfaceMapping.getApps().contains(app));
    }
  }
}
