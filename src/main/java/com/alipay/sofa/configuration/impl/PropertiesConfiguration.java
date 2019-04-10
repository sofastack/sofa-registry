/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.configuration.impl;

import com.alipay.sofa.configuration.ConfigChangeListener;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author lepdou
 * @version $Id: PropertyConfiguration.java, v 0.1 2019年03月28日 下午4:21 lepdou Exp $
 */
public class PropertiesConfiguration extends AbstractConfiguration {

    private String           sourceName;
    private MapConfiguration mapConfiguration;

    public PropertiesConfiguration(String sourceName, Properties properties) {
        this.sourceName = sourceName;
        mapConfiguration = new MapConfiguration(sourceName, properties2Map(properties));
    }

    public String sourceName() {
        return sourceName;
    }

    public Set<String> keySet() {
        return mapConfiguration.keySet();
    }

    public String getProperty(String key, String defaultValue) {
        return mapConfiguration.getProperty(key, defaultValue);
    }

    public void put(String key, String value) {
        mapConfiguration.put(key, value);
    }

    public void replaceProperties(Properties newProperties) {
        mapConfiguration.replaceAll(properties2Map(newProperties));
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        mapConfiguration.addChangeListener(listener);
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys, Set<String> interestedKeyPrefixes) {
        mapConfiguration.addChangeListener(listener, interestedKeys, interestedKeyPrefixes);
    }

    @Override
    public boolean removeChangeListener(ConfigChangeListener listener) {
        return mapConfiguration.removeChangeListener(listener);
    }

    private Map<String, String> properties2Map(Properties properties) {
        Map<String, String> result = Maps.newHashMap();
        if (properties == null) {
            return result;
        }

        Set<Entry<Object, Object>> entries = properties.entrySet();
        for (Entry<Object, Object> entry : entries) {
            result.put(object2String(entry.getKey()), object2String(entry.getValue()));
        }

        return result;
    }

    private String object2String(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }

}