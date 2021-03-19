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
 * @version $Id: AppInterfaceIndexQueryModel.java, v 0.1 2021年01月24日 19:08 xiaojian.xj Exp $
 */
public class AppInterfaceIndexQueryModel {

  /** local data center */
  private String dataCenter;

  /** appName */
  private String appName;

  public AppInterfaceIndexQueryModel(String dataCenter, String appName) {
    this.dataCenter = dataCenter;
    this.appName = appName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AppInterfaceIndexQueryModel)) {
      return false;
    }
    AppInterfaceIndexQueryModel that = (AppInterfaceIndexQueryModel) o;
    return Objects.equal(dataCenter, that.dataCenter) && Objects.equal(appName, that.appName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dataCenter, appName);
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
}
