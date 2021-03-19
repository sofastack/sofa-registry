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
package com.alipay.sofa.registry.client.model;

import com.alipay.sofa.registry.core.model.DataBox;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhuoyu.sjw
 * @version $Id: SegmentData.java, v 0.1 2018-03-12 22:13 zhuoyu.sjw Exp $$
 */
public class SegmentData implements Serializable {

  private static final long serialVersionUID = 3066687802211879826L;

  private String segment;

  private Map<String, List<DataBox>> data;

  private Long version;

  /**
   * Getter method for property <tt>segment</tt>.
   *
   * @return property value of segment
   */
  public String getSegment() {
    return segment;
  }

  /**
   * Setter method for property <tt>segment</tt>.
   *
   * @param segment value to be assigned to property segment
   */
  public void setSegment(String segment) {
    this.segment = segment;
  }

  /**
   * Getter method for property <tt>data</tt>.
   *
   * @return property value of data
   */
  public Map<String, List<DataBox>> getData() {
    return data;
  }

  /**
   * Setter method for property <tt>data</tt>.
   *
   * @param data value to be assigned to property data
   */
  public void setData(Map<String, List<DataBox>> data) {
    this.data = data;
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public Long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(Long version) {
    this.version = version;
  }
}
