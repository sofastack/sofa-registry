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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.informer.DbEntryContainer;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;

public class InterfaceAppsIndexContainer implements DbEntryContainer<InterfaceAppsIndexDomain> {

  private final Map<String, InterfaceMapping> data = Maps.newConcurrentMap();

  private void addEntry(InterfaceAppsIndexDomain entry) {
    InterfaceMapping mapping =
        data.computeIfAbsent(
            entry.getInterfaceName(),
            k ->
                new InterfaceMapping(
                    TimestampUtil.getNanosLong(entry.getGmtCreate()), entry.getAppName()));
    data.put(
        entry.getInterfaceName(),
        mapping.addApp(TimestampUtil.getNanosLong(entry.getGmtCreate()), entry.getAppName()));
  }

  private void removeEntry(InterfaceAppsIndexDomain entry) {
    InterfaceMapping mapping =
        data.computeIfAbsent(
            entry.getInterfaceName(),
            k -> new InterfaceMapping(TimestampUtil.getNanosLong(entry.getGmtCreate())));
    data.put(
        entry.getInterfaceName(),
        mapping.removeApp(TimestampUtil.getNanosLong(entry.getGmtCreate()), entry.getAppName()));
  }

  public boolean containsName(String interfaceName, String appName) {
    InterfaceMapping mapping = data.get(interfaceName);
    if (mapping == null) {
      return false;
    }
    return mapping.getApps().contains(appName);
  }

  public InterfaceMapping getAppMapping(String interfaceName) {
    return data.get(interfaceName);
  }

  public Set<String> interfaces() {
    return data.keySet();
  }

  @Override
  public synchronized void onEntry(InterfaceAppsIndexDomain entry) {
    if (entry.isReference()) {
      addEntry(entry);
    } else {
      removeEntry(entry);
    }
  }
}
