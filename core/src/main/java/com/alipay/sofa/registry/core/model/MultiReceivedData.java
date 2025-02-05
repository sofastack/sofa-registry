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
import java.util.List;
import java.util.Map;

/**
 * @author xiaojian.xj
 * @version : MultiReceivedData.java, v 0.1 2022年07月17日 16:18 xiaojian.xj Exp $
 */
public class MultiReceivedData implements Serializable {

  private static final long serialVersionUID = -1741750169930120188L;

  private String dataId;

  private String group;

  private String instanceId;

  private String scope;

  private List<String /*registId*/> subscriberRegistIds;

  private String localSegment;

  private String localZone;

  private Map<String, MultiSegmentData> multiData;

  public MultiReceivedData() {}

  /**
   * Getter method for property <tt>dataId</tt>.
   *
   * @return property value of dataId
   */
  public String getDataId() {
    return dataId;
  }

  /**
   * Setter method for property <tt>dataId</tt>.
   *
   * @param dataId value to be assigned to property dataId
   */
  public void setDataId(String dataId) {
    this.dataId = dataId;
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
   * Setter method for property <tt>group</tt>.
   *
   * @param group value to be assigned to property group
   */
  public void setGroup(String group) {
    this.group = group;
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
   * Setter method for property <tt>instanceId</tt>.
   *
   * @param instanceId value to be assigned to property instanceId
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Getter method for property <tt>scope</tt>.
   *
   * @return property value of scope
   */
  public String getScope() {
    return scope;
  }

  /**
   * Setter method for property <tt>scope</tt>.
   *
   * @param scope value to be assigned to property scope
   */
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * Getter method for property <tt>subscriberRegistIds</tt>.
   *
   * @return property value of subscriberRegistIds
   */
  public List<String> getSubscriberRegistIds() {
    return subscriberRegistIds;
  }

  /**
   * Setter method for property <tt>subscriberRegistIds</tt>.
   *
   * @param subscriberRegistIds value to be assigned to property subscriberRegistIds
   */
  public void setSubscriberRegistIds(List<String> subscriberRegistIds) {
    this.subscriberRegistIds = subscriberRegistIds;
  }

  /**
   * Getter method for property <tt>localSegment</tt>.
   *
   * @return property value of localSegment
   */
  public String getLocalSegment() {
    return localSegment;
  }

  /**
   * Setter method for property <tt>localSegment</tt>.
   *
   * @param localSegment value to be assigned to property localSegment
   */
  public void setLocalSegment(String localSegment) {
    this.localSegment = localSegment;
  }

  /**
   * Getter method for property <tt>localZone</tt>.
   *
   * @return property value of localZone
   */
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
   * Getter method for property <tt>multiData</tt>.
   *
   * @return property value of multiData
   */
  public Map<String, MultiSegmentData> getMultiData() {
    return multiData;
  }

  /**
   * Setter method for property <tt>multiData</tt>.
   *
   * @param multiData value to be assigned to property multiData
   */
  public void setMultiData(Map<String, MultiSegmentData> multiData) {
    this.multiData = multiData;
  }

  @Override
  public String toString() {
    return "MultiReceivedData{"
        + "dataId='"
        + dataId
        + '\''
        + ", group='"
        + group
        + '\''
        + ", instanceId='"
        + instanceId
        + '\''
        + ", scope='"
        + scope
        + '\''
        + ", subscriberRegistIds="
        + subscriberRegistIds
        + ", localSegment='"
        + localSegment
        + '\''
        + ", localZone='"
        + localZone
        + '\''
        + ", multiData="
        + multiData
        + '}';
  }
}
