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
package com.alipay.sofa.registry.server.session.cache;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.service.AppRevisionNodeService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.RevisionUtils;
import com.alipay.sofa.registry.util.SingleFlight;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AppRevisionCacheRegistry {

    private static final Logger                                                     LOG                = LoggerFactory
                                                                                                           .getLogger(AppRevisionCacheRegistry.class);

    @Autowired
    private AppRevisionNodeService                                                  appRevisionNodeService;

    final private Map<String /*revision*/, AppRevision>                            registry           = new ConcurrentHashMap<>();
    private volatile String                                                         keysDigest         = "";
    final private Map<String /*interface*/, Map<String /*appname*/, Set<String>>> interfaceRevisions = new ConcurrentHashMap<>();
    final private Map<String /*appname*/, Set<String /*interfaces*/>>             appInterfaces      = new ConcurrentHashMap<>();
    private SingleFlight                                                            singleFlight       = new SingleFlight();

    @PostConstruct
    public void init() {
        ConcurrentUtils
            .createDaemonThread("SessionRefreshRevisionWatchDog", new RevisionWatchDog()).start();
    }

    private final class RevisionWatchDog extends LoopRunnable {
        @Override
        public void runUnthrowable() {
            refreshAll();
        }

        @Override
        public void waitingUnthrowable() {
            ConcurrentUtils.sleepUninterruptibly(5, TimeUnit.SECONDS);
        }
    }

    public void register(AppRevision appRevision) throws Exception {
        if (this.registry.containsKey(appRevision.getRevision())) {
            return;
        }
        singleFlight.execute("revisionRegister" + appRevision.getRevision(), () -> {
            appRevisionNodeService.register(appRevision);
            return null;
        });
    }

    public Map<String, Set<String>> getAppRevisions(String dataInfoId) {
        final Map<String, Set<String>> ret = interfaceRevisions.get(dataInfoId);
        return ret == null ? Collections.emptyMap() : ret;
    }

    public AppRevision getRevision(String revision) {
        for (int i = 0; i < 2; i++) {
            // 第一次可能会被前一个revision的请求合并到导致虽然在meta内没有fetch回来，第二个fetch肯定能拿到对应 revisipn
            AppRevision revisionRegister = registry.get(revision);
            if (revisionRegister != null) {
                return revisionRegister;
            }
            refreshAll();
        }
        return registry.get(revision);
    }

    public Set<String> getInterfaces(String appname) {
        final Set<String> ret = appInterfaces.get(appname);
        return ret == null ? Collections.emptySet() : ret;
    }

    public void refreshAll() {
        try {
            singleFlight.execute("refreshAll", () -> {
                List<String> allRevisionIds = appRevisionNodeService.checkRevisions(keysDigest);
                if (allRevisionIds == null || allRevisionIds.size() == 0) {
                    return null;
                }
                Set<String> newRevisionIds = Sets.difference(new HashSet<>(allRevisionIds), registry.keySet());
                LOG.info("refresh revisions: {}, newRevisionIds: {} ", keysDigest, newRevisionIds);
                List<AppRevision> revisions = appRevisionNodeService
                        .fetchMulti(new ArrayList<>(newRevisionIds));
                for (AppRevision rev : revisions) {
                    onNewRevision(rev);
                }
                if (revisions.size() > 0) {
                    keysDigest = generateKeysDigest();
                }
                return null;
            });
        } catch (Exception e) {
            LOG.error("refresh revisions failed ", e);
            throw new RuntimeException("refresh revision failed", e);
        }
    }

    public void refreshMeta(Collection<String> revisions) {
        if (CollectionUtils.isEmpty(revisions)) {
            return;
        }
        for (String revision : revisions) {
            getRevision(revision);
        }
    }

    private void onNewRevision(AppRevision rev) {
        if (rev.getInterfaceMap() == null) {
            LOG.warn("AppRevision no interface, {}", rev);
            return;
        }
        for (AppRevisionInterface inf : rev.getInterfaceMap().values()) {
            String dataInfoId = DataInfo.toDataInfoId(inf.getDataId(), inf.getInstanceId(), inf.getGroup());
            Map<String, Set<String>> apps = interfaceRevisions.computeIfAbsent(dataInfoId,
                    k -> new ConcurrentHashMap<>());
            Set<String> infRevisions = apps.computeIfAbsent(rev.getAppName(),
                    k -> Sets.newConcurrentHashSet());
            infRevisions.add(rev.getRevision());

            appInterfaces.computeIfAbsent(rev.getAppName(), k -> Sets.newConcurrentHashSet())
                    .add(dataInfoId);
        }
        registry.put(rev.getRevision(), rev);
        LOG.info("onNewRevision {}", rev);
    }

    private String generateKeysDigest() {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, AppRevision> entry : registry.entrySet()) {
            keys.add(entry.getKey());
        }
        return RevisionUtils.revisionsDigest(keys);
    }
}
