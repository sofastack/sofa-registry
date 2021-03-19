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
package com.alipay.sofa.registry.server.data.resource;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author kezhu.wukz
 * @version $Id: DatumParam.java, v 0.1 2019-06-28 15:55 kezhu.wukz Exp $
 */
public class DatumParam implements Serializable {

  private static final long serialVersionUID = -3747877410102014868L;

  private String dataInfoId;

  private String dataCenter;

  private String publisherRegisterId = UUID.randomUUID().toString();

  private long publisherVersion = System.currentTimeMillis();

  private long publisherRegisterTimestamp = System.currentTimeMillis();

  private String publisherDataBox;

  private String publisherConnectId;

  private String publisherCell;

  /**
   * Getter method for property <tt>publisherCell</tt>.
   *
   * @return property value of publisherCell
   */
  public String getPublisherCell() {
    return publisherCell;
  }

  /**
   * Setter method for property <tt>publisherCell </tt>.
   *
   * @param publisherCell value to be assigned to property publisherCell
   */
  public void setPublisherCell(String publisherCell) {
    this.publisherCell = publisherCell;
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
   * Setter method for property <tt>dataInfoId </tt>.
   *
   * @param dataInfoId value to be assigned to property dataInfoId
   */
  public void setDataInfoId(String dataInfoId) {
    this.dataInfoId = dataInfoId;
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
   * Setter method for property <tt>dataCenter </tt>.
   *
   * @param dataCenter value to be assigned to property dataCenter
   */
  public void setDataCenter(String dataCenter) {
    this.dataCenter = dataCenter;
  }

  /**
   * Getter method for property <tt>publisherRegisterId</tt>.
   *
   * @return property value of publisherRegisterId
   */
  public String getPublisherRegisterId() {
    return publisherRegisterId;
  }

  /**
   * Setter method for property <tt>publisherRegisterId </tt>.
   *
   * @param publisherRegisterId value to be assigned to property publisherRegisterId
   */
  public void setPublisherRegisterId(String publisherRegisterId) {
    this.publisherRegisterId = publisherRegisterId;
  }

  /**
   * Getter method for property <tt>publisherVersion</tt>.
   *
   * @return property value of publisherVersion
   */
  public long getPublisherVersion() {
    return publisherVersion;
  }

  /**
   * Setter method for property <tt>publisherVersion </tt>.
   *
   * @param publisherVersion value to be assigned to property publisherVersion
   */
  public void setPublisherVersion(long publisherVersion) {
    this.publisherVersion = publisherVersion;
  }

  /**
   * Getter method for property <tt>publisherRegisterTimestamp</tt>.
   *
   * @return property value of publisherRegisterTimestamp
   */
  public long getPublisherRegisterTimestamp() {
    return publisherRegisterTimestamp;
  }

  /**
   * Setter method for property <tt>publisherRegisterTimestamp </tt>.
   *
   * @param publisherRegisterTimestamp value to be assigned to property publisherRegisterTimestamp
   */
  public void setPublisherRegisterTimestamp(long publisherRegisterTimestamp) {
    this.publisherRegisterTimestamp = publisherRegisterTimestamp;
  }

  /**
   * Getter method for property <tt>publisherDataBox</tt>.
   *
   * @return property value of publisherDataBox
   */
  public String getPublisherDataBox() {
    return publisherDataBox;
  }

  /**
   * Setter method for property <tt>publisherDataBox </tt>.
   *
   * @param publisherDataBox value to be assigned to property publisherDataBox
   */
  public void setPublisherDataBox(String publisherDataBox) {
    this.publisherDataBox = publisherDataBox;
  }

  /**
   * Getter method for property <tt>publisherConnectId</tt>.
   *
   * @return property value of publisherConnectId
   */
  public String getPublisherConnectId() {
    return publisherConnectId;
  }

  /**
   * Setter method for property <tt>publisherConnectId </tt>.
   *
   * @param publisherConnectId value to be assigned to property publisherConnectId
   */
  public void setPublisherConnectId(String publisherConnectId) {
    this.publisherConnectId = publisherConnectId;
  }
}
