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
package com.alipay.sofa.registry.common.model.console;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author shangyu.wh
 * @version $Id: PersistenceData.java, v 0.1 2018-04-18 11:20 shangyu.wh Exp $
 */
public class PersistenceData implements Serializable {

  private String dataId;

  private String group;

  private String instanceId;

  private long version;

  private String data;

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * Getter method for property <tt>data</tt>.
   *
   * @return property value of data
   */
  public String getData() {
    return data;
  }

  /**
   * Setter method for property <tt>data</tt>.
   *
   * @param data value to be assigned to property data
   */
  public void setData(String data) {
    this.data = data;
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

  @Override
  public String toString() {
    return "PersistenceData{"
        + "dataId='"
        + dataId
        + '\''
        + ", group='"
        + group
        + '\''
        + ", instanceId='"
        + instanceId
        + '\''
        + ", version="
        + version
        + ", data='"
        + data
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PersistenceData that = (PersistenceData) o;
    return Objects.equals(dataId, that.dataId)
        && Objects.equals(group, that.group)
        && Objects.equals(instanceId, that.instanceId)
        && Objects.equals(version, that.version)
        && data.equals(that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataId, group, instanceId, version, data);
  }
}
