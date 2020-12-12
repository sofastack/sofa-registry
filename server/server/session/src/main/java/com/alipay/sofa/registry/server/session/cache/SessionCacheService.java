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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 *
 * @author shangyu.wh
 * @version $Id: CacheService.java, v 0.1 2017-12-06 18:22 shangyu.wh Exp $
 */
public class SessionCacheService implements CacheService {

    private static final Logger            LOGGER = LoggerFactory
                                                      .getLogger(SessionCacheService.class);

    private final LoadingCache<Key, Value> readWriteCacheMap;
    /**
     * injectQ
     */
    private Map<String, CacheGenerator>    cacheGenerators;

    /**
     * constructor
     */
    public SessionCacheService() {
        this.readWriteCacheMap = CacheBuilder.newBuilder().maximumSize(1000L)
            .expireAfterWrite(31000, TimeUnit.MILLISECONDS).build(new CacheLoader<Key, Value>() {
                @Override
                public Value load(Key key) {
                    return generatePayload(key);
                }
            });
    }

    private Value generatePayload(Key key) {
        if (key == null || key.getEntityType() == null) {
            throw new IllegalArgumentException("Generator key input error!");
        }

        Value value = null;
        switch (key.getKeyType()) {
            case OBJ:
                EntityType entityType = key.getEntityType();
                CacheGenerator cacheGenerator = cacheGenerators
                    .get(entityType.getClass().getName());
                value = cacheGenerator.generatePayload(key);
                break;
            case JSON:
                break;
            case XML:
                break;
            default:
                LOGGER.error("Unidentified data type: " + key.getKeyType()
                             + " found in the cache key.");
                value = new Value(new HashMap<String, Object>());
                break;
        }
        return value;
    }

    @Override
    public Value getValue(final Key key) throws CacheAccessException {
        try {
            return readWriteCacheMap.get(key);
        } catch (Throwable e) {
            String msg = "Cannot get value for key is:" + key;
            throw new CacheAccessException(msg, e);
        }
    }

    @Override
    public Value getValueIfPresent(Key key) {
        return readWriteCacheMap.getIfPresent(key);
    }

    @Override
    public Map<Key, Value> getValues(final Iterable<Key> keys) throws CacheAccessException {
        try {
            return readWriteCacheMap.getAll(keys);
        } catch (Throwable e) {
            String msg = "Cannot get value for keys are:" + keys;
            throw new CacheAccessException(msg, e);
        }
    }

    @Override
    public void invalidate(Key... keys) {
        for (Key key : keys) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Invalidating the response cache key : {} {} {}", key.getEntityType(),
                    key.getEntityName(), key.getKeyType());
            }

            readWriteCacheMap.invalidate(key);
        }
    }

    /**
     * Setter method for property <tt>cacheGenerators</tt>.
     *
     * @param cacheGenerators  value to be assigned to property cacheGenerators
     */
    @Autowired
    public void setCacheGenerators(Map<String, CacheGenerator> cacheGenerators) {
        this.cacheGenerators = cacheGenerators;
    }
}