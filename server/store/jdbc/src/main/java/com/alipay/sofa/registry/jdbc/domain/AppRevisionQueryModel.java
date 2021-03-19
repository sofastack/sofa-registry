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

import com.google.common.base.Objects;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionQueryModel.java, v 0.1 2021年01月24日 16:26 xiaojian.xj Exp $
 */
public class AppRevisionQueryModel {
  /** local data center */
  private String dataCenter;

  /** revision */
  private String revision;

  public AppRevisionQueryModel(String dataCenter, String revision) {
    this.dataCenter = dataCenter;
    this.revision = revision;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AppRevisionQueryModel)) {
      return false;
    }
    AppRevisionQueryModel that = (AppRevisionQueryModel) o;
    return Objects.equal(dataCenter, that.dataCenter) && Objects.equal(revision, that.revision);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dataCenter, revision);
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
}
