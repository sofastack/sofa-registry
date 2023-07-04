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
package com.alipay.sofa.registry.server.session.metadata;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.MultiClusterSessionServerConfig;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.AtomicMap;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class MetadataCacheRegistry {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE");

  @Autowired private AppRevisionRepository appRevisionRepository;

  @Autowired private InterfaceAppsRepository interfaceAppsRepository;

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Autowired private MultiClusterSessionServerConfig multiClusterSessionServerConfig;

  @Autowired private ExecutorManager executorManager;

  private AtomicMap<String, AppRevision> registerTask = new AtomicMap<>();

  private AtomicReference<Set<String>> pushEnableSet = new AtomicReference<>();

  private final MetadataRegisterWorker registerWorker = new MetadataRegisterWorker();
  private final MultiSyncInfoReloadWorker syncInfoReloadWorker = new MultiSyncInfoReloadWorker();

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("MetadataRegisterWorker", registerWorker).start();
    ConcurrentUtils.createDaemonThread("MultiSyncInfoReloadWorker", syncInfoReloadWorker).start();
  }

  public Set<String> getPushEnableDataCenters() {
    Set<String> set = pushEnableSet.get();
    return set == null ? Collections.EMPTY_SET : set;
  }

  public void register(AppRevision appRevision) {
    registerTask.put(appRevision.getRevision(), appRevision);
  }

  private Tuple<Set<String>, Set<String>> getMetadataDataCenters() {
    Set<MultiClusterSyncInfo> multiClusterSyncInfoSet = getSyncDataCenters();

    Set<String> syncDataCenters = new HashSet<>();
    Set<String> syncEnableDataCenters = new HashSet<>();
    syncDataCenters.add(defaultCommonConfig.getDefaultClusterId());

    for (MultiClusterSyncInfo info : multiClusterSyncInfoSet) {
      syncDataCenters.add(info.getRemoteDataCenter());
      if (info.isEnablePush()) {
        syncEnableDataCenters.add(info.getRemoteDataCenter());
      }
    }
    return new Tuple<>(syncDataCenters, syncEnableDataCenters);
  }

  private Set<MultiClusterSyncInfo> getSyncDataCenters() {

    return multiClusterSyncRepository.queryLocalSyncInfos();
  }

  public void startSynced() {
    LOG.info("metadata cache enter startSynced.");
    Tuple<Set<String>, Set<String>> tuple = getMetadataDataCenters();

    appRevisionRepository.setDataCenters(tuple.o1);
    appRevisionRepository.startSynced();

    interfaceAppsRepository.setDataCenters(tuple.o1);
    interfaceAppsRepository.startSynced();

    pushEnableSet.set(tuple.o2);
    LOG.info("metadata cache finish startSynced.");
  }

  public void waitSynced() {
    appRevisionRepository.waitSynced();
    interfaceAppsRepository.waitSynced();
  }

  public boolean heartbeat(String revision) {
    return appRevisionRepository.heartbeat(revision);
  }

  public InterfaceMapping getAppNames(String dataInfoId) {
    return interfaceAppsRepository.getAppNames(dataInfoId);
  }

  public AppRevision getRevision(String revision) {
    return appRevisionRepository.queryRevision(revision);
  }

  private class MetadataRegisterWorker extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      Map<String, AppRevision> registers = registerTask.getAndReset();

      if (CollectionUtils.isEmpty(registers)) {
        return;
      }

      Map<String, Future<Boolean>> futures = doRegister(registers);

      for (Entry<String, Future<Boolean>> entry : futures.entrySet()) {
        try {
          Boolean success = entry.getValue().get(3000, TimeUnit.MILLISECONDS);

          if (success == null || !success) {
            String revision = entry.getKey();
            LOG.info("register fail, retry ro register revision:{}", revision);
            register(registers.get(revision));
          }
        } catch (Throwable throwable) {
          String revision = entry.getKey();
          register(registers.get(revision));
          LOG.error(
              "[AppRevision]do register error, retry next time. revision:{}", revision, throwable);
        }
      }
    }

    @Override
    public int getWaitingMillis() {
      return 100;
    }
  }

  private Map<String, Future<Boolean>> doRegister(Map<String, AppRevision> registers) {
    Map<String, Future<Boolean>> futures = Maps.newHashMapWithExpectedSize(registers.size());
    for (AppRevision value : registers.values()) {

      try {
        Future<Boolean> future =
            executorManager
                .getAppRevisionRegisterExecutor()
                .submit(
                    () -> {
                      try {
                        appRevisionRepository.register(value);
                        return true;
                      } catch (Throwable t) {
                        LOG.error("[AppRevision]register error, AppRevision:{}", value, t);
                        return false;
                      }
                    });
        futures.put(value.getRevision(), future);
      } catch (Throwable t) {
        LOG.error("[AppRevision]submit register error, AppRevision:{}", value, t);
        futures.put(value.getRevision(), Futures.immediateFuture(false));
      }
    }
    return futures;
  }

  private class MultiSyncInfoReloadWorker extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      Tuple<Set<String>, Set<String>> tuple = getMetadataDataCenters();
      appRevisionRepository.setDataCenters(tuple.o1);
      interfaceAppsRepository.setDataCenters(tuple.o1);

      pushEnableSet.set(tuple.o2);
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          multiClusterSessionServerConfig.getMultiClusterConfigReloadSecs(), TimeUnit.SECONDS);
    }
  }

  /**
   * Setter method for property <tt>appRevisionRepository</tt>.
   *
   * @param appRevisionRepository value to be assigned to property appRevisionRepository
   * @return MetadataCacheRegistry
   */
  @VisibleForTesting
  public MetadataCacheRegistry setAppRevisionRepository(
      AppRevisionRepository appRevisionRepository) {
    this.appRevisionRepository = appRevisionRepository;
    return this;
  }

  /**
   * Setter method for property <tt>interfaceAppsRepository</tt>.
   *
   * @param interfaceAppsRepository value to be assigned to property interfaceAppsRepository
   * @return MetadataCacheRegistry
   */
  @VisibleForTesting
  public MetadataCacheRegistry setInterfaceAppsRepository(
      InterfaceAppsRepository interfaceAppsRepository) {
    this.interfaceAppsRepository = interfaceAppsRepository;
    return this;
  }

  /**
   * Setter method for property <tt>executorManager</tt>.
   *
   * @param executorManager value to be assigned to property executorManager
   * @return MetadataCacheRegistry
   */
  @VisibleForTesting
  public MetadataCacheRegistry setExecutorManager(ExecutorManager executorManager) {
    this.executorManager = executorManager;
    return this;
  }

  /**
   * Setter method for property <tt>multiClusterSyncRepository</tt>.
   *
   * @param multiClusterSyncRepository value to be assigned to property multiClusterSyncRepository
   * @return MetadataCacheRegistry
   */
  @VisibleForTesting
  public MetadataCacheRegistry setMultiClusterSyncRepository(
      MultiClusterSyncRepository multiClusterSyncRepository) {
    this.multiClusterSyncRepository = multiClusterSyncRepository;
    return this;
  }

  /**
   * Setter method for property <tt>defaultCommonConfig</tt>.
   *
   * @param defaultCommonConfig value to be assigned to property defaultCommonConfig
   * @return MetadataCacheRegistry
   */
  @VisibleForTesting
  public MetadataCacheRegistry setDefaultCommonConfig(DefaultCommonConfig defaultCommonConfig) {
    this.defaultCommonConfig = defaultCommonConfig;
    return this;
  }

  /**
   * Setter method for property <tt>multiClusterSessionServerConfig</tt>.
   *
   * @param multiClusterSessionServerConfig value to be assigned to property
   *     multiClusterSessionServerConfig
   * @return MetadataCacheRegistry
   */
  @VisibleForTesting
  public MetadataCacheRegistry setMultiClusterSessionServerConfig(
      MultiClusterSessionServerConfig multiClusterSessionServerConfig) {
    this.multiClusterSessionServerConfig = multiClusterSessionServerConfig;
    return this;
  }
}
