package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SessionInterestsTest extends AbstractSessionServerTestBase {

    private SessionInterests interests = new SessionInterests();

    @Before
    public void beforeSessionInterestsTest() {
        interests.setSessionServerConfig(sessionServerConfig);
    }

    @Test
    public void testAdd() {
        Subscriber subscriber = randomSubscriber();
        Assert.assertNotNull(subscriber.getDataInfoId());
        Assert.assertTrue(interests.add(subscriber));
        Assert.assertTrue(interests.add(subscriber));
    }

    @Test
    public void testCheckInterestVersion() {
        Assert.assertSame(Interests.InterestVersionCheck.NoSub,
                interests.checkInterestVersion(getDc(), randomSubscriber().getDataInfoId(), 1L));
        String dataInfo = randomString(10);
        String instanceId = randomString(10);
        interests.add(randomSubscriber(dataInfo, instanceId));
        Assert.assertEquals(Interests.InterestVersionCheck.Interested, interests.checkInterestVersion(getDc(),
                DataInfo.toDataInfoId(dataInfo, instanceId, "default-group"),
                System.currentTimeMillis() + 100));

    }

    @Test
    public void testGetInterests() {
    }

    @Test
    public void testGetInterestVersions() {
    }

    @Test
    public void testGetInterestsNeverPushed() {
    }
}