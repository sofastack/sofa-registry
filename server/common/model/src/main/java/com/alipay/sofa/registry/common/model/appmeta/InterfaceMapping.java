/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.common.model.appmeta;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public final class InterfaceMapping {
    private final long        nanosVersion;
    private final Set<String> apps;

    public InterfaceMapping(long nanosVersion) {
        this.nanosVersion = nanosVersion;
        this.apps = Collections.EMPTY_SET;
    }

    public InterfaceMapping(long nanosVersion, String app) {
        this.nanosVersion = nanosVersion;
        this.apps = Sets.newHashSet(app);
    }

    public InterfaceMapping(long nanosVersion, Set<String> apps) {
        this.nanosVersion = nanosVersion;
        this.apps = Sets.newHashSet(apps);
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
        return "InterfaceMapping{" + "nanosVersion=" + nanosVersion + ", appSets=" + apps + '}';
    }
}
