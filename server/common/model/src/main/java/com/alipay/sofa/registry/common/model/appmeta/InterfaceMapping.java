package com.alipay.sofa.registry.common.model.appmeta;


import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public final class InterfaceMapping {
    private final long nanosVersion;
    private final Set<String> apps;

    public InterfaceMapping(long nanosVersion) {
        this.nanosVersion = nanosVersion;
        this.apps = Collections.EMPTY_SET;
    }

    public InterfaceMapping(long nanosVersion, String app) {
        this.nanosVersion = nanosVersion;
        this.apps = Sets.newHashSet(app);
    }

    public InterfaceMapping(long nanosVersion, Set<String> copyApp, String newApp) {
        this.nanosVersion = nanosVersion;
        this.apps = Sets.newHashSet(copyApp);
        this.apps.add(newApp);
    }

    public long getNanosVersion() {
        return nanosVersion;
    }

    public Set<String> getApps() {
        return Collections.unmodifiableSet(apps);
    }

    @Override
    public String toString() {
        return "InterfaceMapping{" +
                "nanosVersion=" + nanosVersion +
                ", appSets=" + apps +
                '}';
    }
}
