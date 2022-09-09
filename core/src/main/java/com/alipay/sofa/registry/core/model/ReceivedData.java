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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Received data multi.
 *
 * @author zhuoyu.sjw
 * @version $Id : ReceivedData.java, v 0.1 2017-11-23 22:23 zhuoyu.sjw Exp $$
 */
public class ReceivedData implements Serializable {

  private static final long serialVersionUID = -7322781873212812819L;

  private String dataId;

  private String group;

  private String instanceId;

  private String segment;

  private String scope;

  private List<String /*registId*/> subscriberRegistIds;

  private Map<String /*zone*/, List<DataBox>> data;

  private Long version;

  private String localZone;

  private Map<String, Integer> dataCount = new HashMap<String, Integer>();

  /** Instantiates a new Received data multi. */
  public ReceivedData() {}

  /**
   * Instantiates a new Received data multi.
   *
   * @param dataId the data id
   * @param group the group
   * @param instanceId the instance id
   * @param segment the data center
   * @param subscriberRegistIds the subscriber regist ids
   * @param data the data
   * @param version the version
   */
  public ReceivedData(
      String dataId,
      String group,
      String instanceId,
      String segment,
      List<String> subscriberRegistIds,
      Map<String, List<DataBox>> data,
      Long version) {
    this.dataId = dataId;
    this.group = group;
    this.instanceId = instanceId;
    this.segment = segment;
    this.subscriberRegistIds = subscriberRegistIds;
    this.data = data;
    this.version = version;
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
   * Getter method for property <tt>segment</tt>.
   *
   * @return property value of segment
   */
  public String getSegment() {
    return segment;
  }

  /**
   * Setter method for property <tt>segment</tt>.
   *
   * @param segment value to be assigned to property segment
   */
  public void setSegment(String segment) {
    this.segment = segment;
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
   * Getter method for property <tt>data</tt>.
   *
   * @return property value of data
   */
  public Map<String, List<DataBox>> getData() {
    return data;
  }

  /**
   * Setter method for property <tt>data</tt>.
   *
   * @param data value to be assigned to property data
   */
  public void setData(Map<String, List<DataBox>> data) {
    this.data = data;
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public Long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(Long version) {
    this.version = version;
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
   * Getter method for property <tt>dataCount</tt>.
   *
   * @return property value of dataCount
   */
  public Map<String, Integer> getDataCount() {
    return dataCount;
  }

  /**
   * Setter method for property <tt>dataCount</tt>.
   *
   * @param dataCount value to be assigned to property dataCount
   */
  public void setDataCount(Map<String, Integer> dataCount) {
    this.dataCount = dataCount;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "ReceivedData{"
        + "dataId='"
        + dataId
        + '\''
        + ", group='"
        + group
        + '\''
        + ", instanceId='"
        + instanceId
        + '\''
        + ", segment='"
        + segment
        + '\''
        + ", scope='"
        + scope
        + '\''
        + ", subscriberRegistIds="
        + subscriberRegistIds
        + ", data="
        + data
        + ", version="
        + version
        + ", localZone='"
        + localZone
        + '\''
        + ", dataCount="
        + dataCount
        + '}';
  }
}
