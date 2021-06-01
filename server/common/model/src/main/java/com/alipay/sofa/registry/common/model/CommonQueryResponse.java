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
 * @author xiaojian.xj
 * @version $Id: CommonQueryResponse.java, v 0.1 2021年06月02日 15:30 xiaojian.xj Exp $
 */
public class CommonQueryResponse<T> extends CommonResponse {
  private static final long serialVersionUID = -2245323916822990362L;

  private T data;

  public CommonQueryResponse(boolean success, String message, T data) {
    super(success, message);
    this.data = data;
  }

  /**
   * build success resp
   *
   * @return
   */
  public static <T> CommonQueryResponse<T> buildSuccessResponse(T data) {
    return new CommonQueryResponse(true, "", data);
  }

  /**
   * build fail resp
   *
   * @param msg
   * @return
   */
  public static CommonQueryResponse buildFailedResponse(String msg) {
    return new CommonQueryResponse(false, msg, null);
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
   */
  public void setData(T data) {
    this.data = data;
  }
}
