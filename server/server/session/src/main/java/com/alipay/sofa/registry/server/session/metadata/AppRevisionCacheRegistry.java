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
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.store.api.driver.RepositoryManager;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

public class AppRevisionCacheRegistry {
    private static final Logger           LOGGER = LoggerFactory
                                                     .getLogger(AppRevisionCacheRegistry.class);
    private final AppRevisionRepository   appRevisionRepository;

    private final InterfaceAppsRepository interfaceAppsRepository;

    @Autowired
    private SessionServerConfig           sessionServerConfig;

    private volatile boolean              startWatch;

    private final class RevisionWatchDog extends LoopRunnable {
        @Override
        public void runUnthrowable() {
            if (!startWatch) {
                LOGGER.info("not start watch");
                return;
            }
            try {
                appRevisionRepository.refresh(sessionServerConfig.getSessionServerDataCenter());
            } catch (Throwable e) {
                LOGGER.error("failed to watch", e);
            }
        }

        @Override
        public void waitingUnthrowable() {
            ConcurrentUtils.sleepUninterruptibly(5, TimeUnit.SECONDS);
        }
    }

    @PostConstruct
    public void init() {
        // fixme bean加载时raftClient还未完成初始化
        ConcurrentUtils
            .createDaemonThread("SessionRefreshRevisionWatchDog", new RevisionWatchDog()).start();
    }

    public AppRevisionCacheRegistry(RepositoryManager repositoryManager) {
        appRevisionRepository = (AppRevisionRepository) repositoryManager
            .getRepository(AppRevisionRepository.class);
        interfaceAppsRepository = (InterfaceAppsRepository) repositoryManager
            .getRepository(InterfaceAppsRepository.class);
    }

    public void loadMetadata() {
        interfaceAppsRepository.loadMetadata(sessionServerConfig.getSessionServerDataCenter());
        startWatch = true;
    }

    public void register(AppRevision appRevision) throws Exception {
        appRevision.setDataCenter(sessionServerConfig.getSessionServerDataCenter());
        appRevisionRepository.register(appRevision);
    }

    public InterfaceMapping getAppNames(String dataInfoId) {
        return interfaceAppsRepository.getAppNames(
            sessionServerConfig.getSessionServerDataCenter(), dataInfoId);
    }

    public AppRevision getRevision(String revision) {
        return appRevisionRepository.queryRevision(
            sessionServerConfig.getSessionServerDataCenter(), revision);
    }

}
