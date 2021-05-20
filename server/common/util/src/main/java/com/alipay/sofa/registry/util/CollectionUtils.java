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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The type Collection utils.
 *
 * @author zhuoyu.sjw
 * @version $Id : CollectionUtils.java, v 0.1 2018-04-12 14:54 zhuoyu.sjw Exp $$
 */
public final class CollectionUtils {
  private CollectionUtils() {}
  /**
   * Gets random.
   *
   * @param <E> the type parameter
   * @param e the e
   * @return the random
   */
  public static <E> E getRandom(List<E> e) {
    if (e.isEmpty()) {
      return null;
    }
    int idx = ThreadLocalRandom.current().nextInt(e.size());
    return e.get(idx);
  }

  public static <K, V> Map<K, V> toSingletonMap(Map<K, V> m) {
    if (m.size() != 1) {
      return m;
    }
    Map.Entry<K, V> one = m.entrySet().stream().findFirst().get();
    return Collections.singletonMap(one.getKey(), one.getValue());
  }
}
