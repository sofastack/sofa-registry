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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.mapper.InterfaceAppsIndexMapper;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsJdbcRepositoryTest.java, v 0.1 2021年04月12日 10:31 xiaojian.xj Exp $
 */
public class InterfaceAppsJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Autowired private InterfaceAppsRepository interfaceAppsJdbcRepository;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  private Set<String> dataCenters = Sets.newHashSet();

  @Before
  public void init() {
    dataCenters.add(defaultCommonConfig.getDefaultClusterId());
    interfaceAppsJdbcRepository.setDataCenters(dataCenters);
  }

  @Test
  public void batchSaveTest() {

    InterfaceAppsJdbcRepository impl = (InterfaceAppsJdbcRepository) interfaceAppsJdbcRepository;
    String app1 = "app1";
    String app2 = "app2";
    List<String> services = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      String service = i + "batchSaveService-" + System.currentTimeMillis();
      services.add(service);
    }
    for (String service : services) {
      impl.register(app1, Collections.singleton(service));
      impl.register(app2, Collections.singleton(service));
    }
    impl.startSynced();
    impl.waitSynced();
    for (String service : services) {
      InterfaceMapping appNames = impl.getAppNames(service);
      Assert.assertEquals(2, appNames.getApps().size());
      Assert.assertTrue(appNames.getApps().contains(app1));
      Assert.assertTrue(appNames.getApps().contains(app2));
    }
    AtomicInteger conflictCount = new AtomicInteger();
    impl.informer.setConflictCallback(
        ((current, newContainer) -> {
          conflictCount.getAndIncrement();
        }));
    InterfaceAppsIndexContainer c1 = new InterfaceAppsIndexContainer();
    for (String interfaceName : impl.informer.getContainer().interfaces()) {
      InterfaceMapping mapping = impl.getAppNames(interfaceName);
      InterfaceAppsIndexDomain domain =
          new InterfaceAppsIndexDomain(
              defaultCommonConfig.getDefaultClusterId(),
              interfaceName,
              mapping.getApps().stream().findFirst().get());
      domain.setGmtCreate(TimestampUtil.fromNanosLong(mapping.getNanosVersion() - 1));
      c1.onEntry(domain);
    }
    impl.informer.preList(impl.informer.getContainer());
    Assert.assertEquals(conflictCount.getAndSet(0), 0);
    impl.informer.preList(c1);
    Assert.assertEquals(
        conflictCount.getAndSet(0), impl.informer.getContainer().interfaces().size());
    impl.getDataVersion();
  }

  @Test(expected = RuntimeException.class)
  public void testRuntimeException() {
    InterfaceAppsIndexMapper mapper = mock(InterfaceAppsIndexMapper.class);
    when(mapper.update(anyObject()))
        .thenThrow(new SofaRegistryRuntimeException("expect exception."));

    MetadataConfig metadataConfig = mock(MetadataConfig.class);
    when(metadataConfig.getInterfaceAppsExecutorPoolSize()).thenReturn(1);
    when(metadataConfig.getInterfaceAppsExecutorQueueSize()).thenReturn(1);

    DefaultCommonConfig defaultCommonConfig = mock(DefaultCommonConfig.class);
    when(defaultCommonConfig.getClusterId(anyString())).thenReturn("DEFAULT_DATACENTER");

    InterfaceAppsJdbcRepository impl = new InterfaceAppsJdbcRepository();
    impl.setInterfaceAppsIndexMapper(mapper).setDefaultCommonConfig(defaultCommonConfig);

    String app1 = "app1";
    List<String> services =
        Lists.newArrayList("testException-service-" + System.currentTimeMillis());

    for (String service : services) {
      impl.register(app1, Collections.singleton(service));
    }
  }
}
