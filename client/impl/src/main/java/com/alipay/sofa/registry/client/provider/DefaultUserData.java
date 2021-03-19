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
package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.api.model.UserData;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhuoyu.sjw
 * @version $Id: DefaultUserData.java, v 0.1 2017-11-30 21:01 zhuoyu.sjw Exp $$
 */
public class DefaultUserData implements UserData {

  /** zone, List */
  private Map<String, List<String>> zoneData = new ConcurrentHashMap<String, List<String>>();

  /** The current client`s zone */
  private String localZone;

  /**
   * Getter method for property <tt>zoneData</tt>.
   *
   * @return property value of zoneData
   */
  @Override
  public Map<String, List<String>> getZoneData() {
    return zoneData;
  }

  /**
   * Setter method for property <tt>zoneData</tt>.
   *
   * @param zoneData value to be assigned to property zoneData
   */
  public void setZoneData(Map<String, List<String>> zoneData) {
    this.zoneData = zoneData;
  }

  /**
   * Getter method for property <tt>localZone</tt>.
   *
   * @return property value of localZone
   */
  @Override
  public String getLocalZone() {
    return localZone;
  }

  /**
   * Setter method for property <tt>localZone</tt>.
   *
   * @param localZone value to be assigned to property localZone
   */
  public void setLocalZone(String localZone) {
    this.localZone = localZone;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "DefaultUserData{" + "zoneData=" + zoneData + ", localZone='" + localZone + '\'' + '}';
  }
}
