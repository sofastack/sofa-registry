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

import java.io.ByteArrayOutputStream;

public class ThreadLocalByteArrayOutputStream {
  private static final int maxBufferSize = 1024 * 1024 * 16;
  private static final int initSize = 1024 * 16;
  private static final transient ThreadLocal<SizeBAOS> builder =
      ThreadLocal.withInitial(() -> new SizeBAOS(initSize));

  public static ByteArrayOutputStream get() {
    SizeBAOS b = builder.get();
    b.reset();
    return b;
  }

  private static class SizeBAOS extends ByteArrayOutputStream {
    public SizeBAOS(int size) {
      super(size);
    }

    public int size() {
      return buf.length;
    }

    @Override
    public synchronized void reset() {
      if (size() > maxBufferSize) {
        buf = new byte[initSize];
      }
      super.reset();
    }
  }
}
