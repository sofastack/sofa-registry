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
package com.alipay.sofa.registry.jraft.service;

import com.alipay.sofa.registry.jraft.processor.AbstractSnapshotProcess;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.jraft.annotation.RaftService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version $Id: PersistenceDataDBService.java, v 0.1 2018-06-22 17:23 shangyu.wh Exp $
 */
@RaftService
public class PersistenceDataDBService extends AbstractSnapshotProcess implements DBService {

    private static final Logger               LOGGER            = LoggerFactory
                                                                    .getLogger(PersistenceDataDBService.class);

    private ConcurrentHashMap<String, Object> serviceMap        = new ConcurrentHashMap<>();

    private Set<String>                       snapShotFileNames = new HashSet<>();

    /**
     * constructor
     */
    public PersistenceDataDBService() {
    }

    /**
     * constructor
     * @param serviceMap
     */
    public PersistenceDataDBService(ConcurrentHashMap<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    public SnapshotProcess copy() {
        return new PersistenceDataDBService(new ConcurrentHashMap<>(serviceMap));
    }

    @Override
    public void openDB(String dbName, Class entityClass) {
    }

    @Override
    public boolean put(String key, Object value) {
        if (key == null || value == null) {
            LOGGER.error("key {} or value {} can't be null", key, value);
            return false;
            //throw new IllegalArgumentException("key or value can't be null!");
        }
        Object ret = serviceMap.put(key, value);
        if (ret != null) {
            LOGGER.warn("value {} with key {} will be override", ret, key);
        }
        return true;
    }

    @Override
    public DBResponse get(String key) {
        if (key == null) {
            LOGGER.error("key can't be null");
            throw new IllegalArgumentException("query key can't be null");
        }
        Object ret = serviceMap.get(key);
        return ret != null ? DBResponse.ok(ret).build() : DBResponse.notfound().build();
    }

    @Override
    public boolean update(String key, Object value) {
        if (key == null || value == null) {
            LOGGER.error("key {} or value {} can't be null", key, value);
            return false;
        }
        Object ret = serviceMap.put(key, value);
        if (ret != null) {
            LOGGER.warn("value {} with key {} will be override", ret, key);
        }
        return true;
    }

    @Override
    public boolean remove(String key) {
        if (key == null) {
            LOGGER.error("key can't be null");
            return false;
        }
        Object obj = serviceMap.remove(key);
        if (obj == null) {
            LOGGER.warn("remove key {} can't be found!", key);
            return false;
        }
        return true;
    }

    @Override
    public boolean save(String path) {
        return save(path, serviceMap);
    }

    @Override
    public boolean load(String path) {
        try {
            ConcurrentHashMap<String, Object> map = load(path, serviceMap.getClass());
            serviceMap.clear();
            serviceMap.putAll(map);
            return true;
        } catch (IOException e) {
            LOGGER.error("Load serviceMap data error!", e);
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