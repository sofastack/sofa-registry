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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 这里不复用 SessionDatumCacheService，或者直接将 SubDatumRevisions 缓存到 SubDatum 这个数据结构上的考虑是这样的: Session
 * 后续可以针对 SubDatumRevisions 做不同的缓存策略，让 SubDatumRevisions 的生命周期可以长于 SubDatum，以尽量命中 增量推送
 *
 * @author huicha
 * @date 2025/3/13
 */
public class SessionDatumRevisionsCacheService extends SessionCacheService
    implements InitializingBean {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Override
  public void afterPropertiesSet() throws Exception {
    // todo(xidong.rxd): 这里不选择遵循 LRU 的驱逐策略，还是遵循按照大小和时间的驱逐策略可能会更好，因为丢失了可以到 Data 上找回
    // 所以这里先考虑和另外一个 Cache 一样
    this.readWriteCacheMap =
        CacheBuilder.newBuilder()
            .maximumWeight(sessionServerConfig.getCacheDatumMaxWeight())
            .weigher((Weigher<Key, Value>) (key, value) -> key.size() + value.size())
            .expireAfterWrite(10000, TimeUnit.SECONDS)
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
