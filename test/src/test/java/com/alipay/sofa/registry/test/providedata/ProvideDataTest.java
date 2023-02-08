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
package com.alipay.sofa.registry.test.providedata;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataTest.java, v 0.1 2021年03月15日 15:32 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class ProvideDataTest extends BaseIntegrationTest {

  private ProvideDataRepository provideDataRepository;

  private ProvideDataService provideDataService;

  @Before
  public void beforeProvideDataTest() {
    MockitoAnnotations.initMocks(this);

    provideDataRepository =
        metaApplicationContext.getBean("provideDataJdbcRepository", ProvideDataRepository.class);

    provideDataService =
        metaApplicationContext.getBean("provideDataService", ProvideDataService.class);
  }

  @Test
  public void testProvideData() throws InterruptedException {

    String key = "keyA" + System.currentTimeMillis();
    String value = "valueA" + System.currentTimeMillis();

    String dataInfoId = DataInfo.toDataInfoId(key, "DEFAULT", "DEFAULT");
    PersistenceData data = PersistenceDataBuilder.createPersistenceData(dataInfoId, value);
    boolean save = provideDataService.saveProvideData(data);
    Assert.assertTrue(save);
    Assert.assertEquals(
        value, provideDataService.queryProvideData(dataInfoId).getEntity().getData());

    PersistenceData newData = new PersistenceData();
    BeanUtils.copyProperties(data, newData);
    long exceptVersion = newData.getVersion();
    newData.setData("new valueA");
    newData.setVersion(System.currentTimeMillis());
    boolean put = provideDataRepository.put(newData, exceptVersion);
    Assert.assertTrue(put);

    provideDataService.becomeLeader();
    Thread.sleep(5000);
    Assert.assertEquals(
        newData.getData(), provideDataService.queryProvideData(dataInfoId).getEntity().getData());
  }
}
