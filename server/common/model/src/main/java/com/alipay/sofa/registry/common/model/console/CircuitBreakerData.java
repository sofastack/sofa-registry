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
import com.google.common.collect.Sets;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CircuitBreakerData {

  @JsonInclude(Include.NON_NULL)
  private Boolean addressSwitch;

  private Set<String> overflowAddress = Sets.newHashSet();

  private Set<String> address = Sets.newHashSet();

  public CircuitBreakerData() {}

  public CircuitBreakerData(Boolean addressSwitch, Set<String> address) {
    this.addressSwitch = addressSwitch;
    this.address = address;
  }

  public CircuitBreakerData(
      Boolean addressSwitch, Set<String> overflowAddress, Set<String> address) {
    this.addressSwitch = addressSwitch;
    this.overflowAddress = overflowAddress;
    this.address = address;
  }

  public Boolean isAddressSwitch() {
    return addressSwitch;
  }

  public void add(CircuitBreakOption option, Set<String> ips) {
    switch (option) {
      case OVERFLOW:
        overflowAddress.addAll(ips);
        break;
      case STOP_PUSH:
        address.addAll(ips);
        break;
      default:
        throw new IllegalArgumentException("invalid option: " + option);
    }
  }

  public void remove(CircuitBreakOption option, Set<String> ips) {
    switch (option) {
      case OVERFLOW:
        overflowAddress.removeAll(ips);
        break;
      case STOP_PUSH:
        address.removeAll(ips);
        break;
      default:
        throw new IllegalArgumentException("invalid option: " + option);
    }
  }

  /**
   * Getter method for property <tt>overflowAddress</tt>.
   *
   * @return property value of overflowAddress
   */
  public Set<String> getOverflowAddress() {
    return overflowAddress;
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
    return "CircuitBreakerData{"
        + "addressSwitch="
        + addressSwitch
        + ", overflowAddress="
        + overflowAddress
        + ", address="
        + address
        + '}';
  }

  public enum CircuitBreakOption {
    OVERFLOW("overflow"),
    STOP_PUSH("stopPush"),
    ;

    String option;

    CircuitBreakOption(String option) {
      this.option = option;
    }

    /**
     * Getter method for property <tt>option</tt>.
     *
     * @return property value of option
     */
    public String getOption() {
      return option;
    }

    public static CircuitBreakOption getByOption(String option) {
      if (StringUtils.isBlank(option)) {
        return null;
      }
      for (CircuitBreakOption value : CircuitBreakOption.values()) {
        if (StringUtils.equalsIgnoreCase(value.option, option)) {
          return value;
        }
      }
      return null;
    }
  }
}
