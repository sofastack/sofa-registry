package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
        Assert.assertTrue(pushEfficiencyImproveConfig.fetchSbfAppPushTaskDebouncingMillis("testSub") > 0);
        Assert.assertTrue(pushEfficiencyImproveConfig.fetchSbfAppPushTaskDebouncingMillis("testSub2") > 0);
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