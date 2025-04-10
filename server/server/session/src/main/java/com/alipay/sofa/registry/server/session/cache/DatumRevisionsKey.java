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

import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author huicha
 * @date 2025/3/13
 */
public class DatumRevisionsKey implements EntityType {

  private final String dataInfoId;

  private final Set<String> dataCenters;

  private final String uniqueKey;

  public DatumRevisionsKey(String dataInfoId, Set<String> dataCenters) {
    ParaCheckUtil.checkNotEmpty(dataCenters, "dataCenters");
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
    this.dataCenters =
        dataCenters.stream().map(WordCache::getWordCache).collect(Collectors.toSet());
    this.uniqueKey = WordCache.getWordCache(createUniqueKey());
  }

  @Override
  public String getUniqueKey() {
    return uniqueKey;
  }

  private String createUniqueKey() {
    String dataCenters = this.dataCenters.stream().sorted().collect(Collectors.joining("COMMA"));
    return dataCenters + "COMMA" + this.dataInfoId;
  }

  @Override
  public int hashCode() {
    return uniqueKey.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof DatumRevisionsKey) {
      return uniqueKey.equals(((DatumRevisionsKey) other).uniqueKey);
    } else {
      return false;
    }
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public Set<String> getDataCenters() {
    return dataCenters;
  }

  @Override
  public String toString() {
    return StringFormatter.format("DatumRevisionsKey{{}}", uniqueKey);
  }
}
