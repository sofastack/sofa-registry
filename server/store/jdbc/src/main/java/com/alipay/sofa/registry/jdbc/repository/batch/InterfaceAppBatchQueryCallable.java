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
package com.alipay.sofa.registry.jdbc.repository.batch;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppIndexQueryModel;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import com.alipay.sofa.registry.jdbc.mapper.InterfaceAppsIndexMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.BatchCallableRunnable;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppBatchQueryCallable.java, v 0.1 2021年01月26日 14:45 xiaojian.xj Exp $
 */
public class InterfaceAppBatchQueryCallable
    extends BatchCallableRunnable<InterfaceAppIndexQueryModel, InterfaceMapping> {

  private static final Logger LOG =
      LoggerFactory.getLogger("METADATA-EXCHANGE", "[InterfaceAppBatchQuery]");

  @Autowired private InterfaceAppsIndexMapper interfaceAppsIndexMapper;

  public InterfaceAppBatchQueryCallable() {
    super(20, TimeUnit.MILLISECONDS, 200);
  }

  @Override
  public boolean batchProcess(List<TaskEvent> taskEvents) {

    if (CollectionUtils.isEmpty(taskEvents)) {
      return true;
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("commit interface_apps_index interface query, task size: " + taskEvents.size());
    }
    List<InterfaceAppIndexQueryModel> querys =
        taskEvents.stream().map(task -> task.getData()).collect(Collectors.toList());
    List<InterfaceAppsIndexDomain> domains = interfaceAppsIndexMapper.batchQueryByInterface(querys);

    Map<
            String
            /** interfaces */
            ,
            Set<String>
        /** app */
        >
        indexResult = new HashMap<>();
    Map<
            String
            /** interfaces */
            ,
            Long
        /** version */
        >
        versionResult = new HashMap<>();
    domains.forEach(
        domain -> {
          indexResult
              .computeIfAbsent(domain.getInterfaceName(), k -> Sets.newConcurrentHashSet())
              .add(domain.getAppName());

          Long v1 = versionResult.get(domain.getInterfaceName());
          long v2 = TimestampUtil.getNanosLong(domain.getGmtModify());
          if (v1 == null || v2 > v1) {
            versionResult.put(domain.getInterfaceName(), v2);
          }
        });

    taskEvents.forEach(
        taskEvent -> {
          String interfaceName = taskEvent.getData().getInterfaceName();
          InvokeFuture<InterfaceMapping> future = taskEvent.getFuture();
          Set<String> appNames = indexResult.get(interfaceName);
          Long version = versionResult.get(interfaceName);

          if (appNames == null || version == null) {
            future.putResponse(new InterfaceMapping(-1));
          } else {
            future.putResponse(new InterfaceMapping(version, appNames));
          }
        });
    return true;
  }
}
