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
 * The type DataBox.
 *
 * @author zhuoyu.sjw
 * @version $Id : DataBox.java, v 0.1 2017-11-28 17:47 zhuoyu.sjw Exp $$
 */
public class DataBox implements Serializable {

  /** UID */
  private static final long serialVersionUID = 2817539491173993030L;

  /** Actual data */
  private String data;

  /** Instantiates a new DataBox. */
  public DataBox() {}

  /**
   * Instantiates a new Data box.
   *
   * @param data the data
   */
  public DataBox(String data) {
    this.data = data;
  }

  /**
   * Getter method for property <tt>data</tt>.
   *
   * @return property value of data
   */
  public String getData() {
    return data;
  }

  /**
   * Setter method for property <tt>data</tt>.
   *
   * @param data value to be assigned to property data
   */
  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "DataBox{data='" + data + "'}";
  }
}
