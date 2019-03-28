package com.alipay.sofa.configuration.impl;

import com.alipay.sofa.configuration.ConfigChangeListener;
import com.alipay.sofa.configuration.Configuration;
import com.alipay.sofa.configuration.model.ConfigChangeEvent;
import com.alipay.sofa.configuration.util.ConfigurationCallbackThreadFactory;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractConfiguration implements Configuration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ExecutorService executorService;

    private final Set<ConfigChangeListener>              listeners = Sets.newCopyOnWriteArraySet();
    private final Map<ConfigChangeListener, Set<String>> interestedKeysMap
                                                                   = new ConcurrentHashMap<ConfigChangeListener,
                                                                               Set<String>>();
    private final Map<ConfigChangeListener, Set<String>> interestedKeyPrefixesMap
                                                                   = new ConcurrentHashMap<ConfigChangeListener,
            Set<String>>();

    private int order = 0;

    static {
        executorService = Executors.newCachedThreadPool(ConfigurationCallbackThreadFactory.create(true));
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public int order() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void addChangeListener(ConfigChangeListener listener) {
        addChangeListener(listener, null);
    }

    public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys) {
        addChangeListener(listener, interestedKeys, null);
    }

    public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys, Set<String> interestedKeyPrefixes) {
        boolean added = listeners.add(listener);
        if (!added) {
            return;
        }
        if (interestedKeys != null && !interestedKeys.isEmpty()) {
            interestedKeysMap.put(listener, Sets.newHashSet(interestedKeys));
        }
        if (interestedKeyPrefixes != null && !interestedKeyPrefixes.isEmpty()) {
            interestedKeyPrefixesMap.put(listener, Sets.newHashSet(interestedKeyPrefixes));
        }
    }

    public boolean removeChangeListener(ConfigChangeListener listener) {
        interestedKeysMap.remove(listener);
        interestedKeyPrefixesMap.remove(listener);
        return listeners.remove(listener);
    }

    protected void fireConfigChange(final ConfigChangeEvent changeEvent) {
        for (final ConfigChangeListener listener : listeners) {
            // check whether the listener is interested in this change event
            if (!isConfigChangeListenerInterested(listener, changeEvent)) {
                continue;
            }
            executorService.submit(new Runnable() {
                public void run() {
                    String listenerName = listener.getClass().getName();
                    try {
                        listener.onChange(changeEvent);
                    } catch (Throwable ex) {
                        logger.error("Failed to invoke config change listener {}", listenerName, ex);
                    }
                }
            });
        }
    }

    private boolean isConfigChangeListenerInterested(ConfigChangeListener configChangeListener, ConfigChangeEvent configChangeEvent) {
        Set<String> interestedKeys = interestedKeysMap.get(configChangeListener);
        Set<String> interestedKeyPrefixes = interestedKeyPrefixesMap.get(configChangeListener);

        if ((interestedKeys == null || interestedKeys.isEmpty())
                && (interestedKeyPrefixes == null || interestedKeyPrefixes.isEmpty())) {
            return true; // no interested keys means interested in all keys
        }

        if (interestedKeys != null) {
            for (String interestedKey : interestedKeys) {
                if (configChangeEvent.isChanged(interestedKey)) {
                    return true;
                }
            }
        }

        if (interestedKeyPrefixes != null) {
            for (String prefix : interestedKeyPrefixes) {
                for (final String changedKey : configChangeEvent.changedKeys()) {
                    if (changedKey.startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
