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
package com.alipay.sofa.registry.util;

import java.util.concurrent.ThreadLocalRandom;

public final class BackOffTimes {
  private static final int[] RANDOM_MILLIS;

  static {
    RANDOM_MILLIS = new int[30];
    for (int i = 0; i < RANDOM_MILLIS.length; i++) {
      RANDOM_MILLIS[i] = 50 + i * 10;
    }
  }

  public static int getBackOffMillis(int retry, int initMillis, int incrementMillis) {
    final int idx = ThreadLocalRandom.current().nextInt(RANDOM_MILLIS.length);
    final int rand = RANDOM_MILLIS[idx];
    if (retry <= 1) {
      return initMillis + rand;
    }
    return initMillis + (incrementMillis * (retry - 1)) + rand;
  }

  public static int maxBackOffRandoms() {
    return RANDOM_MILLIS[RANDOM_MILLIS.length - 1];
  }
}
