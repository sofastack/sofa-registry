/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.configuration.impl;

import com.alipay.sofa.configuration.model.ConfigChange;
import com.alipay.sofa.configuration.model.ConfigChangeEvent;
import com.alipay.sofa.configuration.model.PropertyChangeType;
import com.google.common.base.Objects;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 *
 * @author lepdou
 * @version $Id: MapConfiguration.java, v 0.1 2019年03月23日 下午2:38 lepdou Exp $
 */
public class MapConfiguration extends AbstractConfiguration {

    private String              sourceName;
    private Map<String, String> configs;

    public MapConfiguration(String sourceName, Map<String, String> configs) {
        this.sourceName = sourceName;

        if (configs == null) {
            this.configs = Maps.newHashMap();
        } else {
            this.configs = configs;
        }
    }

    public String sourceName() {
        return sourceName;
    }

    public String getProperty(String key, String defaultValue) {
        if (configs == null) {
            return defaultValue;
        }

        String value = configs.get(key);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public void put(String key, String value) {
        ConfigChange configChange;
        if (configs.containsKey(key)) {
            String oldValue = configs.get(key);

            if (Objects.equal(oldValue, value)) {
                return;
            }

            // Update
            configs.put(key, value);

            configChange = new ConfigChange(this.sourceName, oldValue, value, PropertyChangeType.MODIFIED);
        } else {
            // Add
            configs.put(key, value);

            configChange = new ConfigChange(this.sourceName, null, value, PropertyChangeType.ADDED);
        }

        Map<String, ConfigChange> configChanges = Maps.newHashMap();
        configChanges.put(key, configChange);

        ConfigChangeEvent event = new ConfigChangeEvent(this.sourceName, configChanges);

        fireConfigChange(event);
    }

    /**
     * Replace all configurations in full.
     * If configs is null, will clear stored configs.
     *
     * @param newConfigs new all configs
     */
    public void replaceAll(Map<String, String> newConfigs) {
        if (newConfigs == null) {
            newConfigs = Maps.newHashMap();
        }

        MapDifference<String, String> difference = Maps.difference(this.configs, newConfigs);

        Map<String, String> deletedConfigs = difference.entriesOnlyOnLeft();
        Map<String, String> addedConfigs = difference.entriesOnlyOnRight();
        Map<String, ValueDifference<String>> modifiedConfigs = difference.entriesDiffering();

        this.configs = newConfigs;

        Map<String, ConfigChange> configChanges = Maps.newHashMap();

        if (deletedConfigs != null && deletedConfigs.size() > 0) {
            for (Map.Entry<String, String> entry : deletedConfigs.entrySet()) {
                String key = entry.getKey();
                ConfigChange configChange = new ConfigChange(sourceName, entry.getValue(), null, PropertyChangeType.DELETED);
                configChanges.put(key, configChange);
            }
        }

        if (addedConfigs != null && addedConfigs.size() > 0) {
            for (Map.Entry<String, String> entry : addedConfigs.entrySet()) {
                String key = entry.getKey();
                ConfigChange configChange = new ConfigChange(sourceName, null, entry.getValue(), PropertyChangeType.ADDED);
                configChanges.put(key, configChange);
            }
        }

        if (modifiedConfigs != null && modifiedConfigs.size() > 0) {
            for (Map.Entry<String, ValueDifference<String>> entry : modifiedConfigs.entrySet()) {
                String key = entry.getKey();
                ValueDifference<String> valueDifference = entry.getValue();
                ConfigChange configChange = new ConfigChange(sourceName, valueDifference.leftValue(), valueDifference.rightValue(),
                        PropertyChangeType.MODIFIED);
                configChanges.put(key, configChange);
            }
        }

        ConfigChangeEvent event = new ConfigChangeEvent(this.sourceName, configChanges);

        fireConfigChange(event);
    }
}