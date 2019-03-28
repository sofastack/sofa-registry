/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.configuration;

import com.alipay.sofa.configuration.impl.PropertiesConfiguration;
import com.alipay.sofa.configuration.model.ConfigChange;
import com.alipay.sofa.configuration.model.ConfigChangeEvent;
import com.alipay.sofa.configuration.model.PropertyChangeType;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author lepdou
 * @version $Id: PropertiesConfigurationTest.java, v 0.1 2019年03月28日 下午4:43 lepdou Exp $
 */
public class PropertiesConfigurationTest {

    @Test
    public void testBasicGetConfig() {
        Properties properties = new Properties();
        properties.put("k1", "v1");
        properties.put("k2", "v2");

        String sourceName = "propertiesConfig";
        Configuration configuration = new PropertiesConfiguration(sourceName, properties);

        Assert.assertEquals("v1", configuration.getProperty("k1"));
        Assert.assertEquals("v2", configuration.getProperty("k2"));
        Assert.assertEquals("v3", configuration.getProperty("k3", "v3"));
        Assert.assertNull("v4", configuration.getProperty("k4"));

        Assert.assertEquals(sourceName, configuration.sourceName());
    }

    @Test
    public void testChangeListener() throws InterruptedException {
        Properties properties = new Properties();
        properties.put("k1", "v1");
        properties.put("k2", "v2");

        String sourceName = "propertiesConfig";
        PropertiesConfiguration configuration = new PropertiesConfiguration(sourceName, properties);

        //all keys
        final AtomicInteger changedCounter = new AtomicInteger(0);
        final AtomicInteger addCounter = new AtomicInteger(0);
        configuration.addChangeListener(new ConfigChangeListener() {

            public void onChange(ConfigChangeEvent changeEvent) {
                for (String changedKey : changeEvent.changedKeys()) {
                    switch (changeEvent.getChange(changedKey).getChangeType()) {
                        case ADDED: {
                            addCounter.incrementAndGet();
                            break;
                        }
                        case MODIFIED: {
                            changedCounter.incrementAndGet();
                            break;
                        }
                    }
                }
            }
        });

        //interested keys
        final AtomicInteger interestedKeyChangedCounter = new AtomicInteger(0);
        configuration.addChangeListener(new ConfigChangeListener() {
            public void onChange(ConfigChangeEvent changeEvent) {
                interestedKeyChangedCounter.incrementAndGet();
            }
        }, Sets.newHashSet("k1"));

        //interested prefix keys
        final AtomicInteger interestedPrefixKeyChangedCounter = new AtomicInteger(0);
        configuration.addChangeListener(new ConfigChangeListener() {
            public void onChange(ConfigChangeEvent changeEvent) {
                interestedPrefixKeyChangedCounter.incrementAndGet();
            }
        }, Sets.newHashSet("k1"), Sets.newHashSet("k"));

        int modifiedTimes = 100;
        for (int i = 0; i < modifiedTimes; i++) {
            configuration.put("k1", UUID.randomUUID().toString());
            configuration.put("k2", UUID.randomUUID().toString());
        }

        int addCounterTimes = 100;
        for (int i = 0; i < addCounterTimes; i++) {
            configuration.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }

        TimeUnit.SECONDS.sleep(2);

        Assert.assertEquals(modifiedTimes * 2, changedCounter.get()); //k1 & k2
        Assert.assertEquals(addCounterTimes, addCounter.get());
        Assert.assertEquals(modifiedTimes, interestedKeyChangedCounter.get()); //k1
        Assert.assertEquals(modifiedTimes * 2, interestedPrefixKeyChangedCounter.get()); // k1 & k2
    }

    @Test
    public void testReplaceAll() throws InterruptedException {
        Properties oldConfigs = new Properties();
        oldConfigs.put("k1", "v1");
        oldConfigs.put("k2", "v2");
        oldConfigs.put("k3", "v3");

        Properties newConfigs = new Properties();
        newConfigs.put("k1", "v11");
        newConfigs.put("k3", "v3");
        newConfigs.put("k4", "v4");

        PropertiesConfiguration mapConfiguration = new PropertiesConfiguration("mapConfigs", oldConfigs);

        Assert.assertEquals("v1", mapConfiguration.getProperty("k1"));
        Assert.assertEquals("v2", mapConfiguration.getProperty("k2"));
        Assert.assertEquals("v3", mapConfiguration.getProperty("k3"));

        final AtomicBoolean pass = new AtomicBoolean(true);
        mapConfiguration.addChangeListener(new ConfigChangeListener() {
            public void onChange(ConfigChangeEvent changeEvent) {
                Set<String> changeKeys = changeEvent.changedKeys();

                try {
                    Assert.assertEquals(3, changeKeys.size());

                    for (String key : changeKeys) {
                        ConfigChange configChange = changeEvent.getChange(key);
                        System.out.println(configChange);
                        if ("k1".equals(key)) {
                            Assert.assertEquals(PropertyChangeType.MODIFIED, configChange.getChangeType());
                            Assert.assertEquals("v1", configChange.getOldValue());
                            Assert.assertEquals("v11", configChange.getNewValue());
                        }

                        if ("k2".equals(key)) {
                            Assert.assertEquals(PropertyChangeType.DELETED, configChange.getChangeType());
                            Assert.assertEquals("v2", configChange.getOldValue());
                            Assert.assertNull(configChange.getNewValue());
                        }

                        if ("k4".equals(key)) {
                            Assert.assertEquals(PropertyChangeType.ADDED, configChange.getChangeType());
                            Assert.assertEquals("v4", configChange.getNewValue());
                            Assert.assertNull(configChange.getOldValue());
                        }
                    }

                } catch (Throwable e) {
                    pass.set(false);
                }
            }
        });

        mapConfiguration.replaceProperties(newConfigs);

        TimeUnit.MILLISECONDS.sleep(500);

        Assert.assertEquals("v11", mapConfiguration.getProperty("k1"));
        Assert.assertNull(mapConfiguration.getProperty("k2"));
        Assert.assertEquals("v3", mapConfiguration.getProperty("k3"));
        Assert.assertEquals("v4", mapConfiguration.getProperty("k4"));

        Assert.assertTrue(pass.get());
    }
}