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

import com.alipay.sofa.registry.cache.CacheCleaner;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.Weigher;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : SessionDatumCacheService.java, v 0.1 2022年06月24日 20:17 xiaojian.xj Exp $
 */
public class SessionDatumCacheService extends SessionCacheService {

  @Autowired SessionServerConfig sessionServerConfig;

  @PostConstruct
  public void init() {
    this.readWriteCacheMap =
        CacheBuilder.newBuilder()
            .maximumWeight(sessionServerConfig.getCacheDatumMaxWeight())
            .weigher((Weigher<Key, Value>) (key, value) -> key.size() + value.size())
            .expireAfterWrite(sessionServerConfig.getCacheDatumExpireSecs(), TimeUnit.SECONDS)
            .removalListener(new RemoveListener())
            .build(
                new CacheLoader<Key, Value>() {
                  @Override
                  public Value load(Key key) {
                    return generatePayload(key);
                  }
                });
    CacheCleaner.autoClean(readWriteCacheMap, 10);
  }
}
