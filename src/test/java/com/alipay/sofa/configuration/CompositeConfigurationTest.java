package com.alipay.sofa.configuration; /**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */

import com.alipay.sofa.configuration.impl.DefaultCompositeConfiguration;
import com.alipay.sofa.configuration.impl.MapConfiguration;
import com.alipay.sofa.configuration.impl.PropertiesConfiguration;
import com.alipay.sofa.configuration.model.ConfigChangeEvent;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author lepdou
 * @version $Id: CompositeConfigurationTest.java, v 0.1 2019年03月23日 下午3:35 lepdou Exp $
 */
public class CompositeConfigurationTest {

    /**
     * compositeConfiguration
     *      ---- mapConfiguration1 (order = 0)
     *          k1 = v1
     *          k2 = v2
     *          k3 = v3
     *          compositeConfiguration (order = 1)
     *              ---- mapConfiguration2 (order = 0)
     *                  k1 = v11
     *                  k4 = v4
     *              ---- mapConfiguration3 (order = 1)
     *                  k1 = v111
     *                  k4 = v44
     *
     *  result
     *  k1 = v1
     *  k2 = v2
     *  k3 = v3
     *  k4 = v4
     */
    @Test
    public void testBasicGetConfig() {
        CompositeConfiguration configuration = buildConfiguration(0, 1, 0, 1);

        Assert.assertEquals("v1", configuration.getProperty("k1"));
        Assert.assertEquals("v2", configuration.getProperty("k2"));
        Assert.assertEquals("v3", configuration.getProperty("k3"));
        Assert.assertEquals("v4", configuration.getProperty("k4"));
    }

    /**
     * compositeConfiguration
     *      ---- mapConfiguration1 (order = 1)
     *          k1 = v1
     *          k2 = v2
     *          k3 = v3
     *          compositeConfiguration (order = 0)
     *              ---- mapConfiguration2 (order = 0)
     *                  k1 = v11
     *                  k4 = v4
     *              ---- mapConfiguration3 (order = 1)
     *                  k1 = v111
     *                  k4 = v44
     *
     *  result
     *  k1 = v11
     *  k2 = v2
     *  k3 = v3
     *  k4 = v4
     */
    @Test
    public void testBasicGetConfig2() {
        CompositeConfiguration configuration = buildConfiguration(1, 0, 0, 1);

        Assert.assertEquals("v11", configuration.getProperty("k1"));
        Assert.assertEquals("v2", configuration.getProperty("k2"));
        Assert.assertEquals("v3", configuration.getProperty("k3"));
        Assert.assertEquals("v4", configuration.getProperty("k4"));
    }

    /**
     * compositeConfiguration
     *      ---- mapConfiguration1 (order = 1)
     *          k1 = v1
     *          k2 = v2
     *          k3 = v3
     *          compositeConfiguration (order = 0)
     *              ---- mapConfiguration2 (order = 1)
     *                  k1 = v11
     *                  k4 = v4
     *              ---- mapConfiguration3 (order = 0)
     *                  k1 = v111
     *                  k4 = v44
     *
     *  result
     *  k1 = v111
     *  k2 = v2
     *  k3 = v3
     *  k4 = v44
     */
    @Test
    public void testBasicGetConfig3() {
        CompositeConfiguration configuration = buildConfiguration(1, 0, 1, 0);
        Assert.assertEquals("v111", configuration.getProperty("k1"));
        Assert.assertEquals("v2", configuration.getProperty("k2"));
        Assert.assertEquals("v3", configuration.getProperty("k3"));
        Assert.assertEquals("v44", configuration.getProperty("k4"));
    }

    /**
     * compositeConfiguration
     *      ---- mapConfiguration1 (order = 1)
     *          k1 = v1
     *          k2 = v2
     *          k3 = v3
     *          compositeConfiguration (order = 0)
     *              ---- mapConfiguration2 (order = 1)
     *                  k1 = v11
     *                  k4 = v4
     *              ---- mapConfiguration3 (order = 0)
     *                  k1 = v111
     *                  k4 = v44
     *      ---- propertiesConfiguration (order = 2)
     *          k1 = v11111
     *          k5 = v5
     *
     *  result
     *  k1 = v111
     *  k2 = v2
     *  k3 = v3
     *  k4 = v44
     *  k5 = v5
     */
    @Test
    public void testCompositeWithMapAndProperties() {
        CompositeConfiguration configuration = buildConfiguration(1, 0, 1, 0);

        Properties properties = new Properties();
        properties.put("k1", "v11111");
        properties.put("k5", "v5");
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration("properties", properties);
        propertiesConfiguration.setOrder(2);

        configuration.addConfiguration(propertiesConfiguration);

        Assert.assertEquals("v111", configuration.getProperty("k1"));
        Assert.assertEquals("v2", configuration.getProperty("k2"));
        Assert.assertEquals("v3", configuration.getProperty("k3"));
        Assert.assertEquals("v44", configuration.getProperty("k4"));
        Assert.assertEquals("v5", configuration.getProperty("k5"));
        Assert.assertEquals(5, configuration.keySet().size());
    }

    @Test
    public void testChangeListener() throws InterruptedException {
        Map<String, String> map1 = Maps.newHashMap();
        map1.put("k1", "v1");
        map1.put("k2", "v2");
        map1.put("k3", "v3");
        MapConfiguration mapConfiguration1 = new MapConfiguration("map1", map1);
        mapConfiguration1.setOrder(0);

        Map<String, String> map2 = Maps.newHashMap();
        map2.put("k1", "v11");
        map2.put("k4", "v4");
        MapConfiguration mapConfiguration2 = new MapConfiguration("map2", map2);
        mapConfiguration2.setOrder(1);

        DefaultCompositeConfiguration sonCompositeConfiguration = new DefaultCompositeConfiguration();
        sonCompositeConfiguration.addConfiguration(mapConfiguration2);
        sonCompositeConfiguration.setOrder(0);

        DefaultCompositeConfiguration configuration = new DefaultCompositeConfiguration();
        configuration.addConfiguration(mapConfiguration1);
        configuration.addConfiguration(sonCompositeConfiguration);

        final AtomicInteger modifiedCounter = new AtomicInteger(0);
        configuration.addChangeListener(new ConfigChangeListener() {
            public void onChange(ConfigChangeEvent changeEvent) {
                modifiedCounter.incrementAndGet();
            }
        });

        int modifiedTime = 100;
        for (int i = 0; i < modifiedTime; i++) {
            mapConfiguration1.put("k1", UUID.randomUUID().toString());
            mapConfiguration2.put("k1", UUID.randomUUID().toString());
        }


        TimeUnit.SECONDS.sleep(2);

        Assert.assertEquals(modifiedTime * 2, modifiedCounter.get());
    }

    /**
     * compositeConfiguration
     *      ---- mapConfiguration1 (order = ${order1})
     *          k1 = v1
     *          k2 = v2
     *          k3 = v3
     *          compositeConfiguration (order = ${order2})
     *              ---- mapConfiguration2 (order = ${order3})
     *                  k1 = v11
     *                  k4 = v4
     *              ---- mapConfiguration3 (order = ${order4})
     *                  k1 = v111
     *                  k4 = v44
     */
    private DefaultCompositeConfiguration buildConfiguration(int order1, int order2, int order3, int order4) {
        Map<String, String> map1 = Maps.newHashMap();
        map1.put("k1", "v1");
        map1.put("k2", "v2");
        map1.put("k3", "v3");
        MapConfiguration mapConfiguration1 = new MapConfiguration("map1", map1);
        mapConfiguration1.setOrder(order1);

        Map<String, String> map2 = Maps.newHashMap();
        map2.put("k1", "v11");
        map2.put("k4", "v4");
        MapConfiguration mapConfiguration2 = new MapConfiguration("map2", map2);
        mapConfiguration2.setOrder(order3);

        Map<String, String> map3 = Maps.newHashMap();
        map3.put("k1", "v111");
        map3.put("k4", "v44");
        MapConfiguration mapConfiguration3 = new MapConfiguration("map3", map3);
        mapConfiguration3.setOrder(order4);

        DefaultCompositeConfiguration sonCompositeConfiguration = new DefaultCompositeConfiguration();
        sonCompositeConfiguration.addConfiguration(mapConfiguration2);
        sonCompositeConfiguration.addConfiguration(mapConfiguration3);
        sonCompositeConfiguration.setOrder(order2);

        DefaultCompositeConfiguration configuration = new DefaultCompositeConfiguration();
        configuration.addConfiguration(mapConfiguration1);
        configuration.addConfiguration(sonCompositeConfiguration);

        return configuration;
    }
}