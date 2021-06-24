package com.alipay.sofa.registry.server.shared.metrics;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import org.junit.Assert;
import org.junit.Test;

public class InterestGroupTest {
    @Test
    public void test(){
        Assert.assertEquals(ValueConstants.DEFAULT_GROUP, InterestGroup.normalizeGroup(ValueConstants.DEFAULT_GROUP));
        InterestGroup.registerInterestGroup("test");
        Assert.assertEquals("other", InterestGroup.normalizeGroup("abc"));
    }
}
