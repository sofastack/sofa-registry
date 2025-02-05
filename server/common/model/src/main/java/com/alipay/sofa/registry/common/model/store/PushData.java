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

import com.alipay.sofa.registry.common.model.DataCenterPushInfo;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.util.StringUtils;

public class PushData<T> {
  private final T payload;

  private final Map<String, String> segment2DataCenter;

  private final Map<String, DataCenterPushInfo> dataCenterPushInfo;

  public PushData(T payload, Map<String, DataCenterPushInfo> dataCenterPushInfo) {
    this.payload = payload;

    if (dataCenterPushInfo == null) {
      this.dataCenterPushInfo = Collections.EMPTY_MAP;
      this.segment2DataCenter = Collections.EMPTY_MAP;
      return;
    }
    this.dataCenterPushInfo = dataCenterPushInfo;

    Map<String, String> segment2DataCenter = Maps.newHashMap();
    for (Entry<String, DataCenterPushInfo> entry : dataCenterPushInfo.entrySet()) {
      for (String segment : entry.getValue().getSegments()) {
        segment2DataCenter.put(segment, entry.getKey());
      }
    }
    this.segment2DataCenter = segment2DataCenter;
  }

  public T getPayload() {
    return payload;
  }

  /**
   * Getter method for property <tt>dataCenterPushInfo</tt>.
   *
   * @return property value of dataCenterPushInfo
   */
  public Map<String, DataCenterPushInfo> getDataCenterPushInfo() {
    return dataCenterPushInfo;
  }

  public void addSegmentInfo(String segment, String encoding, int encodeSize) {
    String dataCenter = segment2DataCenter.get(segment);
    if (StringUtils.isEmpty(dataCenter)) {
      throw new SofaRegistryRuntimeException(
          StringFormatter.format("[addSegmentInfo]not find dataCenter for segment: %s", segment));
    }
    DataCenterPushInfo dataCenterPushInfo = this.dataCenterPushInfo.get(dataCenter);
    if (dataCenterPushInfo == null) {
      throw new SofaRegistryRuntimeException(
          StringFormatter.format(
              "[addSegmentInfo]not find dataCenterPushInfo for dataCenter:%s, segment: %s",
              dataCenter, segment));
    }
    dataCenterPushInfo.addSegmentInfo(segment, encoding, encodeSize);
  }
}
