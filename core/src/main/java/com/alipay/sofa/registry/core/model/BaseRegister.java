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
import java.util.Map;

/**
 * @author zhuoyu.sjw
 * @version $Id: BaseRegister.java, v 0.1 2017-11-28 14:32 zhuoyu.sjw Exp $$
 */
public class BaseRegister implements Serializable {

  private static final long serialVersionUID = -3825175839851738346L;

  private String instanceId;

  private String zone;

  private String appName;

  private String dataId;

  private String group;

  private String processId;

  private String registId;

  private String clientId;

  private String dataInfoId;

  private String ip;

  private Integer port;

  private String eventType;

  private Long version;

  private Long timestamp;

  private volatile Map<String, String> attributes;

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
   * Getter method for property <tt>appName</tt>.
   *
   * @return property value of appName
   */
  public String getAppName() {
    return appName;
  }

  /**
   * Setter method for property <tt>appName</tt>.
   *
   * @param appName value to be assigned to property appName
   */
  public void setAppName(String appName) {
    this.appName = appName;
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
   * Getter method for property <tt>processId</tt>.
   *
   * @return property value of processId
   */
  public String getProcessId() {
    return processId;
  }

  /**
   * Setter method for property <tt>processId</tt>.
   *
   * @param processId value to be assigned to property processId
   */
  public void setProcessId(String processId) {
    this.processId = processId;
  }

  /**
   * Getter method for property <tt>registId</tt>.
   *
   * @return property value of registId
   */
  public String getRegistId() {
    return registId;
  }

  /**
   * Setter method for property <tt>registId</tt>.
   *
   * @param registId value to be assigned to property registId
   */
  public void setRegistId(String registId) {
    this.registId = registId;
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
   * Getter method for property <tt>timestamp</tt>.
   *
   * @return property value of timestamp
   */
  public Long getTimestamp() {
    return timestamp;
  }

  /**
   * Setter method for property <tt>timestamp</tt>.
   *
   * @param timestamp value to be assigned to property timestamp
   */
  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Getter method for property <tt>eventType</tt>.
   *
   * @return property value of eventType
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * Setter method for property <tt>eventType</tt>.
   *
   * @param eventType value to be assigned to property eventType
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  /**
   * Getter method for property <tt>clientId</tt>.
   *
   * @return property value of clientId
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Setter method for property <tt>clientId</tt>.
   *
   * @param clientId value to be assigned to property clientId
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Getter method for property <tt>ip</tt>.
   *
   * @return property value of ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * Setter method for property <tt>ip</tt>.
   *
   * @param ip value to be assigned to property ip
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Getter method for property <tt>port</tt>.
   *
   * @return property value of port
   */
  public Integer getPort() {
    return port;
  }

  /**
   * Setter method for property <tt>port</tt>.
   *
   * @param port value to be assigned to property port
   */
  public void setPort(Integer port) {
    this.port = port;
  }

  /**
   * Getter method for property <tt>dataInfoId</tt>.
   *
   * @return property value of dataInfoId
   */
  public String getDataInfoId() {
    return dataInfoId;
  }

  /**
   * Setter method for property <tt>dataInfoId</tt>.
   *
   * @param dataInfoId value to be assigned to property dataInfoId
   */
  public void setDataInfoId(String dataInfoId) {
    this.dataInfoId = dataInfoId;
  }

  /**
   * Getter method for property <tt>attributes</tt>.
   *
   * @return property value of attributes
   */
  public synchronized Map<String, String> getAttributes() {
    if (this.attributes == null) {
      this.attributes = new HashMap<String, String>();
    }
    return attributes;
  }

  /**
   * Setter method for property <tt>attributes</tt>.
   *
   * @param attributes value to be assigned to property attributes
   */
  public synchronized void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "BaseRegister{"
        + "instanceId='"
        + instanceId
        + '\''
        + ", zone='"
        + zone
        + '\''
        + ", appName='"
        + appName
        + '\''
        + ", dataId='"
        + dataId
        + '\''
        + ", group='"
        + group
        + '\''
        + ", processId='"
        + processId
        + '\''
        + ", registId='"
        + registId
        + '\''
        + ", clientId='"
        + clientId
        + '\''
        + ", dataInfoId='"
        + dataInfoId
        + '\''
        + ", ip='"
        + ip
        + '\''
        + ", port="
        + port
        + ", eventType='"
        + eventType
        + '\''
        + ", version="
        + version
        + ", timestamp="
        + timestamp
        + ", attributes="
        + attributes
        + '}';
  }
}
