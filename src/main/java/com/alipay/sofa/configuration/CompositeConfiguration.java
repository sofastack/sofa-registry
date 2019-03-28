package com.alipay.sofa.configuration;

public interface CompositeConfiguration extends Configuration {
    void addConfiguration(Configuration configuration);
}
