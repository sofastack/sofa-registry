package com.alipay.sofa.configuration.impl;


import com.alipay.sofa.configuration.CompositeConfiguration;
import com.alipay.sofa.configuration.ConfigChangeListener;
import com.alipay.sofa.configuration.Configuration;
import com.alipay.sofa.configuration.model.ConfigChangeEvent;
import com.alipay.sofa.configuration.util.ConfigurationComparator;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultCompositeConfiguration extends AbstractConfiguration implements CompositeConfiguration, ConfigChangeListener {
    private List<Configuration> sources = new CopyOnWriteArrayList<Configuration>();

    public String getProperty(String key, String defaultValue) {
        String value = null;
        for (Configuration configuration : sources) {
            value = configuration.getProperty(key);
            if (value != null) {
                break;
            }
        }
        return value == null ? defaultValue : value;
    }

    public String sourceName() {
        return "DefaultCompositeConfiguration";
    }

    public Set<String> keySet() {
        Set<String> keys = Sets.newHashSet();
        for (Configuration configuration : sources) {
            keys.addAll(configuration.keySet());
        }
        return keys;
    }

    public void onChange(ConfigChangeEvent changeEvent) {
        fireConfigChange(changeEvent);
    }

    public void addConfiguration(Configuration configuration) {
        if (sources.contains(configuration)) {
            return;
        }
        sources.add(configuration);
        Collections.sort(sources, ConfigurationComparator.DEFAULT);
        configuration.addChangeListener(this);
    }
}
