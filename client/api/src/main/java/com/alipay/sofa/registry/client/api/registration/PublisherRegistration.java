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

/**
 * The type Publisher registration.
 *
 * @author zhuoyu.sjw
 * @version $Id : PublisherRegistration.java, v 0.1 2017-11-23 15:39 zhuoyu.sjw Exp $$
 */
public class PublisherRegistration extends BaseRegistration {

  /**
   * Instantiates a new Publisher registration.
   *
   * @param dataId the data id
   */
  public PublisherRegistration(String dataId) {
    this.dataId = dataId;
  }

  /**
   * Equals boolean.
   *
   * @param o the o
   * @return the boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PublisherRegistration)) {
      return false;
    }

    PublisherRegistration that = (PublisherRegistration) o;

    if (dataId != null ? !dataId.equals(that.dataId) : that.dataId != null) {
      return false;
    }
    if (group != null ? !group.equals(that.group) : that.group != null) {
      return false;
    }
    if (appName != null ? !appName.equals(that.appName) : that.appName != null) {
      return false;
    }
    if (instanceId != null ? !instanceId.equals(that.instanceId) : that.instanceId != null) {
      return false;
    }
    return ip != null ? ip.equals(that.ip) : that.ip == null;
  }

  /**
   * Hash code int.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = dataId != null ? dataId.hashCode() : 0;
    result = 31 * result + (group != null ? group.hashCode() : 0);
    result = 31 * result + (appName != null ? appName.hashCode() : 0);
    result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
    result = 31 * result + (ip != null ? ip.hashCode() : 0);
    return result;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "PublisherRegistration{"
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
        + +'}';
  }
}
