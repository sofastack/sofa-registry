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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.cache.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: CacheService.java, v 0.1 2017-12-06 18:22 shangyu.wh Exp $
 */
public abstract class SessionCacheService implements CacheService {
  private static final Logger CACHE_LOGGER = LoggerFactory.getLogger("CACHE-GEN");

  protected LoadingCache<Key, Value> readWriteCacheMap;

  /** injectQ */
  private Map<String, CacheGenerator> cacheGenerators;

  static final class RemoveListener implements RemovalListener<Key, Value> {

    @Override
    public void onRemoval(RemovalNotification<Key, Value> notification) {
      final RemovalCause cause = notification.getCause();
      if (cause == RemovalCause.SIZE || cause == RemovalCause.COLLECTED) {
        Key key = notification.getKey();
        EntityType entityType = key.getEntityType();
        if (entityType instanceof DatumKey) {
          DatumKey datumKey = (DatumKey) entityType;
          CACHE_LOGGER.info(
              "remove,{},{},{}", datumKey.getDataInfoId(), datumKey.getDataCenters(), cause);
        }
      }
    }
  }

  protected Value generatePayload(Key key) {
    ParaCheckUtil.checkNotNull(key, "generatePayload.key");
    ParaCheckUtil.checkNotNull(key.getEntityType(), "generatePayload.key.entityType");

    EntityType entityType = key.getEntityType();
    CacheGenerator cacheGenerator = cacheGenerators.get(entityType.getClass().getName());
    return cacheGenerator.generatePayload(key);
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
  public void invalidate(Key key) {
    readWriteCacheMap.invalidate(key);
  }

  /**
   * Setter method for property <tt>cacheGenerators</tt>.
   *
   * @param cacheGenerators value to be assigned to property cacheGenerators
   */
  @Autowired
  public void setCacheGenerators(Map<String, CacheGenerator> cacheGenerators) {
    this.cacheGenerators = cacheGenerators;
  }
}
