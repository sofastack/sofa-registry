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

import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.StringUtils;
import java.util.Objects;

public class CompressDatumKey implements CompressKey {
  private final String encode;
  private final String dataInfoId;
  private final String dataCenter;
  private final long version;
  private final int pubNum;
  private final int byteSize;

  public CompressDatumKey(
      String encode, String dataInfoId, String dataCenter, long version, int pubNum) {
    this.encode = encode;
    this.dataInfoId = dataInfoId;
    this.dataCenter = dataCenter;
    this.version = version;
    this.pubNum = pubNum;
    this.byteSize = calcSize();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompressDatumKey that = (CompressDatumKey) o;
    return version == that.version
        && pubNum == that.pubNum
        && Objects.equals(encode, that.encode)
        && Objects.equals(dataInfoId, that.dataInfoId)
        && Objects.equals(dataCenter, that.dataCenter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(encode, dataInfoId, dataCenter, version, pubNum);
  }

  @Override
  public int size() {
    return byteSize;
  }

  private int calcSize() {
    return StringUtils.sizeof(encode)
        + StringUtils.sizeof(dataCenter)
        + StringUtils.sizeof(dataCenter)
        + 16;
  }

  @Override
  public String toString() {
    return StringFormatter.format("dataInfoId={}, ver={}, encode={}", dataInfoId, version, encode);
  }
}
