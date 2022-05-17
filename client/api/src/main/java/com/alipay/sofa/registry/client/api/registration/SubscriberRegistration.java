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
package com.alipay.sofa.registry.client.api.registration;

import com.alipay.sofa.registry.client.api.SubscriberDataObserver;
import com.alipay.sofa.registry.core.model.ScopeEnum;

/**
 * The type Subscriber registration.
 *
 * @author zhuoyu.sjw
 * @version $Id : SubscriberRegistration.java, v 0.1 2017-11-23 17:05 zhuoyu.sjw Exp $$
 */
public class SubscriberRegistration extends BaseRegistration {

  private ScopeEnum scopeEnum;

  private SubscriberDataObserver subscriberDataObserver;

  /**
   * Instantiates a new Subscriber registration.
   *
   * @param dataId the data id
   * @param subscriberDataObserver the subscriber data observer
   */
  public SubscriberRegistration(String dataId, SubscriberDataObserver subscriberDataObserver) {
    this.dataId = dataId;
    this.subscriberDataObserver = subscriberDataObserver;
  }

  /**
   * Getter method for property <tt>scopeEnum</tt>.
   *
   * @return property value of scopeEnum
   */
  public ScopeEnum getScopeEnum() {
    return scopeEnum;
  }

  /**
   * Setter method for property <tt>scopeEnum</tt>.
   *
   * @param scopeEnum value to be assigned to property scopeEnum
   */
  public void setScopeEnum(ScopeEnum scopeEnum) {
    this.scopeEnum = scopeEnum;
  }

  /**
   * Getter method for property <tt>subscriberDataObserver</tt>.
   *
   * @return property value of subscriberDataObserver
   */
  public SubscriberDataObserver getSubscriberDataObserver() {
    return subscriberDataObserver;
  }

  /**
   * Setter method for property <tt>subscriberDataObserver</tt>.
   *
   * @param subscriberDataObserver value to be assigned to property subscriberDataObserver
   */
  public void setSubscriberDataObserver(SubscriberDataObserver subscriberDataObserver) {
    this.subscriberDataObserver = subscriberDataObserver;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "SubscriberRegistration{"
        + "dataId='"
        + dataId
        + '\''
        + ", group='"
        + group
        + '\''
        + ", appName='"
        + appName
        + '\''
        + ", instanceId='"
        + instanceId
        + '\''
        + ", ip='"
        + ip
        + '\''
        + ", scopeEnum="
        + scopeEnum
        + ", subscriberDataObserver="
        + subscriberDataObserver
        + '}';
  }
}
