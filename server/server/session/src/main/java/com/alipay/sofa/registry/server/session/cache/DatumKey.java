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
 * @author shangyu.wh
 * @version $Id: DatumKey.java, v 0.1 2018-11-19 16:08 shangyu.wh Exp $
 */
public class DatumKey implements EntityType {

  private final String dataInfoId;

  private final Set<String> dataCenters;

  private final String uniqueKey;

  public DatumKey(String dataInfoId, Set<String> dataCenters) {
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
    int dataCenterSize = 0;
    StringBuilder builder = new StringBuilder();
    for (String dataCenter : dataCenters) {
      dataCenterSize += dataCenter.length();
      builder.append(dataCenter).append(COMMA);
    }
    StringBuilder sb = new StringBuilder(dataCenterSize + dataInfoId.length() + 1);
    sb.append(builder).append(dataInfoId);
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return uniqueKey.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof DatumKey) {
      return uniqueKey.equals(((DatumKey) other).uniqueKey);
    } else {
      return false;
    }
  }

  /**
   * Getter method for property <tt>dataInfoId</tt>.
   *
   * @return property value of dataInfoId
   */
  public String getDataInfoId() {
    return dataInfoId;
  }

  /**
   * Getter method for property <tt>dataCenters</tt>.
   *
   * @return property value of dataCenters
   */
  public Set<String> getDataCenters() {
    return dataCenters;
  }

  @Override
  public String toString() {
    return StringFormatter.format("DatumKey{{}}", uniqueKey);
  }
}
