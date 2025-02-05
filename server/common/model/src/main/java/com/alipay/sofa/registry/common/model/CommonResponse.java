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
package com.alipay.sofa.registry.common.model;

import java.io.Serializable;

/**
 * common result
 *
 * @author qian.lqlq
 * @version $Id: CommonResponse.java, v 0.1 2017-12-06 15:48 qian.lqlq Exp $
 */
public class CommonResponse implements Serializable {

  private static final long serialVersionUID = 8269764971983130557L;

  private boolean success;

  private String message;

  /** constructor */
  public CommonResponse() {}

  /**
   * constructor
   *
   * @param success success
   * @param message message
   */
  public CommonResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  /**
   * build success resp
   *
   * @return CommonResponse
   */
  public static CommonResponse buildSuccessResponse() {
    return new CommonResponse(true, "");
  }

  /**
   * build success resp
   *
   * @param msg msg
   * @return CommonResponse
   */
  public static CommonResponse buildSuccessResponse(String msg) {
    return new CommonResponse(true, msg);
  }

  /**
   * build fail resp
   *
   * @param msg msg
   * @return CommonResponse
   */
  public static CommonResponse buildFailedResponse(String msg) {
    return new CommonResponse(false, msg);
  }

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
    final StringBuilder sb = new StringBuilder("CommonResponse{");
    sb.append("success=").append(success);
    sb.append(", message='").append(message).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
