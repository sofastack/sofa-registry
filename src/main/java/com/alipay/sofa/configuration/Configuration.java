package com.alipay.sofa.configuration;

import java.util.Set;

public interface Configuration {
    String sourceName();

    int order();

    void setOrder(int order);

    Set<String> keySet();

    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    void addChangeListener(ConfigChangeListener listener);

    void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys);

    void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys, Set<String> interestedKeyPrefixes);

    boolean removeChangeListener(ConfigChangeListener listener);
}
