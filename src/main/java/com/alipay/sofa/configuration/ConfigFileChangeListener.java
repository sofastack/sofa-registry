package com.alipay.sofa.configuration;

import com.alipay.sofa.configuration.model.ConfigFileChangeEvent;

public interface ConfigFileChangeListener {
    /**
     * Invoked when there is any config change for the namespace.
     * @param changeEvent the event for this change
     */
    void onChange(ConfigFileChangeEvent changeEvent);
}
