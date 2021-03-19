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
package com.alipay.sofa.registry.core.model;

import java.io.Serializable;

/**
 * @author zhuoyu.sjw
 * @version $Id: RegisterResponse.java, v 0.1 2018-03-05 18:11 zhuoyu.sjw Exp $$
 */
public class RegisterResponse implements Serializable {

  private static final long serialVersionUID = -3692498042919432434L;

  private boolean success;

  private String registId;

  private long version;

  private boolean refused;

  private String message;

  /**
   * Getter method for property <tt>success</tt>.
   *
   * @return property value of success
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * Setter method for property <tt>success</tt>.
   *
   * @param success value to be assigned to property success
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /**
   * Getter method for property <tt>registId</tt>.
   *
   * @return property value of registId
   */
  public String getRegistId() {
    return registId;
  }

  /**
   * Setter method for property <tt>registId</tt>.
   *
   * @param registId value to be assigned to property registId
   */
  public void setRegistId(String registId) {
    this.registId = registId;
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * Getter method for property <tt>refused</tt>.
   *
   * @return property value of refused
   */
  public boolean isRefused() {
    return refused;
  }

  /**
   * Setter method for property <tt>refused</tt>.
   *
   * @param refused value to be assigned to property refused
   */
  public void setRefused(boolean refused) {
    this.refused = refused;
  }

  /**
   * Getter method for property <tt>message</tt>.
   *
   * @return property value of message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Setter method for property <tt>message</tt>.
   *
   * @param message value to be assigned to property message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "RegisterResponse{"
        + "success="
        + success
        + ", registId='"
        + registId
        + '\''
        + ", version="
        + version
        + ", refused="
        + refused
        + ", message='"
        + message
        + '\''
        + '}';
  }
}
