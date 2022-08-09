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

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.MultiClusterSessionServerConfig;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class MetadataCacheRegistry {

  private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE");

  @Autowired private AppRevisionRepository appRevisionRepository;

  @Autowired private InterfaceAppsRepository interfaceAppsRepository;

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private MultiClusterSessionServerConfig multiClusterSessionServerConfig;

  @Autowired private ExecutorManager executorManager;

  private AtomicMap<String, AppRevision> registerTask = new AtomicMap<>();

  private final MetadataRegisterWorker registerWorker = new MetadataRegisterWorker();
  private final MultiSyncInfoReloadWorker syncInfoReloadWorker = new MultiSyncInfoReloadWorker();

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("MetadataRegisterWorker", registerWorker).start();
    ConcurrentUtils.createDaemonThread("MultiSyncInfoReloadWorker", syncInfoReloadWorker).start();
  }

  public void register(AppRevision appRevision) {
    registerTask.put(appRevision.getRevision(), appRevision);
  }

  private Set<String> getMetadataDataCenters() {
    Set<String> dataCenters = getSyncDataCenters();
    dataCenters.add(sessionServerConfig.getSessionServerDataCenter());
    dataCenters.addAll(getSyncDataCenters());
    return dataCenters;
  }

  private Set<String> getSyncDataCenters() {
    Set<MultiClusterSyncInfo> syncInfos = multiClusterSyncRepository.queryLocalSyncInfos();
    return syncInfos.stream()
        .map(MultiClusterSyncInfo::getRemoteDataCenter)
        .collect(Collectors.toSet());
  }

  public void startSynced() {

    Set<String> dataCenters = getMetadataDataCenters();

    appRevisionRepository.setDataCenters(dataCenters);
    appRevisionRepository.startSynced();

    interfaceAppsRepository.setDataCenters(dataCenters);
    interfaceAppsRepository.startSynced();
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
      Set<String> dataCenters = getMetadataDataCenters();
      appRevisionRepository.setDataCenters(dataCenters);
      interfaceAppsRepository.setDataCenters(dataCenters);
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
   */
  @VisibleForTesting
  public MetadataCacheRegistry setExecutorManager(ExecutorManager executorManager) {
    this.executorManager = executorManager;
    return this;
  }
}
