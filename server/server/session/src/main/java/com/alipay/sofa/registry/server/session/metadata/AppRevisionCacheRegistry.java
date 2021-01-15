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
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.store.api.driver.RepositoryManager;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AppRevisionCacheRegistry {

    private AppRevisionRepository   appRevisionRepository;

    private InterfaceAppsRepository interfaceAppsRepository;

    @Autowired
    private SessionServerConfig     sessionServerConfig;

    private final class RevisionWatchDog extends LoopRunnable {
        @Override
        public void runUnthrowable() {
            appRevisionRepository.refresh(sessionServerConfig.getSessionServerDataCenter());
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
    }

    public void register(AppRevision appRevision) throws Exception {
        appRevision.setDataCenter(sessionServerConfig.getSessionServerDataCenter());
        appRevisionRepository.register(appRevision);
    }

    public Tuple<Long, Set<String>> getAppNames(String dataInfoId) {
        return interfaceAppsRepository.getAppNames(
            sessionServerConfig.getSessionServerDataCenter(), dataInfoId);
    }

    public AppRevision getRevision(String revision) {
        return appRevisionRepository.queryRevision(
            sessionServerConfig.getSessionServerDataCenter(), revision);
    }

}
