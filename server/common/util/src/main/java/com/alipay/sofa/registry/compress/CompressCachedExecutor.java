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
package com.alipay.sofa.registry.compress;

import com.alipay.sofa.registry.cache.Sizer;
import com.alipay.sofa.registry.concurrent.CachedExecutor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

public class CompressCachedExecutor<V extends Sizer> extends CachedExecutor<CompressKey, V> {
  private static final Logger LOG = LoggerFactory.getLogger("COMPRESS");
  private final String name;

  public CompressCachedExecutor(String name, long silentMs, long maxWeight) {
    super(silentMs, maxWeight, (CompressKey k, V v) -> k.size() + v.size(), true);
    this.name = name;
  }

  @Override
  protected void onMiss(CompressKey key) {
    LOG.info("[CompressCacheMiss] executor {} on miss: {}", name, key);
  }
}
