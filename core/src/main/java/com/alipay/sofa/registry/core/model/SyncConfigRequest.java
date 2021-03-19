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
package com.alipay.sofa.registry.core.model;

import java.io.Serializable;

/**
 * @author zhuoyu.sjw
 * @version $Id: SyncConfigRequest.java, v 0.1 2018-03-14 23:06 zhuoyu.sjw Exp $$
 */
public class SyncConfigRequest implements Serializable {

  private static final long serialVersionUID = -843642420869816713L;

  private String dataCenter;

  private String zone;

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  /**
   * Setter method for property <tt>dataCenter</tt>.
   *
   * @param dataCenter value to be assigned to property dataCenter
   */
  public void setDataCenter(String dataCenter) {
    this.dataCenter = dataCenter;
  }

  /**
   * Getter method for property <tt>zone</tt>.
   *
   * @return property value of zone
   */
  public String getZone() {
    return zone;
  }

  /**
   * Setter method for property <tt>zone</tt>.
   *
   * @param zone value to be assigned to property zone
   */
  public void setZone(String zone) {
    this.zone = zone;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "SyncConfigRequest{"
        + "dataCenter='"
        + dataCenter
        + '\''
        + ", zone='"
        + zone
        + '\''
        + '}';
  }
}
