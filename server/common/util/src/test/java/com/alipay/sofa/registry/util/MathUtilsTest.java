package com.alipay.sofa.registry.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void testDivideCeil() {
        Assert.assertEquals(3, MathUtils.divideCeil(5, 2));
        Assert.assertEquals(3, MathUtils.divideCeil(6, 2));
        Assert.assertEquals(4, MathUtils.divideCeil(7, 2));
    }
}