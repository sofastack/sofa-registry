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
package com.alipay.sofa.registry.server.session.providedata;

import static org.mockito.Mockito.mock;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.push.ChangeProcessor;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jiangcun.hlc@antfin.com
 * @since 2023/2/22
 */
public class FetchPushEfficiencyConfigServiceTest {

  FetchPushEfficiencyConfigService fetchPushEfficiencyConfigService;

  @Before
  public void beforeTest() {
    fetchPushEfficiencyConfigService = new FetchPushEfficiencyConfigService();
    fetchPushEfficiencyConfigService
        .setPushProcessor(mock(PushProcessor.class))
        .setChangeProcessor(mock(ChangeProcessor.class))
        .setFirePushService(mock(FirePushService.class))
        .setSessionServerConfig(mock(SessionServerConfig.class));
  }

  @Test
  public void test() {
    Assert.assertTrue(fetchPushEfficiencyConfigService.getSystemPropertyIntervalMillis() == 0);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(""), ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID, 0L)));
    Assert.assertFalse(
        fetchPushEfficiencyConfigService.doProcess(
            new FetchPushEfficiencyConfigService.SwitchStorage(0L, null),
            new ProvideData(
                new ServerDataBox(
                    "{\"changeDebouncingMillis\":1000,\"changeDebouncingMaxMillis\":3000,\"changeTaskWaitingMillis\":100,\"pushTaskWaitingMillis\":0,\"pushTaskDebouncingMillis\":500,\"regWorkWaitingMillis\":200,\"zoneSet\":[\"ALL_ZONE\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                0L)));

    Assert.assertFalse(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"xxx\":1000,\"changeDebouncingMaxMillis\":3000,\"changeTaskWaitingMillis\":100,\"pushTaskWaitingMillis\":200,\"pushTaskDebouncingMillis\":500,\"regWorkWaitingMillis\":200,\"zoneSet\":[\"ALL_ZONE\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                0L)));
    Assert.assertFalse(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"changeDebouncingMillis\":1000,\"changeDebouncingMaxMillis\":3000,\"changeTaskWaitingMillis\":100,\"pushTaskWaitingMillis\":0,\"pushTaskDebouncingMillis\":500,\"regWorkWaitingMillis\":200,\"zoneSet\":[\"ALL_ZONE\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                2L)));
    Assert.assertFalse(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"changeDebouncingMillis\":100,\"changeDebouncingMaxMillis\":1000,\"changeTaskWaitingMillis\":50,\"pushTaskWaitingMillis\":50,\"pushTaskDebouncingMillis\":10,\"regWorkWaitingMillis\":100,\"ipSet\":[\"127.0.0.1\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                5L)));
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMillis()
            == 1000);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMaxMillis()
            == 3000);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeTaskWaitingMillis()
            == 100);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskWaitingMillis()
            == 200);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskDebouncingMillis()
            == 500);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getRegWorkWaitingMillis()
            == 200);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getSbfAppPushTaskDebouncingMillis()
            == 500);

    Assert.assertTrue(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"changeDebouncingMillis\":1000,\"changeDebouncingMaxMillis\":3000,\"changeTaskWaitingMillis\":100,\"pushTaskWaitingMillis\":200,\"pushTaskDebouncingMillis\":500,\"regWorkWaitingMillis\":200,\"zoneSet\":[\"ALL_ZONE\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                2L)));
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMillis()
            == 1000);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMaxMillis()
            == 3000);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeTaskWaitingMillis()
            == 100);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskWaitingMillis()
            == 200);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskDebouncingMillis()
            == 500);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getRegWorkWaitingMillis()
            == 200);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getSbfAppPushTaskDebouncingMillis()
            == 500);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"changeDebouncingMillis\":500,\"changeDebouncingMaxMillis\":1500,\"changeTaskWaitingMillis\":50,\"pushTaskWaitingMillis\":50,\"pushTaskDebouncingMillis\":200,\"regWorkWaitingMillis\":100,\"zoneSet\":[\"ALL_ZONE\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                3L)));
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMillis()
            == 500);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMaxMillis()
            == 1500);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeTaskWaitingMillis()
            == 50);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskWaitingMillis()
            == 50);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskDebouncingMillis()
            == 200);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getRegWorkWaitingMillis()
            == 100);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getSbfAppPushTaskDebouncingMillis()
            == 500);

    Assert.assertTrue(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"changeDebouncingMillis\":100,\"changeDebouncingMaxMillis\":1000,\"changeTaskWaitingMillis\":50,\"pushTaskWaitingMillis\":50,\"pushTaskDebouncingMillis\":10,\"regWorkWaitingMillis\":100,\"zoneSet\":[\"ALL_ZONE\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                4L)));
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMillis()
            == 100);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMaxMillis()
            == 1000);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeTaskWaitingMillis()
            == 50);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskWaitingMillis()
            == 50);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskDebouncingMillis()
            == 10);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getRegWorkWaitingMillis()
            == 100);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getSbfAppPushTaskDebouncingMillis()
            == 500);

    Assert.assertTrue(
        fetchPushEfficiencyConfigService.doProcess(
            fetchPushEfficiencyConfigService.getStorage().get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"changeDebouncingMillis\":100,\"changeDebouncingMaxMillis\":1000,\"changeTaskWaitingMillis\":50,\"pushTaskWaitingMillis\":50,\"pushTaskDebouncingMillis\":10,\"regWorkWaitingMillis\":100,\"subAppSet\":[\"app1\"],\"sbfAppPushTaskDebouncingMillis\":\"10\",\"zoneSet\":[\"ALL_ZONE\"]}"),
                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                6L)));
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMillis()
            == 100);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeDebouncingMaxMillis()
            == 1000);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getChangeTaskWaitingMillis()
            == 50);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskWaitingMillis()
            == 50);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getPushTaskDebouncingMillis()
            == 10);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getRegWorkWaitingMillis()
            == 100);
    Assert.assertTrue(
        fetchPushEfficiencyConfigService
                .getStorage()
                .get()
                .pushEfficiencyImproveConfig
                .getSbfAppPushTaskDebouncingMillis()
            == 10);
  }
}
