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
package com.alipay.sofa.registry.common.model.store;

import com.google.common.collect.Maps;
import java.util.Map;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-26 11:46 yuzhi.lyz Exp $
 */
public class SubTest {

  static final int count = 500;
  static final int tests = 1000000;
  static final String dataCenter = "abcdefgaaaaaaa";
  static final int ver = 0;

  public static void main(String[] args) {
    Map<String, Subscriber> subs = Maps.newConcurrentMap();
    initMap(subs, count);
    for (int i = 0; i < tests; i++) {
      test1(subs, dataCenter, ver);
    }
    long start = System.currentTimeMillis();
    for (int i = 0; i < tests; i++) {
      test1(subs, dataCenter, ver);
    }
    long end = System.currentTimeMillis();
    System.out.println("@@" + (end - start));
  }

  private static boolean test1(Map<String, Subscriber> map, String dataCenter, long ver) {
    for (Subscriber s : map.values()) {
      if (s.checkVersion(dataCenter, ver)) {
        return true;
      }
    }
    return false;
  }

  private static void initMap(Map<String, Subscriber> map, int count) {
    String key = String.valueOf(System.currentTimeMillis());
    for (int i = 0; i < count; i++) {
      Subscriber sub = new Subscriber();
      sub.checkVersion(dataCenter, 10L);
      map.put(key + "_" + i, sub);
    }
  }
}
