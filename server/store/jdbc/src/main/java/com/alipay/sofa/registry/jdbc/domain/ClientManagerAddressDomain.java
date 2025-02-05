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
 * @version $Id: ClientManagerAddress.java, v 0.1 2021年05月12日 20:11 xiaojian.xj Exp $
 */
public class ClientManagerAddressDomain implements DbEntry {

  /** primary key */
  private long id;

  /** local data center */
  private String dataCenter;

  /** local data center */
  private String address;

  /** CLIENT_OFF/CLIENT_OPEN */
  private String operation;

  /** create time */
  private Date gmtCreate;

  /** last update time */
  private Date gmtModify;

  private long gmtCreateUnixNanos;

  /** client_off pub true:持久化关流pub false:临时关流pub */
  private boolean pub;

  /** client_off sub true:持久化关流sub false:临时关流sub */
  private boolean sub;

  public ClientManagerAddressDomain() {}

  public ClientManagerAddressDomain(
      String dataCenter, String address, String operation, boolean pub, boolean sub) {
    this.dataCenter = dataCenter;
    this.address = address;
    this.operation = operation;
    this.pub = pub;
    this.sub = sub;
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
   * Getter method for property <tt>address</tt>.
   *
   * @return property value of address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Setter method for property <tt>address</tt>.
   *
   * @param address value to be assigned to property address
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Getter method for property <tt>operation</tt>.
   *
   * @return property value of operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Setter method for property <tt>operation</tt>.
   *
   * @param operation value to be assigned to property operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
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
   * Setter method for property <tt>pub</tt>.
   *
   * @param pub value to be assigned to property pub
   */
  public void setPub(boolean pub) {
    this.pub = pub;
  }

  public boolean isPub() {
    return pub;
  }
  /**
   * Setter method for property <tt>sub</tt>.
   *
   * @param sub value to be assigned to property sub
   */
  public void setSub(boolean sub) {
    this.sub = sub;
  }

  public boolean isSub() {
    return sub;
  }

  public long getGmtCreateUnixNanos() {
    return gmtCreateUnixNanos;
  }

  public long getGmtCreateUnixMillis() {
    return gmtCreateUnixNanos / 1000000;
  }

  public void setGmtCreateUnixNanos(long gmtGmtCreateUnixNanos) {
    this.gmtCreateUnixNanos = gmtCreateUnixNanos;
  }

  @Override
  public String toString() {
    return "ClientManagerAddress{"
        + "id="
        + id
        + ", dataCenter='"
        + dataCenter
        + '\''
        + ", address='"
        + address
        + '\''
        + ", operation='"
        + operation
        + '\''
        + ", gmtCreate="
        + gmtCreate
        + ", gmtModify="
        + gmtModify
        + '}';
  }
}
