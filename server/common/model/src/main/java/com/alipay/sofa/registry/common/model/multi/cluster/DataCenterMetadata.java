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
package com.alipay.sofa.registry.common.model.multi.cluster;

import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : DataCenterMetadata.java, v 0.1 2022年07月21日 11:54 xiaojian.xj Exp $
 */
public class DataCenterMetadata implements Serializable {
  private static final long serialVersionUID = 5438256870784168278L;

  private final String dataCenter;

  private final Set<String> zones;

  public DataCenterMetadata(String dataCenter, Set<String> zones) {
    this.dataCenter = dataCenter;
    this.zones = zones;
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  /**
   * Getter method for property <tt>zones</tt>.
   *
   * @return property value of zones
   */
  public Set<String> getZones() {
    return zones;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataCenterMetadata that = (DataCenterMetadata) o;
    return Objects.equal(dataCenter, that.dataCenter) && Objects.equal(zones, that.zones);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dataCenter, zones);
  }

  @Override
  public String toString() {
    return "DataCenterMetadata{" + "dataCenter='" + dataCenter + '\'' + ", zones=" + zones + '}';
  }
}
