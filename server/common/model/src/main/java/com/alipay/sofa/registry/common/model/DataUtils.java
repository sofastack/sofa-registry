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
    Map<String, Map<String, Map<String, List<T>>>> groupBys = Maps.newHashMap();
    for (T info : infos) {
      Map<String, Map<String, List<T>>> groupCount =
          groupBys.computeIfAbsent(info.getInstanceId(), k -> Maps.newHashMap());
      Map<String, List<T>> appCount =
          groupCount.computeIfAbsent(
              InterestGroup.normalizeGroup(info.getGroup()), k -> Maps.newHashMap());
      String appName = info.getAppName();
      if (StringUtils.isBlank(appName)) {
        appName = "";
      }
      List<T> list = appCount.computeIfAbsent(appName, k -> Lists.newLinkedList());
      list.add(info);
    }
    Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> ret = Maps.newHashMap();
    for (Map.Entry<String, Map<String, Map<String, List<T>>>> count : groupBys.entrySet()) {
      final String instanceId = count.getKey();
      Map<String, Map<String, Tuple<Integer, Integer>>> instanceCount =
          ret.computeIfAbsent(instanceId, k -> Maps.newHashMap());
      for (Map.Entry<String, Map<String, List<T>>> e : count.getValue().entrySet()) {
        final String group = e.getKey();
        Map<String, Tuple<Integer, Integer>> groupCount =
            instanceCount.computeIfAbsent(group, k -> Maps.newHashMap());
        for (Map.Entry<String, List<T>> apps : e.getValue().entrySet()) {
          List<T> list = apps.getValue();
          final int infoCount = list.size();
          Set<String> dataInfoIds = Sets.newHashSetWithExpectedSize(64);
          for (T t : list) {
            dataInfoIds.add(t.getDataInfoId());
          }
          final int dataInfoIdCount = dataInfoIds.size();
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

    Map<String, Map<String, List<T>>> groupBys = Maps.newHashMap();
    for (T info : infos) {
      Map<String, List<T>> groupCount =
          groupBys.computeIfAbsent(info.getInstanceId(), k -> Maps.newHashMap());
      List<T> infoList =
          groupCount.computeIfAbsent(
              InterestGroup.normalizeGroup(info.getGroup()), k -> Lists.newLinkedList());
      infoList.add(info);
    }
    Map<String, Map<String, Tuple<Integer, Integer>>> ret = Maps.newHashMap();
    for (Map.Entry<String, Map<String, List<T>>> instances : groupBys.entrySet()) {
      final String instanceId = instances.getKey();
      for (Map.Entry<String, List<T>> groups : instances.getValue().entrySet()) {
        final String group = groups.getKey();
        List<T> list = groups.getValue();
        final int infoCount = list.size();
        Set<String> dataInfoIds = Sets.newHashSetWithExpectedSize(256);
        for (T t : list) {
          dataInfoIds.add(t.getDataInfoId());
        }
        final int dataInfoIdCount = dataInfoIds.size();
        Map<String, Tuple<Integer, Integer>> groupCount =
            ret.computeIfAbsent(instanceId, k -> Maps.newHashMap());
        groupCount.put(group, Tuple.of(infoCount, dataInfoIdCount));
      }
    }
    return ret;
  }
}
