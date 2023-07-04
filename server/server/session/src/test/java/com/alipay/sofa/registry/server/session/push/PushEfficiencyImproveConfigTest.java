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

import static org.junit.jupiter.api.Assertions.*;

import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author jiangcun.hlc@antfin.com
 * @since 2023/3/23
 */
class PushEfficiencyImproveConfigTest {

  @Test
  void inIpZoneSBF() {
    PushEfficiencyImproveConfig pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    Assert.assertFalse(pushEfficiencyImproveConfig.inIpZoneSBF());
    Set<String> zones = new HashSet<>();
    zones.add("ALL_ZONE");
    pushEfficiencyImproveConfig.setZoneSet(zones);
    Assert.assertTrue(pushEfficiencyImproveConfig.inIpZoneSBF());

    zones = new HashSet<>();
    zones.add("DEF_ZONE");
    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc");
    pushEfficiencyImproveConfig.setZoneSet(zones);
    pushEfficiencyImproveConfig.setSessionServerConfig(configBean);
    Assert.assertTrue(pushEfficiencyImproveConfig.inIpZoneSBF());

    Assert.assertFalse(pushEfficiencyImproveConfig.inAppSBF("testSub"));

    Set<String> appSet = new HashSet<>();
    appSet.add("ALL_APP");
    pushEfficiencyImproveConfig.setSubAppSet(appSet);
    Assert.assertTrue(pushEfficiencyImproveConfig.inAppSBF("testSub2"));

    appSet = new HashSet<>();
    appSet.add("testSub");
    pushEfficiencyImproveConfig.setSubAppSet(appSet);

    Assert.assertTrue(pushEfficiencyImproveConfig.inAppSBF("testSub"));
    Assert.assertTrue(
        pushEfficiencyImproveConfig.fetchSbfAppPushTaskDebouncingMillis("testSub") > 0);
    Assert.assertTrue(
        pushEfficiencyImproveConfig.fetchSbfAppPushTaskDebouncingMillis("testSub2") > 0);
    Assert.assertFalse(pushEfficiencyImproveConfig.fetchPushTaskWake("testSub"));
    Assert.assertTrue(pushEfficiencyImproveConfig.fetchRegWorkWake("testSub"));
    Assert.assertFalse(pushEfficiencyImproveConfig.fetchPushTaskWake("testSub2"));
    Assert.assertTrue(pushEfficiencyImproveConfig.fetchRegWorkWake("testSub2"));
    pushEfficiencyImproveConfig.setPushTaskWake(true);
    pushEfficiencyImproveConfig.setRegWorkWake(false);
    Assert.assertTrue(pushEfficiencyImproveConfig.fetchPushTaskWake("testSub"));
    Assert.assertFalse(pushEfficiencyImproveConfig.fetchRegWorkWake("testSub"));
  }
}
