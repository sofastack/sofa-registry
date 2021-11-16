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

import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AppRevision implements Serializable {

  private long id;

  private String dataCenter;

  private String revision;

  private String appName;

  private String clientVersion;

  private Map<String, List<String>> baseParams = Maps.newHashMap();

  private Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();

  private boolean isDeleted;

  private Date lastHeartbeat;

  private int size;

  public AppRevision() {}

  public AppRevision(String dataCenter, String revision, Date lastHeartbeat) {
    this.dataCenter = dataCenter;
    this.revision = revision;
    this.lastHeartbeat = lastHeartbeat;
  }

  /**
   * Getter method for property <tt>id</tt>.
   *
   * @return property value of id
   */
  public long getId() {
    return id;
  }

  /**
   * Setter method for property <tt>id</tt>.
   *
   * @param id value to be assigned to property id
   */
  public void setId(long id) {
    this.id = id;
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
   * Setter method for property <tt>dataCenter</tt>.
   *
   * @param dataCenter value to be assigned to property dataCenter
   */
  public void setDataCenter(String dataCenter) {
    this.dataCenter = dataCenter;
  }

  /**
   * Getter method for property <tt>revision</tt>.
   *
   * @return property value of revision
   */
  public String getRevision() {
    return revision;
  }

  /**
   * Setter method for property <tt>revision</tt>.
   *
   * @param revision value to be assigned to property revision
   */
  public void setRevision(String revision) {
    this.revision = revision;
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
   * Getter method for property <tt>baseParams</tt>.
   *
   * @return property value of baseParams
   */
  public Map<String, List<String>> getBaseParams() {
    return baseParams;
  }

  /**
   * Setter method for property <tt>baseParams</tt>.
   *
   * @param baseParams value to be assigned to property baseParams
   */
  public void setBaseParams(Map<String, List<String>> baseParams) {
    this.baseParams = baseParams;
  }

  public Map<String, AppRevisionInterface> getInterfaceMap() {
    return interfaceMap;
  }

  public void setInterfaceMap(Map<String, AppRevisionInterface> interfaceMap) {
    this.interfaceMap = interfaceMap;
  }

  /**
   * Getter method for property <tt>lastHeartbeat</tt>.
   *
   * @return property value of lastHeartbeat
   */
  public Date getLastHeartbeat() {
    return lastHeartbeat;
  }

  /**
   * Setter method for property <tt>lastHeartbeat</tt>.
   *
   * @param lastHeartbeat value to be assigned to property lastHeartbeat
   */
  public void setLastHeartbeat(Date lastHeartbeat) {
    this.lastHeartbeat = lastHeartbeat;
  }

  /**
   * Getter method for property <tt>clientVersion</tt>.
   *
   * @return property value of clientVersion
   */
  public String getClientVersion() {
    return clientVersion;
  }

  /**
   * Setter method for property <tt>clientVersion</tt>.
   *
   * @param clientVersion value to be assigned to property clientVersion
   */
  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }

  /**
   * Getter method for property <tt>size</tt>.
   *
   * @return property value of size
   */
  public int getSize() {
    return size;
  }

  /**
   * Setter method for property <tt>size</tt>.
   *
   * @param size value to be assigned to property size
   */
  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public String toString() {
    return "AppRevision{"
        + "dataCenter='"
        + dataCenter
        + '\''
        + ", revision='"
        + revision
        + '\''
        + ", appName='"
        + appName
        + '\''
        + ", clientVersion='"
        + clientVersion
        + '\''
        + ", baseParams="
        + baseParams
        + ", interfaceMap="
        + interfaceMap
        + ", lastHeartbeat="
        + lastHeartbeat
        + '}';
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean deleted) {
    isDeleted = deleted;
  }
}
