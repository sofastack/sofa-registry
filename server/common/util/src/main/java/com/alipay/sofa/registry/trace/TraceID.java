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
package com.alipay.sofa.registry.trace;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class TraceID implements Serializable {
  private static final transient UUID SEED = UUID.randomUUID();
  private static final transient String SEED_PREFIX =
      Long.toHexString(SEED.getMostSignificantBits())
          + Long.toHexString(SEED.getLeastSignificantBits())
          + '-';
  private static final transient AtomicLong SEQ = new AtomicLong();

  private final long mostSigBits;
  private final long leastSigBits;
  private final long seq;

  private transient volatile String string;

  private TraceID(long most, long least, long seq) {
    this.mostSigBits = most;
    this.leastSigBits = least;
    this.seq = seq;
  }

  public static TraceID newTraceID() {
    return new TraceID(
        SEED.getMostSignificantBits(), SEED.getLeastSignificantBits(), SEQ.incrementAndGet());
  }

  @Override
  public String toString() {
    if (string == null) {
      string = createString();
    }
    return string;
  }

  private String createString() {
    if (SEED.getMostSignificantBits() == mostSigBits
        && SEED.getLeastSignificantBits() == leastSigBits) {
      return SEED_PREFIX + seq;
    }
    StringBuilder sb = new StringBuilder(64);
    sb.append(Long.toHexString(mostSigBits))
        .append(Long.toHexString(leastSigBits))
        .append('-')
        .append(seq);
    return sb.toString();
  }
}
