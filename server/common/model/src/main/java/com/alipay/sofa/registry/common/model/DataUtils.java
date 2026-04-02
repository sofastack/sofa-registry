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
package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.internal.guava.Sets;

public final class DataUtils {

  private DataUtils() {}

  /**
   * instanceId/group/app - {info.count,dataInfoId.count}
   *
   * @param infos infos
   * @param <T> T
   * @return Map
   */
  public static <T extends BaseInfo>
      Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> countGroupByInstanceIdGroupApp(
          Collection<T> infos) {
    Map<String, Map<String, Map<String, Tuple<AtomicInteger, Set<String>>>>> groupBys = Maps.newHashMap();
    for (T info : infos) {
      String instanceId = info.getInstanceId();
      String group = InterestGroup.normalizeGroup(info.getGroup());
      String appName = info.getAppName();
      if (StringUtils.isBlank(appName)) {
        appName = "";
      }
      
      Map<String, Map<String, Tuple<AtomicInteger, Set<String>>>> groupCount =
          groupBys.computeIfAbsent(instanceId, k -> Maps.newHashMap());
      Map<String, Tuple<AtomicInteger, Set<String>>> appCount =
          groupCount.computeIfAbsent(group, k -> Maps.newHashMap());
      Tuple<AtomicInteger, Set<String>> tuple =
          appCount.computeIfAbsent(appName, k -> Tuple.of(new AtomicInteger(0), Sets.newHashSetWithExpectedSize(16)));
      tuple.o1.incrementAndGet();
      tuple.o2.add(info.getDataInfoId());
    }
    
    Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> ret = Maps.newHashMap();
    for (Map.Entry<String, Map<String, Map<String, Tuple<AtomicInteger, Set<String>>>>> count : groupBys.entrySet()) {
      final String instanceId = count.getKey();
      Map<String, Map<String, Tuple<Integer, Integer>>> instanceCount =
          ret.computeIfAbsent(instanceId, k -> Maps.newHashMap());
      for (Map.Entry<String, Map<String, Tuple<AtomicInteger, Set<String>>>> e : count.getValue().entrySet()) {
        final String group = e.getKey();
        Map<String, Tuple<Integer, Integer>> groupCount =
            instanceCount.computeIfAbsent(group, k -> Maps.newHashMap());
        for (Map.Entry<String, Tuple<AtomicInteger, Set<String>>> apps : e.getValue().entrySet()) {
          Tuple<AtomicInteger, Set<String>> tuple = apps.getValue();
          final int infoCount = tuple.o1.get();
          final int dataInfoIdCount = tuple.o2.size();
          groupCount.put(apps.getKey(), Tuple.of(infoCount, dataInfoIdCount));
        }
      }
    }
    return ret;
  }

  /**
   * instanceId/group - {info.count,dataInfoId.count}
   *
   * @param infos infos
   * @param <T> T
   * @return Map
   */
  public static <T extends BaseInfo>
      Map<String, Map<String, Tuple<Integer, Integer>>> countGroupByInstanceIdGroup(
          Collection<T> infos) {

    Map<String, Map<String, Tuple<AtomicInteger, Set<String>>>> groupBys = Maps.newHashMap();
    for (T info : infos) {
      String instanceId = info.getInstanceId();
      String group = InterestGroup.normalizeGroup(info.getGroup());
      
      Map<String, Tuple<AtomicInteger, Set<String>>> groupCount =
          groupBys.computeIfAbsent(instanceId, k -> Maps.newHashMap());
      Tuple<AtomicInteger, Set<String>> tuple =
          groupCount.computeIfAbsent(group, k -> Tuple.of(new AtomicInteger(0), Sets.newHashSetWithExpectedSize(16)));
      tuple.o1.incrementAndGet();
      tuple.o2.add(info.getDataInfoId());
    }
    
    Map<String, Map<String, Tuple<Integer, Integer>>> ret = Maps.newHashMap();
    for (Map.Entry<String, Map<String, Tuple<AtomicInteger, Set<String>>>> instances : groupBys.entrySet()) {
      final String instanceId = instances.getKey();
      Map<String, Tuple<Integer, Integer>> groupMap = ret.computeIfAbsent(instanceId, k -> Maps.newHashMap());
      for (Map.Entry<String, Tuple<AtomicInteger, Set<String>>> groups : instances.getValue().entrySet()) {
        final String group = groups.getKey();
        Tuple<AtomicInteger, Set<String>> tuple = groups.getValue();
        final int infoCount = tuple.o1.get();
        final int dataInfoIdCount = tuple.o2.size();
        groupMap.put(group, Tuple.of(infoCount, dataInfoIdCount));
      }
    }
    return ret;
  }
}
