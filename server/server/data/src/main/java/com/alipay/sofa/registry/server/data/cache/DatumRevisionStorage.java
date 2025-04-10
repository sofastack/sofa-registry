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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.dataserver.DatumRevisionData;
import com.alipay.sofa.registry.common.model.dataserver.DatumRevisionKey;
import com.alipay.sofa.registry.util.SystemUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import java.util.Map;

/**
 * @author huicha
 * @date 2025/3/11
 */
public class DatumRevisionStorage {

  private static final DatumRevisionStorage INSTANCE = new DatumRevisionStorage();

  public static DatumRevisionStorage getInstance() {
    return DatumRevisionStorage.INSTANCE;
  }

  private final Cache<DatumRevisionKey, DatumRevisionData> cache;

  private DatumRevisionStorage() {
    // todo(xidong.rxd): 重新考虑下参数配置 ???
    long size = SystemUtils.getSystemLong("registry.data.datum.revision.size", 10000L);
    this.cache = CacheBuilder.newBuilder().maximumSize(size).build();
  }

  public void upsertDatumRevision(DatumRevisionKey key, DatumRevisionData value) {
    this.cache.put(key, value);
  }

  public DatumRevisionData loadDatumRevision(DatumRevisionKey key) {
    return this.cache.getIfPresent(key);
  }

  public Map<DatumRevisionKey, DatumRevisionData> loadDatumRevisions(List<DatumRevisionKey> keys) {
    return this.cache.getAllPresent(keys);
  }
}
