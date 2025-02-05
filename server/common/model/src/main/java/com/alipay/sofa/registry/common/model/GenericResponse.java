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

/**
 * generic response
 *
 * @author qian.lqlq
 * @version $Id: GenericResponse.java, v 0.1 2017-12-06 19:54 qian.lqlq Exp $
 */
public class GenericResponse<T> extends CommonResponse {

  private static final long serialVersionUID = -3986568405174281303L;

  private T data;

  /**
   * get success response
   *
   * @param data data
   * @return GenericResponse
   */
  public GenericResponse<T> fillSucceed(T data) {
    this.setSuccess(true);
    this.setData(data);
    return this;
  }

  /**
   * get fail response
   *
   * @param msg msg
   * @return GenericResponse
   */
  public GenericResponse<T> fillFailed(String msg) {
    this.setSuccess(false);
    this.setMessage(msg);
    return this;
  }

  /**
   * get fail response
   *
   * @param data data
   * @return GenericResponse
   */
  public GenericResponse<T> fillFailData(T data) {
    this.setSuccess(false);
    this.setData(data);
    return this;
  }

  /**
   * Getter method for property <tt>data</tt>.
   *
   * @return property value of data
   */
  public T getData() {
    return data;
  }

  /**
   * Setter method for property <tt>data</tt>.
   *
   * @param data value to be assigned to property data
   * @return GenericResponse
   */
  public GenericResponse<T> setData(T data) {
    this.data = data;
    return this;
  }
}
