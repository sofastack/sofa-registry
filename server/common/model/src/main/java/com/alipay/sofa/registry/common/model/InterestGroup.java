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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;

public class InterestGroup {

  private static final Set<String> interestGroups =
      Sets.newConcurrentHashSet(Lists.newArrayList(ValueConstants.DEFAULT_GROUP));

  public static void registerInterestGroup(String... groups) {
    interestGroups.addAll(Arrays.asList(groups));
  }

  public static String normalizeGroup(String group) {
    if (group != null && interestGroups.contains(group)) {
      return group;
    }
    return "other";
  }
}
