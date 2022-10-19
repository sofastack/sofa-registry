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
package com.alipay.sofa.registry.common.model.dataserver;

import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.slot.filter.SyncAcceptorRequest;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 14:27 yuzhi.lyz Exp $
 */
public class DatumSummary implements Serializable {
  private final String dataInfoId;
  private final Map<String /*registerId*/, RegisterVersion> publisherVersions;

  public DatumSummary(
      String dataInfoId, Map<String /*registerId*/, RegisterVersion> publisherVersions) {
    this.dataInfoId = dataInfoId;
    this.publisherVersions = publisherVersions;
  }

  public DatumSummary(String dataInfoId) {
    this.dataInfoId = dataInfoId;
    this.publisherVersions = new HashMap<>();
  }

  public static DatumSummary of(
      String dataInfoId,
      Map<String, Publisher> publisherMap,
      SyncSlotAcceptorManager acceptorManager) {
    Map<String, RegisterVersion> versionMap = Maps.newHashMapWithExpectedSize(publisherMap.size());
    for (Map.Entry<String, Publisher> e : publisherMap.entrySet()) {
      // filter publisher
      if (!acceptorManager.accept(
          SyncAcceptorRequest.buildRequest(dataInfoId, e.getValue().getPublishSource()))) {
        continue;
      }

      versionMap.put(e.getKey(), e.getValue().registerVersion());
    }
    return new DatumSummary(dataInfoId, versionMap);
  }

  /**
   * Getter method for property <tt>dataInfoId</tt>.
   *
   * @return property value of dataInfoId
   */
  public String getDataInfoId() {
    return dataInfoId;
  }

  /**
   * Getter method for property <tt>publisherVersions</tt>.
   *
   * @return property value of publisherVersions
   */
  public Map<String, RegisterVersion> getPublisherVersions() {
    return publisherVersions;
  }

  public Map<String, RegisterVersion> getPublisherVersions(Collection<String> registerIds) {
    Map<String, RegisterVersion> m = Maps.newHashMapWithExpectedSize(registerIds.size());
    for (String registerId : registerIds) {
      RegisterVersion v = publisherVersions.get(registerId);
      if (v == null) {
        throw new IllegalArgumentException("not contains registerId:" + registerId);
      }
      m.put(registerId, v);
    }
    return m;
  }

  public boolean isEmpty() {
    return publisherVersions.isEmpty();
  }

  public int size() {
    return publisherVersions.size();
  }

  public static int countPublisherSize(Collection<DatumSummary> summaries) {
    int count = 0;
    for (DatumSummary summary : summaries) {
      count += summary.size();
    }
    return count;
  }

  @Override
  public String toString() {
    return String.format("Summary={%s=%d}", dataInfoId, size());
  }
}
