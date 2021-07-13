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
package com.alipay.sofa.registry.concurrent;

public final class ThreadLocalStringBuilder {
  private static final int maxBufferSize = 8192;
  private static final transient ThreadLocal<StringBuilder> builder =
      ThreadLocal.withInitial(() -> new StringBuilder(maxBufferSize));

  private ThreadLocalStringBuilder() {}

  public static StringBuilder get() {
    StringBuilder b = builder.get();
    if (b.capacity() > maxBufferSize) {
      b = new StringBuilder(maxBufferSize);
      builder.set(b);
    } else {
      b.setLength(0);
    }
    return b;
  }

  public static String join(String e1, String e2) {
    StringBuilder sb = get();
    sb.append(e1).append(e2);
    return sb.toString();
  }

  public static String join(String e1, String e2, String e3) {
    StringBuilder sb = get();
    sb.append(e1).append(e2).append(e3);
    return sb.toString();
  }

  public static String join(String e1, String e2, String e3, String e4) {
    StringBuilder sb = get();
    sb.append(e1).append(e2).append(e3).append(e4);
    return sb.toString();
  }

  public static String join(String e1, String e2, String e3, String e4, String e5) {
    StringBuilder sb = get();
    sb.append(e1).append(e2).append(e3).append(e4).append(e5);
    return sb.toString();
  }

  public static String join(String e1, String e2, String e3, String e4, String e5, String... es) {
    StringBuilder sb = get();
    sb.append(e1).append(e2).append(e3).append(e4).append(e5);
    for (String e : es) {
      sb.append(e);
    }
    return sb.toString();
  }
}
