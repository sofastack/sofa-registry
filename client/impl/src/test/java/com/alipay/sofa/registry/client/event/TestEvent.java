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
package com.alipay.sofa.registry.client.event;

import com.alipay.sofa.registry.client.api.model.Event;

/**
 * @author zhuoyu.sjw
 * @version $Id: TestEvent.java, v 0.1 2018-07-15 22:38 zhuoyu.sjw Exp $$
 */
public class TestEvent implements Event {

  private String data;

  public TestEvent(String data) {
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
}
