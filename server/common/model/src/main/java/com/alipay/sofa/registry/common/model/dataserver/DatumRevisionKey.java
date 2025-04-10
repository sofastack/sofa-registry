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
package com.alipay.sofa.registry.common.model.dataserver;

import com.alipay.sofa.registry.common.model.store.WordCache;
import java.io.Serializable;

/**
 * @author huicha
 * @date 2025/3/11
 */
public class DatumRevisionKey implements Serializable {

  private static final long serialVersionUID = -2738103684065545625L;

  private static final char COMMA = '#';

  private final String dataCenter;

  private final String dataInfoId;

  private final long version;

  private final boolean mock;

  private final String uniqueKey;

  public static DatumRevisionKey of(
      String dataCenter, String dataInfoId, DatumRevisionMark datumRevisionMark) {
    return new DatumRevisionKey(
        dataCenter, dataInfoId, datumRevisionMark.getVersion(), datumRevisionMark.isMock());
  }

  public DatumRevisionKey(String dataCenter, String dataInfoId, long version, boolean mock) {
    this.dataCenter = WordCache.getWordCache(dataCenter);
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
    this.version = version;
    this.mock = mock;
    this.uniqueKey = WordCache.getWordCache(this.createUniqueKey());
  }

  private String createUniqueKey() {
    return new StringBuilder()
        .append(dataCenter)
        .append(COMMA)
        .append(dataInfoId)
        .append(COMMA)
        .append(version)
        .toString();
  }

  public String getDataCenter() {
    return dataCenter;
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public long getVersion() {
    return version;
  }

  public boolean isMock() {
    return mock;
  }

  public DatumRevisionKey intern() {
    // these string will be intern in construct
    return new DatumRevisionKey(this.dataCenter, this.dataInfoId, this.version, this.mock);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof DatumRevisionKey) {
      return uniqueKey.equals(((DatumRevisionKey) other).uniqueKey);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.uniqueKey.hashCode();
  }
}
