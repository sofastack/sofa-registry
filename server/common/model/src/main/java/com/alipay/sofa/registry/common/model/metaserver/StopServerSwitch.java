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
 * @version : StopServerSwitch.java, v 0.1 2021年10月14日 17:19 xiaojian.xj Exp $
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopServerSwitch {

  private boolean stopServer = false;

  @JsonSetter(nulls = Nulls.SKIP)
  private CauseEnum causeEnum;

  public StopServerSwitch() {}

  public StopServerSwitch(boolean stopServer) {
    this.stopServer = stopServer;
  }

  public StopServerSwitch(boolean stopServer, CauseEnum causeEnum) {
    this.stopServer = stopServer;
    this.causeEnum = causeEnum;
  }

  public static StopServerSwitch defaultSwitch() {
    return new StopServerSwitch();
  }

  public boolean isStopServer() {
    return stopServer;
  }

  public void setStopServer(boolean stopServer) {
    this.stopServer = stopServer;
  }
  /**
   * Getter method for property <tt>causeEnum</tt>.
   *
   * @return property value of causeEnum
   */
  public CauseEnum getCauseEnum() {
    return causeEnum;
  }

  /**
   * Setter method for property <tt>causeEnum</tt>.
   *
   * @param causeEnum value to be assigned to property causeEnum
   */
  public void setCauseEnum(CauseEnum causeEnum) {
    this.causeEnum = causeEnum;
  }

  @Override
  public String toString() {
    return "StopServerSwitch{" + "stopServer=" + stopServer + ", causeEnum=" + causeEnum + '}';
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
