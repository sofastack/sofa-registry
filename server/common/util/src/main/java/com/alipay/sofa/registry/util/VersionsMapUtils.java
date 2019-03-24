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

import java.util.Map;

/**
 * @author xuanbei
 * @since 2019/2/12
 */
public class VersionsMapUtils {
    public static boolean checkAndUpdateVersions(Map<String, Long> versionsMap, String versionKey,
                                                 Long version) {
        while (true) {
            Long oldValue = versionsMap.get(versionKey);
            if (oldValue == null) {
                // Add firstly
                if (versionsMap.putIfAbsent(versionKey, version) == null) {
                    return true;
                }
            } else {
                if (version > oldValue) {
                    if (versionsMap.replace(versionKey, oldValue, version)) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
    }
}
