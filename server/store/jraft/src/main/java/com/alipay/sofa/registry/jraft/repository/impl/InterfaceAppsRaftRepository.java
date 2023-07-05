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
package com.alipay.sofa.registry.jraft.repository.impl;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsRaftRepository.java, v 0.1 2021年01月24日 19:44 xiaojian.xj Exp $
 */
public class InterfaceAppsRaftRepository implements InterfaceAppsRepository {
  protected static final Logger LOG = LoggerFactory.getLogger(InterfaceAppsRaftRepository.class);

  /** map: interface, appNames */
  protected final Map<String, InterfaceMapping> interfaceApps = new ConcurrentHashMap<>();

  @Override
  public InterfaceMapping getAppNames(String dataInfoId) {
    final InterfaceMapping ret = interfaceApps.get(dataInfoId);
    return ret;
  }

  @Override
  public void register(String appName, Set<String> interfaceNames) {
    for (String interfaceName : interfaceNames) {
      InterfaceMapping interfaceMapping =
          interfaceApps.computeIfAbsent(interfaceName, k -> new InterfaceMapping(-1));
      interfaceMapping.getApps().add(appName);
    }
  }

  @Override
  public void renew(String interfaceName, String appName) {}

  @Override
  public void startSynced() {}

  @Override
  public void waitSynced() {}

  @Override
  public long getDataVersion() {
    return 0;
  }

  @Override
  public Map<String, Map<String, InterfaceMapping>> allServiceMapping() {
    return null;
  }

  @Override
  public Set<String> dataCenters() {
    return null;
  }

  @Override
  public void setDataCenters(Set<String> dataCenters) {}
}
