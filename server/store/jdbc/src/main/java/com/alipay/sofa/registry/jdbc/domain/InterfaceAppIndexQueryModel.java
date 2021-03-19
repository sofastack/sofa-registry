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
 * @version $Id: InterfaceAppIndexQueryModel.java, v 0.1 2021年01月24日 19:07 xiaojian.xj Exp $
 */
public class InterfaceAppIndexQueryModel {

  /** local data center */
  private String dataCenter;

  /** interfaceName */
  private String interfaceName;

  public InterfaceAppIndexQueryModel(String dataCenter, String interfaceName) {
    this.dataCenter = dataCenter;
    this.interfaceName = interfaceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceAppIndexQueryModel)) {
      return false;
    }
    InterfaceAppIndexQueryModel that = (InterfaceAppIndexQueryModel) o;
    return Objects.equal(dataCenter, that.dataCenter)
        && Objects.equal(interfaceName, that.interfaceName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dataCenter, interfaceName);
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
}
