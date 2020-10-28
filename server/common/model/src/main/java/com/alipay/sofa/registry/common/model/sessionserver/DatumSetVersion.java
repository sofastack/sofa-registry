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
package com.alipay.sofa.registry.common.model.sessionserver;

import java.util.HashMap;
import java.util.Map;

public class DatumSetVersion {

    private Map<String, Long> versions;

    public DatumSetVersion() {
        versions = new HashMap<>();
    }

    public DatumSetVersion(String dataInfoId, Long version) {
        versions = new HashMap<>();
        addVersion(dataInfoId, version);
    }

    public void addVersion(String dataInfoId, Long version) {
        versions.put(dataInfoId, version);
    }

    public boolean higher(DatumSetVersion other) {
        if (!versions.keySet().equals(other.versions.keySet())) {
            return true;
        }
        for (Map.Entry<String, Long> entry : versions.entrySet()) {
            if (entry.getValue() > other.versions.get(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public boolean lower(String dataInfoId, Long version) {
        if (versions.containsKey(dataInfoId) && versions.get(dataInfoId) >= version) {
            return false;
        }
        return false;
    }

    public boolean isZero() {
        return versions.size() == 0;
    }
}
