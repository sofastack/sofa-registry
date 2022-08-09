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
package com.alipay.sofa.registry.compress;

import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.StringUtils;
import java.util.*;

public class CompressPushKey implements CompressKey {
  private final String encode;
  private final String dataId;
  private final String instanceId;
  private final String group;
  private final String segment;
  private final long version;
  private final ZoneCount[] zoneCounts;

  private final int byteSize;

  private CompressPushKey(
      String encode,
      String dataId,
      String instanceId,
      String group,
      String segment,
      long version,
      ZoneCount[] zoneCounts) {
    this.encode = encode;
    this.dataId = dataId;
    this.instanceId = instanceId;
    this.group = group;
    this.segment = segment;
    this.version = version;
    this.zoneCounts = zoneCounts;
    this.byteSize = calcSize();
  }

  public static CompressPushKey of(
      String segment,
      String dataId,
      String instanceId,
      String group,
      long version,
      Map<String, List<DataBox>> data,
      String encode) {
    List<Map.Entry<String, List<DataBox>>> zoneData = new ArrayList<>(data.entrySet());
    zoneData.sort(Map.Entry.comparingByKey());
    ZoneCount[] zoneCounts = new ZoneCount[zoneData.size()];
    for (int i = 0; i < zoneData.size(); i++) {
      Map.Entry<String, List<DataBox>> entry = zoneData.get(i);
      zoneCounts[i] = new ZoneCount(entry.getKey(), entry.getValue().size());
    }
    return new CompressPushKey(encode, dataId, instanceId, group, segment, version, zoneCounts);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompressPushKey that = (CompressPushKey) o;
    return version == that.version
        && Objects.equals(encode, that.encode)
        && Objects.equals(dataId, that.dataId)
        && Objects.equals(instanceId, that.instanceId)
        && Objects.equals(group, that.group)
        && Objects.equals(segment, that.segment)
        && Arrays.equals(zoneCounts, that.zoneCounts);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(encode, dataId, instanceId, group, segment, version);
    result = 31 * result + Arrays.hashCode(zoneCounts);
    return result;
  }

  private int calcSize() {
    int bytes = 8;
    bytes += StringUtils.sizeof(encode);
    bytes += StringUtils.sizeof(dataId);
    bytes += StringUtils.sizeof(instanceId);
    bytes += StringUtils.sizeof(group);
    bytes += StringUtils.sizeof(segment);
    for (ZoneCount zoneCount : zoneCounts) {
      bytes += zoneCount.size();
    }
    return bytes;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "dataId={}, group={}, instanceId={}, ver={}, encode={}",
        dataId,
        group,
        instanceId,
        version,
        encode);
  }

  @Override
  public int size() {
    return byteSize;
  }

  private static class ZoneCount {
    private final String zone;
    private final int count;

    private ZoneCount(String zone, int count) {
      this.zone = zone;
      this.count = count;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ZoneCount zoneCount = (ZoneCount) o;
      return count == zoneCount.count && Objects.equals(zone, zoneCount.zone);
    }

    @Override
    public int hashCode() {
      return Objects.hash(zone, count);
    }

    public int size() {
      return StringUtils.sizeof(zone) + 4;
    }
  }
}
