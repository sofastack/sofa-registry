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
package com.alipay.sofa.registry.server.data.change;

import com.alipay.sofa.registry.common.model.TraceTimes;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;

public class DataChangeMerger {
  private final Set<String> dataInfoIds = Sets.newConcurrentHashSet();
  private volatile DataChangeType lastDataChangeType;
  private long firstTs;

  public DataChangeMerger() {}

  public void addChanges(Collection<String> dataInfoIds, DataChangeType dataChangeType) {
    long now = System.currentTimeMillis();
    this.dataInfoIds.addAll(dataInfoIds);
    this.lastDataChangeType = dataChangeType;
    if (firstTs == 0) {
      firstTs = now;
    }
  }

  public void clear() {
    this.dataInfoIds.clear();
    this.firstTs = 0;
  }

  public TraceTimes createTraceTime() {
    TraceTimes times = new TraceTimes();
    times.setDataChangeType(lastDataChangeType.ordinal());
    times.setFirstDataChange(firstTs);
    return times;
  }

  public Set<String> getDataInfoIds() {
    return dataInfoIds;
  }
}
