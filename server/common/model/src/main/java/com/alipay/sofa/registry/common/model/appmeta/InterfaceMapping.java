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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class InterfaceMapping {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE", "[InterfaceApps]");

  private final long nanosVersion;
  private final Set<String> apps;

  public InterfaceMapping(long nanosVersion) {
    this.nanosVersion = nanosVersion;
    this.apps = Collections.EMPTY_SET;
  }

  public InterfaceMapping(long nanosVersion, String appName) {
    this.nanosVersion = nanosVersion;
    this.apps = Collections.unmodifiableSet(Collections.singleton(appName));
  }

  public InterfaceMapping(long nanosVersion, Set<String> appNames) {
    this.nanosVersion = nanosVersion;
    this.apps = Collections.unmodifiableSet(appNames);
  }

  public long getNanosVersion() {
    return nanosVersion;
  }

  public InterfaceMapping addApp(long version, String app) {
    if (this.apps.contains(app) && this.nanosVersion >= version) {
      return this;
    }
    if (version <= nanosVersion) {
      LOG.error("interface app mapping stale version: {}", version);
    }
    Set<String> apps = new HashSet<>(this.apps);
    apps.add(app);
    return new InterfaceMapping(Math.max(this.nanosVersion, version), apps);
  }

  public InterfaceMapping removeApp(long version, String app) {
    if (!this.apps.contains(app) && this.nanosVersion >= version) {
      return this;
    }
    if (version <= nanosVersion) {
      LOG.error("interface app mapping stale version: {}", version);
    }
    Set<String> apps = new HashSet<>(this.apps);
    apps.remove(app);
    return new InterfaceMapping(Math.max(this.nanosVersion, version), apps);
  }

  public Set<String> getApps() {
    return apps;
  }

  @Override
  public String toString() {
    return "InterfaceMapping{" + "nanosVersion=" + nanosVersion + ", appSets=" + apps + '}';
  }
}
