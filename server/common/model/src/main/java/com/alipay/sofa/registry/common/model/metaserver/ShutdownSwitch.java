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
package com.alipay.sofa.registry.common.model.metaserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

/**
 * @author xiaojian.xj
 * @version : ShutdownSwitch.java, v 0.1 2021年10月14日 17:19 xiaojian.xj Exp $
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShutdownSwitch {

  private boolean shutdown = false;

  @JsonSetter(nulls = Nulls.SKIP)
  private String cause;

  public ShutdownSwitch() {}

  public ShutdownSwitch(boolean shutdown) {
    this.shutdown = shutdown;
  }

  public ShutdownSwitch(boolean shutdown, String cause) {
    this.shutdown = shutdown;
    this.cause = cause;
  }

  public static ShutdownSwitch defaultSwitch() {
    return new ShutdownSwitch();
  }

  public boolean isShutdown() {
    return shutdown;
  }

  public void setShutdown(boolean shutdown) {
    this.shutdown = shutdown;
  }

  /**
   * Getter method for property <tt>cause</tt>.
   *
   * @return property value of cause
   */
  public String getCause() {
    return cause;
  }

  /**
   * Setter method for property <tt>cause</tt>.
   *
   * @param cause value to be assigned to property cause
   */
  public void setCause(String cause) {
    this.cause = cause;
  }

  @Override
  public String toString() {
    return "ShutdownSwitch{" + "shutdown=" + shutdown + ", cause=" + cause + '}';
  }

  public enum CauseEnum {
    FORCE("force"),
    SELF_ADAPTION("self_adaption"),
    ;

    private String cause;

    CauseEnum(String cause) {
      this.cause = cause;
    }

    /**
     * Getter method for property <tt>cause</tt>.
     *
     * @return property value of cause
     */
    public String getCause() {
      return cause;
    }
  }
}
