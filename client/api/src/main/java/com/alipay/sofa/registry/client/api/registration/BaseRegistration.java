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
package com.alipay.sofa.registry.client.api.registration;

/**
 * Base registration.
 *
 * @author yeqing.yq
 * @version $Id : BaseRegistration.java, v 0.1 2018-09-04 11:36 yeqing.yq Exp $$
 */
public class BaseRegistration {

  protected String dataId;

  protected String group;

  protected String appName;

  protected String instanceId;

  protected String ip;

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

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "BaseRegistration{"
        + "dataId='"
        + dataId
        + "'"
        + ", group='"
        + group
        + "'"
        + ", appName='"
        + appName
        + "'"
        + ", instanceId='"
        + instanceId
        + "'"
        + ", ip='"
        + ip
        + "'"
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseRegistration that = (BaseRegistration) o;
    if (dataId != null ? !dataId.equals(that.dataId) : that.dataId != null) return false;
    if (group != null ? !group.equals(that.group) : that.group != null) return false;
    if (appName != null ? !appName.equals(that.appName) : that.appName != null) return false;
    if (instanceId != null ? !instanceId.equals(that.instanceId) : that.instanceId != null) return false;
    return ip != null ? ip.equals(that.ip) : that.ip == null;
  }

  @Override
  public int hashCode() {
    int result = dataId != null ? dataId.hashCode() : 0;
    result = 31 * result + (group != null ? group.hashCode() : 0);
    result = 31 * result + (appName != null ? appName.hashCode() : 0);
    result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
    result = 31 * result + (ip != null ? ip.hashCode() : 0);
    return result;
  }
}
