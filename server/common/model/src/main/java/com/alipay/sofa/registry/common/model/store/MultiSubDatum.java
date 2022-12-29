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

import com.alipay.sofa.registry.cache.Sizer;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : MultiSubDatum.java, v 0.1 2022年06月18日 20:06 xiaojian.xj Exp $
 */
public class MultiSubDatum implements Serializable, Sizer {
  private static final long serialVersionUID = -5246148250587330736L;

  private final String dataInfoId;

  private final String dataId;

  private final String instanceId;

  private final String group;

  private final Map<String, SubDatum> datumMap;

  public MultiSubDatum(String dataInfoId, Map<String, SubDatum> datumMap) {
    DataInfo dataInfo = DataInfo.valueOf(dataInfoId);
    this.dataId = dataInfo.getDataId();
    this.instanceId = dataInfo.getInstanceId();
    this.group = dataInfo.getGroup();
    this.dataInfoId = dataInfoId;
    this.datumMap = datumMap;
  }

  public static MultiSubDatum intern(MultiSubDatum datum) {
    if (CollectionUtils.isEmpty(datum.getDatumMap())) {
      return datum;
    }
    Map<String, SubDatum> datumMap = Maps.newHashMapWithExpectedSize(datum.getDatumMap().size());
    for (Entry<String, SubDatum> entry : datum.datumMap.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }
      datumMap.put(entry.getKey(), SubDatum.intern(entry.getValue()));
    }
    return new MultiSubDatum(WordCache.getWordCache(datum.dataInfoId), datumMap);
  }

  public static MultiSubDatum of(SubDatum datum) {
    return new MultiSubDatum(
        datum.getDataInfoId(),
        Collections.singletonMap(datum.getDataCenter(), SubDatum.intern(datum)));
  }

  @Override
  public int size() {
    return CollectionUtils.isEmpty(datumMap)
        ? 0
        : datumMap.values().stream().mapToInt(SubDatum::size).sum();
  }

  /**
   * Getter method for property <tt>datumMap</tt>.
   *
   * @return property value of datumMap
   */
  public Map<String, SubDatum> getDatumMap() {
    return datumMap;
  }

  public Set<String> dataCenters() {
    if (CollectionUtils.isEmpty(datumMap)) {
      return Collections.emptySet();
    }
    return datumMap.keySet();
  }

  public SubDatum getSubDatum(String dataCenter) {
    if (CollectionUtils.isEmpty(datumMap)) {
      return null;
    }
    return datumMap.get(dataCenter);
  }

  public long getVersion(String dataCenter) {
    if (CollectionUtils.isEmpty(datumMap)) {
      return Long.MIN_VALUE;
    }
    SubDatum subDatum = datumMap.get(dataCenter);
    if (subDatum == null) {
      return Long.MIN_VALUE;
    }
    return subDatum.getVersion();
  }

  public int getPubNum() {
    return CollectionUtils.isEmpty(datumMap)
        ? 0
        : datumMap.values().stream().mapToInt(SubDatum::getPubNum).sum();
  }

  public int getDataBoxBytes() {
    return CollectionUtils.isEmpty(datumMap)
        ? 0
        : datumMap.values().stream().mapToInt(SubDatum::getDataBoxBytes).sum();
  }

  public Map<String, Long> getVersion() {
    if (CollectionUtils.isEmpty(datumMap)) {
      return Collections.EMPTY_MAP;
    }
    Map<String, Long> ret = Maps.newHashMapWithExpectedSize(datumMap.size());
    for (Entry<String, SubDatum> entry : datumMap.entrySet()) {
      ret.put(entry.getKey(), entry.getValue().getVersion());
    }
    return ret;
  }

  /**
   * Getter method for property <tt>dataId</tt>.
   *
   * @return property value of dataId
   */
  public String getDataId() {
    return dataId;
  }

  /**
   * Getter method for property <tt>instanceId</tt>.
   *
   * @return property value of instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Getter method for property <tt>group</tt>.
   *
   * @return property value of group
   */
  public String getGroup() {
    return group;
  }

  /**
   * Getter method for property <tt>dataInfoId</tt>.
   *
   * @return property value of dataInfoId
   */
  public String getDataInfoId() {
    return dataInfoId;
  }

  public void mustUnzipped() {
    for (SubDatum value : datumMap.values()) {
      value.mustUnzipped();
    }
  }

  public List<Long> getRecentVersions(String dataCenter) {
    SubDatum subDatum = datumMap.get(dataCenter);
    if (subDatum == null) {
      return Collections.emptyList();
    }
    return subDatum.getRecentVersions();
  }

  public void putDatum(String dataCenter, SubDatum subDatum) {
    datumMap.put(dataCenter, subDatum);
  }
}
