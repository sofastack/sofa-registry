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
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.MultiSegmentData;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author xiaojian.xj
 * @version : LocalDataCenterPushData.java, v 0.1 2022年07月18日 18:57 xiaojian.xj Exp $
 */
public class LocalDataCenterPushData {

  private SegmentDataCounter localSegmentDatas;

  private Map<String, SegmentDataCounter> remoteSegmentDatas;

  public void from(
      Map<String, List<DataBox>> pushData,
      String localDataCenter,
      long pushVersion,
      Predicate<String> pushdataPredicate,
      Set<String> segmentZones) {

    SegmentDataCounter local =
        new SegmentDataCounter(new MultiSegmentData(localDataCenter, pushVersion));
    Map<String, SegmentDataCounter> remotes = Maps.newHashMap();

    for (String zone : segmentZones) {
      if (pushdataPredicate.test(zone)) {
        SegmentDataCounter counter =
            remotes.computeIfAbsent(
                zone, k -> new SegmentDataCounter(new MultiSegmentData(zone, pushVersion)));
        counter.put(zone, pushData.get(zone));
      } else {
        local.put(zone, pushData.get(zone));
      }
    }
    this.localSegmentDatas = local;
    this.remoteSegmentDatas = remotes;
    return;
  }

  /**
   * Getter method for property <tt>localSegmentDatas</tt>.
   *
   * @return property value of localSegmentDatas
   */
  public SegmentDataCounter getLocalSegmentDatas() {
    return localSegmentDatas;
  }

  /**
   * Getter method for property <tt>remoteSegmentDatas</tt>.
   *
   * @return property value of remoteSegmentDatas
   */
  public Map<String, SegmentDataCounter> getRemoteSegmentDatas() {
    return remoteSegmentDatas;
  }
}
