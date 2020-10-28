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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.service.AppRevisionNodeService;
import com.alipay.sofa.registry.util.RevisionUtils;
import com.alipay.sofa.registry.util.SingleFlight;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AppRevisionCacheRegistry {

    private static final Logger                                                     LOG                = LoggerFactory
                                                                                                           .getLogger(AppRevisionCacheRegistry.class);

    @Autowired
    private AppRevisionNodeService                                                  appRevisionNodeService;

    final private Map<String /*revision*/, AppRevisionRegister>                    registry           = new ConcurrentHashMap<>();
    private String                                                                  keysDigest         = "";
    final private Map<String /*interface*/, Map<String /*appname*/, Set<String>>> interfaceRevisions = new ConcurrentHashMap<>();
    final private Map<String /*appname*/, Set<String /*interfaces*/>>             appInterfaces      = new ConcurrentHashMap<>();
    private SingleFlight                                                            singleFlight       = new SingleFlight();

    public AppRevisionCacheRegistry() {
    }

    public void register(AppRevisionRegister appRevision) throws Exception {
        if (this.registry.containsKey(appRevision.revision)) {
            return;
        }
        singleFlight.execute("revisionRegister" + appRevision.revision, () -> {
            appRevisionNodeService.register(appRevision);
            return null;
        });
    }

    public Set<String> getApps(String dataInfoId) {
        if (!interfaceRevisions.containsKey(dataInfoId)) {
            return new HashSet<>();
        }
        return interfaceRevisions.get(dataInfoId).keySet();
    }

    public AppRevisionRegister getRevision(String revision) {
        AppRevisionRegister revisionRegister = registry.get(revision);
        if (revisionRegister != null) {
            return revisionRegister;
        }
        refreshAll();
        return registry.get(revision);
    }

    public Set<String> getInterfaces(String appname) {
        return appInterfaces.get(appname);
    }

    public void refreshAll() {
        try {
            singleFlight.execute("refreshAll", () -> {
                List<AppRevisionRegister> revisions = appRevisionNodeService
                        .fetchMulti(appRevisionNodeService.checkRevisions(keysDigest));
                for (AppRevisionRegister rev : revisions) {
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

    private void onNewRevision(AppRevisionRegister rev) {
        for (AppRevisionInterface inf : rev.interfaces.values()) {
            String dataInfoId = DataInfo.toDataInfoId(inf.dataId, inf.instanceId, inf.group);
            Map<String, Set<String>> apps = interfaceRevisions.computeIfAbsent(dataInfoId,
                    k -> new ConcurrentHashMap<>());
            Set<String> infRevisions = apps.computeIfAbsent(rev.appname,
                    k -> Sets.newConcurrentHashSet());
            infRevisions.add(rev.revision);

            appInterfaces.computeIfAbsent(rev.appname, k -> Sets.newConcurrentHashSet())
                    .add(dataInfoId);
        }
        registry.put(rev.revision, rev);
    }

    private String generateKeysDigest() {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, AppRevisionRegister> entry : registry.entrySet()) {
            keys.add(entry.getKey());
        }
        return RevisionUtils.revisionsDigest(keys);
    }
}
