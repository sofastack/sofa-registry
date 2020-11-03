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

import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.AppRevisionKey;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.service.AppRevisionNodeService;
import com.alipay.sofa.registry.util.RevisionUtils;
import com.alipay.sofa.registry.util.SingleFlight;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class AppRevisionCacheRegistry {

    private static final Logger                                                     LOG                = LoggerFactory
                                                                                                           .getLogger(AppRevisionCacheRegistry.class);

    @Autowired
    private AppRevisionNodeService                                                  appRevisionNodeService;

    final private Map<AppRevisionKey, AppRevisionRegister>                          registry           = new ConcurrentHashMap<>();
    private String                                                                  keysDigest         = "";
    final private Map<String /*interface*/, Map<String /*appname*/, Set<String>>> interfaceRevisions = new ConcurrentHashMap<>();
    private SingleFlight                                                            singleFlight       = new SingleFlight();

    public AppRevisionCacheRegistry() {
    }

    public void register(AppRevisionRegister appRevision) throws Exception {
        AppRevisionKey key = new AppRevisionKey(appRevision.appname, appRevision.revision);
        if (this.registry.containsKey(key)) {
            return;
        }
        singleFlight.execute(key, new AppRevisionRegisterTask(appRevision));
    }

    public Map<String, Set<String>> search(String dataInfoId) {
        return interfaceRevisions.get(dataInfoId);
    }

    public void refreshAll() {
        List<AppRevisionRegister> revisions = appRevisionNodeService
            .fetchMulti(appRevisionNodeService.checkRevisions(keysDigest));
        for (AppRevisionRegister rev : revisions) {
            onNewRevision(rev);
        }
        if (revisions.size() > 0) {
            keysDigest = generateKeysDigest();
        }
    }

    private void onNewRevision(AppRevisionRegister rev) {
        AppRevisionKey key = new AppRevisionKey(rev.appname, rev.revision);
        if (registry.putIfAbsent(key, rev) != null) {
            return;
        }
        for (AppRevisionInterface inf : rev.interfaces) {
            Map<String, Set<String>> apps = interfaceRevisions.computeIfAbsent(
                DataInfo.toDataInfoId(inf.dataId, inf.instanceId, inf.group),
                k -> new ConcurrentHashMap<>());
            Set<String> infRevisions = apps.computeIfAbsent(rev.appname,
                k -> Sets.newConcurrentHashSet());
            infRevisions.add(rev.revision);
        }
    }

    private String generateKeysDigest() {
        return RevisionUtils.revisionsDigest(new ArrayList<>(registry.keySet()));
    }

    private void onInterfaceChanged(String dataInfoId) {
    }

    private class AppRevisionRegisterTask implements Callable {
        private AppRevisionRegister revision;

        public AppRevisionRegisterTask(AppRevisionRegister revision) {
            this.revision = revision;
        }

        @Override
        public Object call() throws Exception {
            appRevisionNodeService.register(revision);
            return null;
        }
    }
}
