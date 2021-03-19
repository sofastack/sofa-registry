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

import java.util.List;

/**
 * The type Publisher register.
 *
 * @author zhuoyu.sjw
 * @version $Id : PublisherRegister.java, v 0.1 2017-11-28 15:39 zhuoyu.sjw Exp $$
 */
public class PublisherRegister extends BaseRegister {

  private static final long serialVersionUID = 17084511452627565L;

  private List<DataBox> dataList;

  /**
   * Getter method for property <tt>dataList</tt>.
   *
   * @return property value of dataList
   */
  public List<DataBox> getDataList() {
    return dataList;
  }

  /**
   * Setter method for property <tt>dataList</tt>.
   *
   * @param dataList value to be assigned to property dataList
   */
  public void setDataList(List<DataBox> dataList) {
    this.dataList = dataList;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "PublisherRegister{" + "dataList=" + dataList + '}' + super.toString();
  }
}
