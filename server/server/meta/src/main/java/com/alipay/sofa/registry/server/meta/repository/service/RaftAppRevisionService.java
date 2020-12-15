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
package com.alipay.sofa.registry.server.meta.repository.service;

import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.alipay.sofa.registry.jraft.processor.AbstractSnapshotProcess;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.revision.AppRevisionService;
import com.alipay.sofa.registry.store.api.annotation.RaftService;
import com.alipay.sofa.registry.util.RevisionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RaftService
public class RaftAppRevisionService extends AbstractSnapshotProcess implements AppRevisionService {
    private static final Logger              LOGGER            = LoggerFactory
                                                                   .getLogger(RaftAppRevisionService.class);

    private Set<String>                      snapShotFileNames = new HashSet<>();

    private Map<String, AppRevisionRegister> registry          = new ConcurrentHashMap<>();
    private String                           keysDigest        = "";

    private static final String              REVISIONS_NAME    = "revisions";
    private ReadWriteLock                    rwLock            = new ReentrantReadWriteLock();

    public RaftAppRevisionService() {
    }

    public RaftAppRevisionService(Map<String, AppRevisionRegister> registry) {
        this.registry = registry;
    }

    @Override
    public boolean save(String path) {
        if (path.endsWith(REVISIONS_NAME)) {
            return save(path, registry);
        }
        return true;
    }

    @Override
    public boolean load(String path) {
        try {
            if (path.endsWith(REVISIONS_NAME)) {
                Map<String, AppRevisionRegister> reg = load(path, registry.getClass());
                if (reg == null) {
                    reg = new HashMap<>();
                }
                registry = reg;
                keysDigest = generateKeysDigest();
                return true;
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Load app revisions error:", e);
            return false;
        }
    }

    @Override
    public SnapshotProcess copy() {
        return new RaftAppRevisionService(registry);
    }

    @Override
    public Set<String> getSnapshotFileNames() {
        if (!snapShotFileNames.isEmpty()) {
            return snapShotFileNames;
        }
        snapShotFileNames.add(this.getClass().getSimpleName() + "_" + REVISIONS_NAME);
        return snapShotFileNames;
    }

    public void add(AppRevisionRegister appRevision) {
        rwLock.writeLock().lock();
        if (registry.putIfAbsent(appRevision.getRevision(), appRevision) == null) {
            keysDigest = generateKeysDigest();
        }
        rwLock.writeLock().unlock();
    }

    public boolean existed(String revision) {
        return registry.containsKey(revision);
    }

    public AppRevisionRegister get(String revision) {
        return registry.get(revision);
    }

    public String getKeysDigest() {
        return keysDigest;
    }

    public List<AppRevisionRegister> getMulti(List<String> keys) {
        if (keys == null) {
            return new ArrayList<>();
        }
        List<AppRevisionRegister> ret = new ArrayList<>(keys.size());
        for (String key : keys) {
            AppRevisionRegister rev = registry.get(key);
            if (rev != null) {
                ret.add(rev);
            }
        }
        return ret;
    }

    public List<String> getKeys() {
        return new ArrayList<>(registry.keySet());
    }

    private String generateKeysDigest() {
        return RevisionUtils.revisionsDigest(new ArrayList<>(registry.keySet()));
    }
}
