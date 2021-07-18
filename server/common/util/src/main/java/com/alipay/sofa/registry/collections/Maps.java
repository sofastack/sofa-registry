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
package com.alipay.sofa.registry.collections;

import java.util.*;

public final class Maps {
  private Maps() {}

  public static <K, V> Map<K, V> trimMap(Map<K, V> map) {
    if (map == null) {
      return Collections.emptyMap();
    }
    final int size = map.size();
    if (size == 0) {
      return Collections.emptyMap();
    }
    if (size == 1) {
      final Map.Entry<K, V> e = map.entrySet().iterator().next();
      return Collections.singletonMap(e.getKey(), e.getValue());
    }
    if (size <= 4) {
      ImmutableMap4<K, V> ret = ImmutableMap4.newMap(map);
      if (ret != null) {
        return ret;
      }
    }
    return map;
  }
}
