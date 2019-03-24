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

import com.alipay.sofa.registry.jraft.processor.AbstractSnapshotProcess;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.repository.VersionRepositoryService;
import com.alipay.sofa.registry.store.api.annotation.RaftService;
import com.alipay.sofa.registry.util.VersionsMapUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataVersionRepositoryService.java, v 0.1 2018-05-11 12:10 shangyu.wh Exp $
 */
@RaftService(uniqueId = "sessionServer")
public class SessionVersionRepositoryService extends AbstractSnapshotProcess
                                                                            implements
                                                                            VersionRepositoryService<String> {

    private static final Logger                                         LOGGER                 = LoggerFactory
                                                                                                   .getLogger(SessionVersionRepositoryService.class);
    /**
     * store dataCenter session list version,now just local dataCenter version
     */
    private ConcurrentHashMap<String/*dataCenter*/, Long /*version*/> dataCenterListVersions = new ConcurrentHashMap();

    private Set<String>                                                 snapShotFileNames      = new HashSet<>();

    /**
     * constructor
     */
    public SessionVersionRepositoryService() {
    }

    /**
     * constructor
     * @param dataCenterListVersions
     */
    public SessionVersionRepositoryService(ConcurrentHashMap<String, Long> dataCenterListVersions) {
        this.dataCenterListVersions = dataCenterListVersions;
    }

    @Override
    public SnapshotProcess copy() {
        return new SessionVersionRepositoryService(new ConcurrentHashMap<>(dataCenterListVersions));
    }

    @Override
    public boolean checkAndUpdateVersions(String dataCenterId, Long version) {
        return VersionsMapUtils.checkAndUpdateVersions(dataCenterListVersions, dataCenterId,
            version);
    }

    @Override
    public Long getVersion(String dataCenterId) {
        return dataCenterListVersions.get(dataCenterId);
    }

    @Override
    public boolean save(String path) {
        return save(path, dataCenterListVersions);
    }

    @Override
    public synchronized boolean load(String path) {
        try {
            ConcurrentHashMap<String, Long> map = load(path, dataCenterListVersions.getClass());
            dataCenterListVersions.clear();
            dataCenterListVersions.putAll(map);
            return true;
        } catch (IOException e) {
            LOGGER.error("Load dataCenter versions data error!", e);
            return false;
        }
    }

    @Override
    public Set<String> getSnapshotFileNames() {
        if (!snapShotFileNames.isEmpty()) {
            return snapShotFileNames;
        }
        snapShotFileNames.add(this.getClass().getSimpleName());
        return snapShotFileNames;
    }
}