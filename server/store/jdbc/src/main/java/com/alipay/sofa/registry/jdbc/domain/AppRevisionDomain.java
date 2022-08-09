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
import java.util.Date;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionDomain.java, v 0.1 2021年01月18日 17:51 xiaojian.xj Exp $
 */
public class AppRevisionDomain implements DbEntry {

  /** primary key */
  private long id;

  /** local data center */
  private String dataCenter;

  /** revision */
  private String revision;

  /** appName */
  private String appName;

  /** clientVersion */
  private String clientVersion;

  /** base_params */
  private String baseParams;

  /** service_params */
  private String serviceParams;

  private String serviceParamsLarge;

  /** create time */
  private Date gmtCreate;

  /** last update time */
  private Date gmtModify;

  private boolean deleted;

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
  public String getBaseParams() {
    return baseParams;
  }

  /**
   * Setter method for property <tt>baseParams</tt>.
   *
   * @param baseParams value to be assigned to property baseParams
   */
  public void setBaseParams(String baseParams) {
    this.baseParams = baseParams;
  }

  /**
   * Getter method for property <tt>serviceParams</tt>.
   *
   * @return property value of serviceParams
   */
  public String getServiceParams() {
    return serviceParams;
  }

  /**
   * Setter method for property <tt>serviceParams</tt>.
   *
   * @param serviceParams value to be assigned to property serviceParams
   */
  public void setServiceParams(String serviceParams) {
    this.serviceParams = serviceParams;
  }

  /**
   * Getter method for property <tt>gmtCreate</tt>.
   *
   * @return property value of gmtCreate
   */
  public Date getGmtCreate() {
    return gmtCreate;
  }

  /**
   * Setter method for property <tt>gmtCreate</tt>.
   *
   * @param gmtCreate value to be assigned to property gmtCreate
   */
  public void setGmtCreate(Date gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  /**
   * Getter method for property <tt>gmtModify</tt>.
   *
   * @return property value of gmtModify
   */
  public Date getGmtModify() {
    return gmtModify;
  }

  /**
   * Setter method for property <tt>gmtModify</tt>.
   *
   * @param gmtModify value to be assigned to property gmtModify
   */
  public void setGmtModify(Date gmtModify) {
    this.gmtModify = gmtModify;
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

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public String toString() {
    return "AppRevisionDomain{"
        + "id="
        + id
        + ", dataCenter='"
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
        + ", baseParams='"
        + baseParams
        + '\''
        + ", serviceParams='"
        + serviceParams
        + '\''
        + ", gmtCreate="
        + gmtCreate
        + ", gmtModify="
        + gmtModify
        + ", deleted="
        + deleted
        + '}';
  }

  public String getServiceParamsLarge() {
    return serviceParamsLarge;
  }

  public void setServiceParamsLarge(String serviceParamsLarge) {
    this.serviceParamsLarge = serviceParamsLarge;
  }
}
