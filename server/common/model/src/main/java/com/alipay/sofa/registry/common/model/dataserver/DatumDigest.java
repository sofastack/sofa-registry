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

import com.alipay.sofa.registry.util.StringFormatter;
import java.io.Serializable;
import java.util.Objects;

public final class DatumDigest implements Serializable {
  private final long publisherIdSign;
  private final long publisherVerSign;
  private final long publisherTimestampSign;
  private final int publisherNum;
  private final short maxTimestamp;
  private final short minTimestamp;

  public DatumDigest(
      int publisherNum,
      long publisherIdSign,
      long publisherVerSign,
      long publisherTimestampSign,
      short maxTimestamp,
      short minTimestamp) {
    this.publisherNum = publisherNum;
    this.publisherIdSign = publisherIdSign;
    this.publisherVerSign = publisherVerSign;
    this.publisherTimestampSign = publisherTimestampSign;
    this.maxTimestamp = maxTimestamp;
    this.minTimestamp = minTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DatumDigest that = (DatumDigest) o;
    return publisherIdSign == that.publisherIdSign
        && publisherVerSign == that.publisherVerSign
        && publisherTimestampSign == that.publisherTimestampSign
        && publisherNum == that.publisherNum
        && maxTimestamp == that.maxTimestamp
        && minTimestamp == that.minTimestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        publisherIdSign,
        publisherVerSign,
        publisherTimestampSign,
        publisherNum,
        maxTimestamp,
        minTimestamp);
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "Digest{num={},idSign={},verSign={},tsSign={},maxTs={},minTs={}",
        publisherNum,
        publisherIdSign,
        publisherVerSign,
        publisherTimestampSign,
        maxTimestamp,
        minTimestamp);
  }
}
