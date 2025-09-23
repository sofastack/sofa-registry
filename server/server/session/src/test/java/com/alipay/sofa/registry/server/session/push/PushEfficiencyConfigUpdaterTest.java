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
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.server.session.resource.ClientManagerResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author huicha
 * @date 2025/7/24
 */
public class PushEfficiencyConfigUpdaterTest {

  @Test
  public void testUpdateFromProviderData() {
    ChangeProcessor changeProcessor = Mockito.mock(ChangeProcessor.class);
    PushProcessor pushProcessor = Mockito.mock(PushProcessor.class);
    FirePushService firePushService = Mockito.mock(FirePushService.class);
    ClientManagerResource clientManagerResource = Mockito.mock(ClientManagerResource.class);

    PushEfficiencyConfigUpdater pushEfficiencyConfigUpdater = new PushEfficiencyConfigUpdater();
    pushEfficiencyConfigUpdater.setChangeProcessor(changeProcessor);
    pushEfficiencyConfigUpdater.setPushProcessor(pushProcessor);
    pushEfficiencyConfigUpdater.setFirePushService(firePushService);
    pushEfficiencyConfigUpdater.setClientManagerResource(clientManagerResource);

    // 更新没有开启自动化配置，因此预期是 null
    pushEfficiencyConfigUpdater.updateFromProviderData(new PushEfficiencyImproveConfig());

    AutoPushEfficiencyRegulator autoPushEfficiencyRegulator =
        pushEfficiencyConfigUpdater.getAutoPushEfficiencyRegulator();
    Assert.assertNull(autoPushEfficiencyRegulator);

    // 第二次开启自动化配置
    AutoPushEfficiencyConfig autoPushEfficiencyConfig = new AutoPushEfficiencyConfig();
    autoPushEfficiencyConfig.setEnableAutoPushEfficiency(true);
    autoPushEfficiencyConfig.setEnableDebouncingTime(true);
    autoPushEfficiencyConfig.setEnableMaxDebouncingTime(true);
    autoPushEfficiencyConfig.setWindowTimeMillis(10);

    PushEfficiencyImproveConfig pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    pushEfficiencyImproveConfig.setAutoPushEfficiencyConfig(autoPushEfficiencyConfig);

    pushEfficiencyConfigUpdater.updateFromProviderData(pushEfficiencyImproveConfig);

    autoPushEfficiencyRegulator = pushEfficiencyConfigUpdater.getAutoPushEfficiencyRegulator();
    Assert.assertNotNull(autoPushEfficiencyRegulator);

    // 第三次仍然开启，但是我们修改一部分配置
    autoPushEfficiencyConfig = new AutoPushEfficiencyConfig();
    autoPushEfficiencyConfig.setEnableAutoPushEfficiency(true);
    autoPushEfficiencyConfig.setEnableDebouncingTime(true);
    autoPushEfficiencyConfig.setEnableMaxDebouncingTime(true);
    autoPushEfficiencyConfig.setWindowTimeMillis(10);
    autoPushEfficiencyConfig.setWindowNum(3);
    autoPushEfficiencyConfig.setPushCountThreshold(10);

    pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    pushEfficiencyImproveConfig.setAutoPushEfficiencyConfig(autoPushEfficiencyConfig);

    pushEfficiencyConfigUpdater.updateFromProviderData(pushEfficiencyImproveConfig);

    AutoPushEfficiencyRegulator newAutoPushEfficiencyRegulator =
        pushEfficiencyConfigUpdater.getAutoPushEfficiencyRegulator();
    Assert.assertNotNull(newAutoPushEfficiencyRegulator);

    int windowNum = newAutoPushEfficiencyRegulator.getWindowNum();
    int windowSize = newAutoPushEfficiencyRegulator.getWindowsSize();
    long pushCountThreshold = newAutoPushEfficiencyRegulator.getPushCountThreshold();

    Assert.assertEquals(3, windowNum);
    Assert.assertEquals(3, windowSize);
    Assert.assertEquals(10, pushCountThreshold);

    // 此时之前的 AutoPushEfficiencyRegulator 应当为关闭状态
    Assert.assertTrue(autoPushEfficiencyRegulator.isClosed());

    // 清理释放线程资源
    pushEfficiencyConfigUpdater.stop();

    // 新的 AutoPushEfficiencyRegulator 也应当为关闭状态
    Assert.assertTrue(newAutoPushEfficiencyRegulator.isClosed());
  }
}
