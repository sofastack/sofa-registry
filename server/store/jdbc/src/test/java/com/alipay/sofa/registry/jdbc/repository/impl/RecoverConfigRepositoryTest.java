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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfigBean;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : RecoverConfigRepository.java, v 0.1 2021年10月08日 20:55 xiaojian.xj Exp $
 */
public class RecoverConfigRepositoryTest extends AbstractH2DbTestBase {

  @Resource private RecoverConfigJdbcRepository recoverConfigJdbcRepository;

  @Resource private DefaultCommonConfigBean defaultCommonConfig;

  @Autowired private ProvideDataRepository provideDataRepository;

  @Test
  public void testSaveConfig() {
    defaultCommonConfig.setClusterId(CLUSTER_ID);
    defaultCommonConfig.setRecoverClusterId(CLUSTER_ID);

    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, "false");

    PersistenceData exist = provideDataRepository.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    long expectVersion = exist == null ? 0 : exist.getVersion();
    boolean put = provideDataRepository.put(persistenceData, expectVersion);
    Assert.assertTrue(put);

    recoverConfigJdbcRepository.save(
        "provide_data", ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, RECOVER_CLUSTER_ID);
    recoverConfigJdbcRepository.save("app_revision", "all", RECOVER_CLUSTER_ID);
    recoverConfigJdbcRepository.doRefresh();

    Set<String> data = recoverConfigJdbcRepository.queryKey("provide_data");
    Assert.assertTrue(data.contains(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID));
    Assert.assertFalse(data.contains(ValueConstants.BLACK_LIST_DATA_ID));
    data = recoverConfigJdbcRepository.queryKey("app_revision");
    Assert.assertTrue(data.contains("all"));

    Map<String, PersistenceData> all = provideDataRepository.getAll();
    Assert.assertEquals(all.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID).getData(), "false");

    defaultCommonConfig.setClusterId(CLUSTER_ID);
    defaultCommonConfig.setRecoverClusterId(RECOVER_CLUSTER_ID);

    all = provideDataRepository.getAll();
    Assert.assertEquals(all.get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID).getData(), "false");

    recoverConfigJdbcRepository.remove(
        "provide_data", ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    recoverConfigJdbcRepository.doRefresh();
    data = recoverConfigJdbcRepository.queryKey("provide_data");
    Assert.assertTrue(CollectionUtils.isEmpty(data));
  }
}
