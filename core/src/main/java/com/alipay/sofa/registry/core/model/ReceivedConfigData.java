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

/**
 * The type Received config data.
 *
 * @author zhuoyu.sjw
 * @version $Id : ReceivedConfigData.java, v 0.1 2018-04-17 18:00 zhuoyu.sjw Exp $$
 */
public class ReceivedConfigData implements Serializable {

  private static final long serialVersionUID = 4077554672965455808L;

  private String dataId;

  private String group;

  private String instanceId;

  private List<String /*registId*/> configuratorRegistIds;

  private DataBox dataBox;

  private Long version;

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
   * Getter method for property <tt>dataBox</tt>.
   *
   * @return property value of dataBox
   */
  public DataBox getDataBox() {
    return dataBox;
  }

  /**
   * Setter method for property <tt>dataBox</tt>.
   *
   * @param dataBox value to be assigned to property dataBox
   */
  public void setDataBox(DataBox dataBox) {
    this.dataBox = dataBox;
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
   * Getter method for property <tt>configuratorRegistIds</tt>.
   *
   * @return property value of configuratorRegistIds
   */
  public List<String> getConfiguratorRegistIds() {
    return configuratorRegistIds;
  }

  /**
   * Setter method for property <tt>configuratorRegistIds</tt>.
   *
   * @param configuratorRegistIds value to be assigned to property configuratorRegistIds
   */
  public void setConfiguratorRegistIds(List<String> configuratorRegistIds) {
    this.configuratorRegistIds = configuratorRegistIds;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "ReceivedConfigData{"
        + "dataId='"
        + dataId
        + '\''
        + ", group='"
        + group
        + '\''
        + ", instanceId='"
        + instanceId
        + '\''
        + ", configuratorRegistIds="
        + configuratorRegistIds
        + ", dataBox="
        + dataBox
        + ", version="
        + version
        + '}';
  }
}
