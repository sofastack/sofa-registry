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
package com.alipay.sofa.registry.common.model.console;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CircuitBreakerData {

  @JsonInclude(Include.NON_NULL)
  private Boolean addressSwitch;

  private Set<String> address;

  public CircuitBreakerData() {}

  public CircuitBreakerData(Boolean addressSwitch, Set<String> address) {
    this.addressSwitch = addressSwitch;
    this.address = address;
  }

  public Boolean isAddressSwitch() {
    return addressSwitch;
  }

  public boolean addressSwitch(boolean defaultValue) {
    return addressSwitch == null ? defaultValue : addressSwitch;
  }

  /**
   * Setter method for property <tt>addressSwitch</tt>.
   *
   * @param addressSwitch value to be assigned to property addressSwitch
   */
  public void setAddressSwitch(Boolean addressSwitch) {
    this.addressSwitch = addressSwitch;
  }

  /**
   * Setter method for property <tt>address</tt>.
   *
   * @param address value to be assigned to property address
   */
  public void setAddress(Set<String> address) {
    this.address = address;
  }

  /**
   * Getter method for property <tt>address</tt>.
   *
   * @return property value of address
   */
  public Set<String> getAddress() {
    return address;
  }

  @Override
  public String toString() {
    return "CircuitBreakerData{" + "addressSwitch=" + addressSwitch + ", address=" + address + '}';
  }
}
