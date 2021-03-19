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
import java.util.List;

/**
 * @author zhuoyu.sjw
 * @version $Id: SyncConfigResponse.java, v 0.1 2018-03-14 23:08 zhuoyu.sjw Exp $$
 */
public class SyncConfigResponse extends Result implements Serializable {

  private static final long serialVersionUID = 5407436619633166827L;

  private List<String> availableSegments;

  private int retryInterval;

  /**
   * Getter method for property <tt>availableSegments</tt>.
   *
   * @return property value of availableSegments
   */
  public List<String> getAvailableSegments() {
    return availableSegments;
  }

  /**
   * Setter method for property <tt>availableSegments</tt>.
   *
   * @param availableSegments value to be assigned to property availableSegments
   */
  public void setAvailableSegments(List<String> availableSegments) {
    this.availableSegments = availableSegments;
  }

  /**
   * Getter method for property <tt>retryInterval</tt>.
   *
   * @return property value of retryInterval
   */
  public int getRetryInterval() {
    return retryInterval;
  }

  /**
   * Setter method for property <tt>retryInterval</tt>.
   *
   * @param retryInterval value to be assigned to property retryInterval
   */
  public void setRetryInterval(int retryInterval) {
    this.retryInterval = retryInterval;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "SyncConfigResponse{"
        + "availableSegments="
        + availableSegments
        + ", retryInterval="
        + retryInterval
        + '}';
  }
}
