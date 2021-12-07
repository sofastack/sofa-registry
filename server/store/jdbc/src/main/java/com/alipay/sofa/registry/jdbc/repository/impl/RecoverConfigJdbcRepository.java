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

import com.alipay.sofa.registry.jdbc.domain.RecoverConfigDomain;
import com.alipay.sofa.registry.jdbc.mapper.RecoverConfigMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import com.alipay.sofa.registry.store.api.meta.RecoverConfigRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : RecoverConfigJdbcRepository.java, v 0.1 2021年09月22日 20:20 xiaojian.xj Exp $
 */
public class RecoverConfigJdbcRepository
    implements RecoverConfigRepository, ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger("RECOVER-CONFIG", "[RecoverConfig]");

  private static AtomicReference<Map<String, Set<String>>> CONFIG_MAP = new AtomicReference<>();

  private static final AtomicBoolean INIT_FINISH = new AtomicBoolean(false);

  private static final ConcurrentMap<String, RecoverConfig> callbackHandler =
      Maps.newConcurrentMap();

  private final ConfigWatcher watcher = new ConfigWatcher();

  @Autowired private RecoverConfigMapper recoverConfigMapper;

  @Autowired private TransactionTemplate transactionTemplate;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    ConcurrentUtils.createDaemonThread(this.getClass().getSimpleName() + "WatchDog", watcher)
        .start();
  }

  @Override
  public Set<String> queryKey(String propertyTable) {
    if (StringUtils.isEmpty(propertyTable)) {
      throw new IllegalArgumentException("params is empty.");
    }
    Map<String, Set<String>> map = CONFIG_MAP.get();
    if (INIT_FINISH.get()) {
      return map.get(propertyTable);
    }
    List<RecoverConfigDomain> resp = recoverConfigMapper.query(propertyTable);
    if (CollectionUtils.isEmpty(resp)) {
      return Collections.emptySet();
    }
    return resp.stream().map(RecoverConfigDomain::getPropertyKey).collect(Collectors.toSet());
  }

  @Override
  public boolean save(String propertyTable, String propertyKey, String recoverClusterId) {
    if (StringUtils.isEmpty(propertyTable) || StringUtils.isEmpty(propertyKey)) {
      throw new IllegalArgumentException("params is empty.");
    }

    Set<String> keys = queryKey(propertyTable);
    if (!CollectionUtils.isEmpty(keys) && keys.contains(propertyKey)) {
      return true;
    }
    transactionTemplate.execute(
        (status) -> {
          recoverConfigMapper.save(new RecoverConfigDomain(propertyTable, propertyKey));
          RecoverConfig recoverConfig = callbackHandler.get(propertyTable);
          if (recoverConfig != null) {
            recoverConfig.afterConfigSet(propertyKey, recoverClusterId);
          }
          return true;
        });

    return true;
  }

  @Override
  public boolean remove(String propertyTable, String propertyKey) {
    if (StringUtils.isEmpty(propertyTable) || StringUtils.isEmpty(propertyKey)) {
      throw new IllegalArgumentException("params is empty.");
    }
    recoverConfigMapper.remove(propertyTable, propertyKey);
    return true;
  }

  @Override
  public void waitSynced() {
    if (INIT_FINISH.get()) {
      return;
    }
    doRefresh();
  }

  @Override
  public void registerCallback(RecoverConfig config) {
    callbackHandler.put(config.tableName(), config);
  }

  class ConfigWatcher extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      doRefresh();
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(60, TimeUnit.SECONDS);
    }
  }

  public void doRefresh() {
    List<RecoverConfigDomain> result = recoverConfigMapper.queryAll();
    Map<String, Set<String>> configMap = Maps.newHashMapWithExpectedSize(result.size());
    for (RecoverConfigDomain config : result) {
      LOG.info("[LOAD]config: {}", config);
      Set<String> set =
          configMap.computeIfAbsent(config.getPropertyTable(), k -> Sets.newHashSet());
      set.add(config.getPropertyKey());
    }
    this.CONFIG_MAP.set(configMap);
    LOG.info("[LOAD-FINISH]CONFIG_MAP: {}", CONFIG_MAP.get());
    if (!INIT_FINISH.get()) {
      INIT_FINISH.set(true);
    }
  }
}
