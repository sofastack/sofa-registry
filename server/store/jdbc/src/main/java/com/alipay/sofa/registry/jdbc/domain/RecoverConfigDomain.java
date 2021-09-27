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

import java.util.Date;

/**
 * @author xiaojian.xj
 * @version : RecoverConfigModel.java, v 0.1 2021年09月22日 20:41 xiaojian.xj Exp $
 */
public class RecoverConfigDomain {

  /** primary dataKey */
  private long id;

  /** table name */
  private String propertyTable;

  /** property key name */
  private String propertyKey;

  /** create time */
  private Date gmtCreate;

  /** last update time */
  private Date gmtModified;

  public RecoverConfigDomain() {}

  public RecoverConfigDomain(String propertyTable, String propertyKey) {
    this.propertyTable = propertyTable;
    this.propertyKey = propertyKey;
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
   * Getter method for property <tt>propertyTable</tt>.
   *
   * @return property value of propertyTable
   */
  public String getPropertyTable() {
    return propertyTable;
  }

  /**
   * Setter method for property <tt>propertyTable</tt>.
   *
   * @param propertyTable value to be assigned to property propertyTable
   */
  public void setPropertyTable(String propertyTable) {
    this.propertyTable = propertyTable;
  }

  /**
   * Getter method for property <tt>propertyKey</tt>.
   *
   * @return property value of propertyKey
   */
  public String getPropertyKey() {
    return propertyKey;
  }

  /**
   * Setter method for property <tt>propertyKey</tt>.
   *
   * @param propertyKey value to be assigned to property propertyKey
   */
  public void setPropertyKey(String propertyKey) {
    this.propertyKey = propertyKey;
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
   * Getter method for property <tt>gmtModified</tt>.
   *
   * @return property value of gmtModified
   */
  public Date getGmtModified() {
    return gmtModified;
  }

  /**
   * Setter method for property <tt>gmtModified</tt>.
   *
   * @param gmtModified value to be assigned to property gmtModified
   */
  public void setGmtModified(Date gmtModified) {
    this.gmtModified = gmtModified;
  }

  @Override
  public String toString() {
    return "RecoverConfigDomain{"
        + "propertyTable='"
        + propertyTable
        + '\''
        + ", propertyKey='"
        + propertyKey
        + '\''
        + '}';
  }
}
