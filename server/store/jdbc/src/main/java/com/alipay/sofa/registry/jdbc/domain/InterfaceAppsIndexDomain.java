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

import com.alipay.sofa.registry.store.api.meta.DbEntry;
import com.alipay.sofa.registry.util.MessageDigests;
import java.sql.Timestamp;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsIndexDomain.java, v 0.1 2021年01月24日 17:05 xiaojian.xj Exp $
 */
public class InterfaceAppsIndexDomain implements DbEntry {

  /** primary key */
  private long id;

  /** local data center */
  private String dataCenter;

  /** interfaceName */
  private String interfaceName;

  /** appName */
  private String appName;

  /** reference */
  private boolean reference;

  /** hashcode */
  private String hashcode;

  /** create time */
  private Timestamp gmtCreate;

  /** last update time */
  private Timestamp gmtModify;

  public InterfaceAppsIndexDomain() {}

  public InterfaceAppsIndexDomain(String dataCenter, String interfaceName, String appName) {
    this.dataCenter = dataCenter;
    this.interfaceName = interfaceName;
    this.appName = appName;
    this.reference = true;
    // uk: dataCenter + appName + 32char
    this.hashcode = MessageDigests.getMd5String(interfaceName);
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
   * Getter method for property <tt>interfaceName</tt>.
   *
   * @return property value of interfaceName
   */
  public String getInterfaceName() {
    return interfaceName;
  }

  /**
   * Setter method for property <tt>interfaceName</tt>.
   *
   * @param interfaceName value to be assigned to property interfaceName
   */
  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
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

  public boolean isReference() {
    return reference;
  }

  /**
   * Setter method for property <tt>reference</tt>.
   *
   * @param reference value to be assigned to property reference
   */
  public void setReference(boolean reference) {
    this.reference = reference;
  }

  /**
   * Getter method for property <tt>gmtCreate</tt>.
   *
   * @return property value of gmtCreate
   */
  public Timestamp getGmtCreate() {
    return gmtCreate;
  }

  /**
   * Setter method for property <tt>gmtCreate</tt>.
   *
   * @param gmtCreate value to be assigned to property gmtCreate
   */
  public void setGmtCreate(Timestamp gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  /**
   * Getter method for property <tt>gmtModify</tt>.
   *
   * @return property value of gmtModify
   */
  public Timestamp getGmtModify() {
    return gmtModify;
  }

  /**
   * Setter method for property <tt>gmtModify</tt>.
   *
   * @param gmtModify value to be assigned to property gmtModify
   */
  public void setGmtModify(Timestamp gmtModify) {
    this.gmtModify = gmtModify;
  }

  /**
   * Getter method for property <tt>hashcode</tt>.
   *
   * @return property value of hashcode
   */
  public String getHashcode() {
    return hashcode;
  }

  /**
   * Setter method for property <tt>hashcode</tt>.
   *
   * @param hashcode value to be assigned to property hashcode
   */
  public void setHashcode(String hashcode) {
    this.hashcode = hashcode;
  }

  @Override
  public String toString() {
    return "InterfaceAppsIndexDomain{"
        + "id="
        + id
        + ", dataCenter='"
        + dataCenter
        + '\''
        + ", interfaceName='"
        + interfaceName
        + '\''
        + ", appName='"
        + appName
        + '\''
        + ", reference="
        + reference
        + ", hashcode='"
        + hashcode
        + '\''
        + ", gmtCreate="
        + gmtCreate
        + ", gmtModify="
        + gmtModify
        + '}';
  }
}
