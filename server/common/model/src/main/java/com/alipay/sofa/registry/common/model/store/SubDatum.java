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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.StringUtils;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class SubDatum implements Serializable, Sizer {
  private static final long serialVersionUID = 5307489721610438103L;

  private final String dataInfoId;

  private final String dataCenter;

  private final String dataId;

  private final String instanceId;

  private final String group;

  private final List<SubPublisher> publishers;

  private final long version;

  public SubDatum(
      String dataInfoId,
      String dataCenter,
      long version,
      Collection<SubPublisher> publishers,
      String dataId,
      String instanceId,
      String group) {
    this.dataInfoId = dataInfoId;
    this.dataCenter = dataCenter;
    this.version = version;
    this.publishers = Collections.unmodifiableList(Lists.newArrayList(publishers));
    this.dataId = dataId;
    this.instanceId = instanceId;
    this.group = group;
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public String getDataCenter() {
    return dataCenter;
  }

  public String getDataId() {
    return dataId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getGroup() {
    return group;
  }

  public List<SubPublisher> getPublishers() {
    return publishers;
  }

  public long getVersion() {
    return version;
  }

  public boolean isEmpty() {
    return publishers.isEmpty();
  }

  public int getDataBoxBytes() {
    int bytes = 0;
    for (SubPublisher p : publishers) {
      bytes += p.getDataBoxBytes();
    }
    return bytes;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "SubDatum{{},{},ver={},num={},bytes={}}",
        dataInfoId,
        dataCenter,
        version,
        publishers.size(),
        getDataBoxBytes());
  }

  public static SubDatum intern(SubDatum datum) {
    if (datum == null) {
      return null;
    }
    final String dataInfoId = WordCache.getWordCache(datum.dataInfoId);
    final String dataCenter = WordCache.getWordCache(datum.dataCenter);

    final String dataId = WordCache.getWordCache(datum.dataId);
    final String instanceId = WordCache.getWordCache(datum.instanceId);
    final String group = WordCache.getWordCache(datum.group);

    List<SubPublisher> publishers = Lists.newArrayListWithCapacity(datum.publishers.size());
    for (SubPublisher publisher : datum.publishers) {
      final String cell = WordCache.getWordCache(publisher.getCell());
      publishers.add(
          new SubPublisher(
              publisher.getRegisterId(),
              cell,
              publisher.getDataList(),
              publisher.getClientId(),
              publisher.getVersion(),
              publisher.getSrcAddressString(),
              publisher.getRegisterTimestamp(),
              publisher.getPublishSource()));
    }
    return new SubDatum(
        dataInfoId, dataCenter, datum.version, publishers, dataId, instanceId, group);
  }

  public int size() {
    int size =
        StringUtils.sizeof(dataInfoId)
            + StringUtils.sizeof(dataCenter)
            + StringUtils.sizeof(dataId)
            + StringUtils.sizeof(instanceId)
            + 20;
    if (publishers != null) {
      for (SubPublisher pub : publishers) {
        size += pub.size();
      }
    }
    return size;
  }
}
