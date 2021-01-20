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

import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.*;

/**
 * request to notify session server when data changed
 *
 * @author qian.lqlq
 * @version $Id: DataChangeRequest.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class DataChangeRequest implements Serializable {

    private static final long               serialVersionUID = -7674982522990222894L;

    private final Map<String, DatumVersion> dataInfoIds;

    private final String                    dataCenter;

    private final Set<String>               revisions;

    public DataChangeRequest(String dataCenter, Map<String, DatumVersion> dataInfoIds,
                             Set<String> revisions) {
        this.dataCenter = dataCenter;
        this.dataInfoIds = dataInfoIds;
        this.revisions = revisions;
    }

    public Map<String, DatumVersion> getDataInfoIds() {
        return Collections.unmodifiableMap(dataInfoIds);
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public Set<String> getRevisions() {
        return Collections.unmodifiableSet(revisions);
    }

    @Override
    public String toString() {
        return "DataChangeRequest{" + "dataInfoIds=" + dataInfoIds.size() + ", dataCenter='"
               + dataCenter + '\'' + ", revisions=" + revisions.size() + '}';
    }
}
