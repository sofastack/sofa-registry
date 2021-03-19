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
 * @version $Id: Result.java, v 0.1 2017-11-30 15:48 zhuoyu.sjw Exp $$
 */
public class Result implements Serializable {

  private static final long serialVersionUID = -3861771860629530576L;

  private boolean success;

  private String message;

  public Result() {}

  public Result(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public static Result failed(String message) {
    return new Result(false, message);
  }

  public static Result success() {
    return new Result(true, null);
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

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "Result{" + "success=" + success + ", message='" + message + '\'' + '}';
  }
}
