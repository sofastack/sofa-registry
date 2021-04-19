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
import com.alipay.sofa.registry.util.StringFormatter;

/**
 * @author shangyu.wh
 * @version $Id: DatumKey.java, v 0.1 2018-11-19 16:08 shangyu.wh Exp $
 */
public class DatumKey implements EntityType {

  private final String dataInfoId;

  private final String dataCenter;

  private final String uniqueKey;

  public DatumKey(String dataInfoId, String dataCenter) {
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
    this.dataCenter = WordCache.getWordCache(dataCenter);
    this.uniqueKey = WordCache.getWordCache(createUniqueKey());
  }

  @Override
  public String getUniqueKey() {
    return uniqueKey;
  }

  private String createUniqueKey() {
    StringBuilder sb = new StringBuilder(dataCenter.length() + dataInfoId.length() + 1);
    sb.append(dataCenter).append(COMMA).append(dataInfoId);
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
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  @Override
  public String toString() {
    return StringFormatter.format("DatumKey{{}}", uniqueKey);
  }
}
