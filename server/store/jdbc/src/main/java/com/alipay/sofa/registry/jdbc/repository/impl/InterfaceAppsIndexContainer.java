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
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.informer.DbEntryContainer;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.util.CollectionUtils;

public class InterfaceAppsIndexContainer implements DbEntryContainer<InterfaceAppsIndexDomain> {

  // <dataInfoId, <dataCenter, InterfaceMapping>>
  private final Map<String, Map<String, InterfaceMapping>> multiData = Maps.newConcurrentMap();

  private void addEntry(InterfaceAppsIndexDomain entry) {

    String interfaceName = WordCache.getWordCache(entry.getInterfaceName());
    String dataCenter = WordCache.getWordCache(entry.getDataCenter());
    String appName = WordCache.getWordCache(entry.getAppName());
    long nanoTime = TimestampUtil.getNanosLong(entry.getGmtCreate());

    Map<String, InterfaceMapping> data =
        multiData.computeIfAbsent(interfaceName, k -> Maps.newConcurrentMap());

    InterfaceMapping mapping =
        data.computeIfAbsent(dataCenter, k -> new InterfaceMapping(nanoTime, appName));
    data.put(dataCenter, mapping.addApp(nanoTime, appName));
  }

  private void removeEntry(InterfaceAppsIndexDomain entry) {

    String interfaceName = WordCache.getWordCache(entry.getInterfaceName());
    String dataCenter = WordCache.getWordCache(entry.getDataCenter());
    String appName = WordCache.getWordCache(entry.getAppName());
    long nanoTime = TimestampUtil.getNanosLong(entry.getGmtCreate());

    Map<String, InterfaceMapping> data =
        multiData.computeIfAbsent(interfaceName, k -> Maps.newConcurrentMap());

    InterfaceMapping mapping =
        data.computeIfAbsent(dataCenter, k -> new InterfaceMapping(nanoTime));
    data.put(dataCenter, mapping.removeApp(nanoTime, appName));
  }

  public boolean containsName(String dataCenter, String interfaceName, String appName) {
    Map<String, InterfaceMapping> data = multiData.get(interfaceName);
    if (CollectionUtils.isEmpty(data)) {
      return false;
    }

    InterfaceMapping mapping = data.get(dataCenter);
    if (mapping == null) {
      return false;
    }
    return mapping.getApps().contains(appName);
  }

  public Map<String, InterfaceMapping> getAppMapping(String interfaceName) {
    if (CollectionUtils.isEmpty(multiData.get(interfaceName))) {
      return Collections.EMPTY_MAP;
    }
    return Maps.newHashMap(multiData.get(interfaceName));
  }

  public Set<String> interfaces() {
    return multiData.keySet();
  }

  public Map<String, Map<String, InterfaceMapping>> allServiceMapping() {
    return Maps.newHashMap(multiData);
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
