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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

public final class DataUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataUtils.class);

    private DataUtils() {
    }

    public static <T extends BaseInfo> Map<String, Map<String, Map<String, Integer>>> countGroupBy(Collection<T> infos) {
        // instanceId/group/app - > count
        Map<String, Map<String, Map<String, Integer>>> counts = Maps.newHashMap();
        for (T info : infos) {
            Map<String, Map<String, Integer>> groupCount = counts.computeIfAbsent(
                    info.getInstanceId(), k -> Maps.newHashMap());
            Map<String, Integer> appCount = groupCount.computeIfAbsent(info.getGroup(),
                    k -> Maps.newHashMap());
            Integer count = appCount.getOrDefault(info.getAppName(), 0);
            appCount.put(info.getAppName(), count += 1);
        }
        return counts;
    }
}
