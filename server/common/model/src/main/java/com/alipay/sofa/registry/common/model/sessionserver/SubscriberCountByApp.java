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
package com.alipay.sofa.registry.common.model.sessionserver;

import java.io.Serializable;

/**
 * app ins count
 *
 * @author shanyan
 * @version $Id: SubscriberCountByApp.java, v 0.1 2024-11-25
 */
public class SubscriberCountByApp implements Serializable {

  private static final long serialVersionUID = 1223527440228443734L;

  /** appName */
  private String appName;

  /** count */
  private int count;

  public SubscriberCountByApp(String appName, int count) {
    this.appName = appName;
    this.count = count;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @Override
  public String toString() {
    return "SubscriberCountByApp{" + "appName='" + appName + '\'' + ", count=" + count + '}';
  }
}
