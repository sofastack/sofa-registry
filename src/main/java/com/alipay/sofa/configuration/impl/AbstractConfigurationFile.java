/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.configuration.impl;

import com.alipay.sofa.configuration.ConfigFileChangeListener;
import com.alipay.sofa.configuration.ConfigurationFile;
import com.alipay.sofa.configuration.model.ConfigFileChangeEvent;
import com.alipay.sofa.configuration.util.ConfigurationCallbackThreadFactory;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author lepdou
 * @version $Id: AbstractConfigurationFile.java, v 0.1 2019年03月27日 上午11:29 lepdou Exp $
 */
public abstract class AbstractConfigurationFile implements ConfigurationFile {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected String content;

    private final List<ConfigFileChangeListener> listeners = Lists.newCopyOnWriteArrayList();

    private static final ExecutorService executorService;

    static {
        executorService = Executors.newCachedThreadPool(ConfigurationCallbackThreadFactory.create(true));
    }

    public String getContent() {
        return content;
    }

    public boolean hasContent() {
        return content != null;
    }

    public void addChangeListener(ConfigFileChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(ConfigFileChangeListener listener) {
        listeners.remove(listener);
    }

    protected void fireConfigChange(final ConfigFileChangeEvent changeEvent) {
        for (final ConfigFileChangeListener listener : listeners) {
            executorService.submit(new Runnable() {

                public void run() {
                    String listenerName = listener.getClass().getName();
                    try {
                        listener.onChange(changeEvent);
                    } catch (Throwable ex) {
                        logger.error("Failed to invoke config file change listener {}", listenerName, ex);
                    }
                }
            });
        }
    }
}