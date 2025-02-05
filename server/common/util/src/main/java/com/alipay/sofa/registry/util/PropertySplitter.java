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

import com.google.common.base.Splitter;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

/**
 * @author shangyu.wh
 * @version $Id: PropertySplitter.java, v 0.1 2018-05-03 16:29 shangyu.wh Exp $
 */
public class PropertySplitter {
  private static final PropertySplitter instance = new PropertySplitter();

  public static PropertySplitter getInstance() {
    return instance;
  }

  /** Example: one.example.property = KEY1:VALUE1,KEY2:VALUE2 */
  Map<String, String> map(String property) {
    if (property == null) {
      return new HashMap<>();
    }
    return this.map(property, ",");
  }

  /**
   * Example: property=KEY1:VALUE1.1,VALUE1.2|KEY2:VALUE2.1,VALUE2.2, defaultKey=ignored Example:
   * property = KEY1:VALUE1.1,VALUE1.2, defaultKey=KEY2 Example: property = VALUE1.1,VALUE1.2,
   * defaultKey=KEY2
   *
   * @param defaultKey defaultKey
   * @param property property
   * @return Map Map
   */
  public Map<String, Collection<String>> mapOfKeyList(String defaultKey, String property) {
    if (StringUtils.isBlank(property)) {
      return Collections.emptyMap();
    }
    if (StringUtils.contains(property, '|')) {
      return mapOfList(property);
    }
    if (StringUtils.contains(property, ':')) {
      Map<String, Collection<String>> singleMap = mapOfList(property);
      return Collections.singletonMap(defaultKey, singleMap.values().stream().findFirst().get());
    }
    return Collections.singletonMap(defaultKey, list(property));
  }

  /** Example: one.example.property = KEY1:VALUE1.1,VALUE1.2|KEY2:VALUE2.1,VALUE2.2 */
  Map<String, Collection<String>> mapOfList(String property) {
    if (property == null) {
      return new HashMap<>();
    }
    Map<String, String> map = this.map(property, "|");

    Map<String, Collection<String>> mapOfList = new HashMap<>();
    for (Entry<String, String> entry : map.entrySet()) {
      mapOfList.put(entry.getKey(), this.list(entry.getValue()));
    }

    return mapOfList;
  }

  /**
   * Example: one.example.property = VALUE1,VALUE2,VALUE3,VALUE4
   *
   * @param property property
   * @return Collection Collection
   */
  public Collection<String> list(String property) {
    if (property == null) {
      return new ArrayList<>();
    }
    return this.list(property, ",");
  }

  /**
   * Example: one.example.property = VALUE1.1,VALUE1.2|VALUE2.1,VALUE2.2
   *
   * @param property property
   * @return Collection Collection
   */
  public Collection<Collection<String>> groupedList(String property) {
    if (property == null) {
      return new ArrayList<>();
    }
    Collection<String> unGroupedList = this.list(property, "|");

    Collection<Collection<String>> groupedList = new ArrayList<>();
    for (String group : unGroupedList) {
      groupedList.add(this.list(group));
    }

    return groupedList;
  }

  private Collection<String> list(String property, String splitter) {
    return Splitter.on(splitter).omitEmptyStrings().trimResults().splitToList(property);
  }

  private Map<String, String> map(String property, String splitter) {
    return Splitter.on(splitter)
        .omitEmptyStrings()
        .trimResults()
        .withKeyValueSeparator(":")
        .split(property);
  }
}
