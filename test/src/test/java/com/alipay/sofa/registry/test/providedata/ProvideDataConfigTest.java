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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import com.alipay.sofa.registry.jdbc.mapper.ProvideDataMapper;
import com.alipay.sofa.registry.jdbc.repository.impl.RecoverConfigJdbcRepository;
import com.alipay.sofa.registry.server.meta.resource.StopPushDataResource;
import com.alipay.sofa.registry.store.api.meta.RecoverConfigRepository;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaojian.xj
 * @version : ProvideDataConfigTest.java, v 0.1 2021年10月12日 16:57 xiaojian.xj Exp $
 */
@RunWith(SpringRunner.class)
public class ProvideDataConfigTest extends BaseIntegrationTest {

  private StopPushDataResource stopPushDataResource;

  private RecoverConfigRepository recoverConfigRepository;

  private ProvideDataMapper provideDataMapper;

  public static final String CLUSTER_ID = "DEFAULT_SEGMENT";
  public static final String RECOVER_CLUSTER_ID = "RECOVER_DEFAULT_SEGMENT";

  @Before
  public void beforeProvideDataTest() {

    System.setProperty("nodes.clusterId", CLUSTER_ID);
    System.setProperty("nodes.recoverClusterId", RECOVER_CLUSTER_ID);

    stopPushDataResource =
        metaApplicationContext.getBean("stopPushDataResource", StopPushDataResource.class);

    recoverConfigRepository =
        metaApplicationContext.getBean(
            "recoverConfigJdbcRepository", RecoverConfigRepository.class);

    provideDataMapper =
        metaApplicationContext.getBean("provideDataMapper", ProvideDataMapper.class);

    recoverConfigRepository.save(
        TableEnum.PROVIDE_DATA.getTableName(),
        ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID,
        RECOVER_CLUSTER_ID);
    recoverConfigRepository.save(
        TableEnum.PROVIDE_DATA.getTableName(),
        ValueConstants.PUSH_SWITCH_GRAY_OPEN_DATA_ID,
        RECOVER_CLUSTER_ID);

    ((RecoverConfigJdbcRepository) recoverConfigRepository).doRefresh();

    // refresh provide data cache
    provideDataService.becomeLeader();
    ConcurrentUtils.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testClosePush() {

    Result result = stopPushDataResource.closePush();
    Assert.assertTrue(result.isSuccess());

    ProvideDataDomain query =
        provideDataMapper.query(RECOVER_CLUSTER_ID, ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(query.getDataValue(), Boolean.TRUE.toString());

    result = stopPushDataResource.openPush();
    Assert.assertTrue(result.isSuccess());

    query =
        provideDataMapper.query(RECOVER_CLUSTER_ID, ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    Assert.assertEquals(query.getDataValue(), Boolean.FALSE.toString());
  }
}
