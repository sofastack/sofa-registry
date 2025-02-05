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
package com.alipay.sofa.registry.jdbc.domain;

import com.alipay.sofa.registry.jdbc.version.config.ConfigEntry;
import java.util.Date;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataDomain.java, v 0.1 2021年03月13日 19:29 xiaojian.xj Exp $
 */
public class ProvideDataDomain implements ConfigEntry {

  /** primary dataKey */
  private long id;

  /** local data center */
  private String dataCenter;

  /** data dataKey */
  private String dataKey;

  /** data dataValue */
  private String dataValue;

  /** create time */
  private Date gmtCreate;

  /** last update time */
  private Date gmtModified;

  /** data version */
  private long dataVersion;

  public ProvideDataDomain() {}

  public ProvideDataDomain(String dataCenter, String dataKey, String dataValue, long dataVersion) {
    this.dataCenter = dataCenter;
    this.dataKey = dataKey;
    this.dataValue = dataValue;
    this.dataVersion = dataVersion;
  }

  /**
   * Getter method for property <tt>id</tt>.
   *
   * @return property dataValue of id
   */
  public long getId() {
    return id;
  }

  /**
   * Setter method for property <tt>id</tt>.
   *
   * @param id dataValue to be assigned to property id
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property dataValue of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  /**
   * Setter method for property <tt>dataCenter</tt>.
   *
   * @param dataCenter dataValue to be assigned to property dataCenter
   */
  public void setDataCenter(String dataCenter) {
    this.dataCenter = dataCenter;
  }

  /**
   * Getter method for property <tt>dataKey</tt>.
   *
   * @return property dataValue of dataKey
   */
  public String getDataKey() {
    return dataKey;
  }

  /**
   * Setter method for property <tt>dataKey</tt>.
   *
   * @param dataKey dataValue to be assigned to property dataKey
   */
  public void setDataKey(String dataKey) {
    this.dataKey = dataKey;
  }

  /**
   * Getter method for property <tt>dataValue</tt>.
   *
   * @return property dataValue of dataValue
   */
  public String getDataValue() {
    return dataValue;
  }

  /**
   * Setter method for property <tt>dataValue</tt>.
   *
   * @param dataValue dataValue to be assigned to property dataValue
   */
  public void setDataValue(String dataValue) {
    this.dataValue = dataValue;
  }

  /**
   * Getter method for property <tt>gmtCreate</tt>.
   *
   * @return property dataValue of gmtCreate
   */
  public Date getGmtCreate() {
    return gmtCreate;
  }

  /**
   * Setter method for property <tt>gmtCreate</tt>.
   *
   * @param gmtCreate dataValue to be assigned to property gmtCreate
   */
  public void setGmtCreate(Date gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  /**
   * Getter method for property <tt>gmtModified</tt>.
   *
   * @return property dataValue of gmtModified
   */
  public Date getGmtModified() {
    return gmtModified;
  }

  /**
   * Setter method for property <tt>gmtModified</tt>.
   *
   * @param gmtModified dataValue to be assigned to property gmtModified
   */
  public void setGmtModified(Date gmtModified) {
    this.gmtModified = gmtModified;
  }

  /**
   * Getter method for property <tt>dataVersion</tt>.
   *
   * @return property value of dataVersion
   */
  @Override
  public long getDataVersion() {
    return dataVersion;
  }

  /**
   * Setter method for property <tt>dataVersion</tt>.
   *
   * @param dataVersion value to be assigned to property dataVersion
   */
  public void setDataVersion(long dataVersion) {
    this.dataVersion = dataVersion;
  }

  @Override
  public String toString() {
    return "ProvideDataDomain{"
        + "dataCenter='"
        + dataCenter
        + '\''
        + ", dataKey='"
        + dataKey
        + '\''
        + ", dataValue='"
        + dataValue
        + '\''
        + ", gmtCreate="
        + gmtCreate
        + ", gmtModified="
        + gmtModified
        + ", dataVersion="
        + dataVersion
        + '}';
  }
}
