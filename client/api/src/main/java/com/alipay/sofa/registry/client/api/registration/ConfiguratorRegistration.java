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

import com.alipay.sofa.registry.client.api.ConfigDataObserver;

/**
 * The type Configurator registration.
 *
 * @author zhuoyu.sjw
 * @version $Id : ConfiguratorRegistration.java, v 0.1 2018-04-20 11:36 zhuoyu.sjw Exp $$
 */
public class ConfiguratorRegistration extends BaseRegistration {

  private ConfigDataObserver configDataObserver;

  /**
   * Instantiates a new Configurator registration.
   *
   * @param dataId the data id
   * @param configDataObserver the config data observer
   */
  public ConfiguratorRegistration(String dataId, ConfigDataObserver configDataObserver) {
    this.dataId = dataId;
    this.configDataObserver = configDataObserver;
  }

  /**
   * Getter method for property <tt>configDataObserver</tt>.
   *
   * @return property value of configDataObserver
   */
  public ConfigDataObserver getConfigDataObserver() {
    return configDataObserver;
  }

  /**
   * Setter method for property <tt>configDataObserver</tt>.
   *
   * @param configDataObserver value to be assigned to property configDataObserver
   */
  public void setConfigDataObserver(ConfigDataObserver configDataObserver) {
    this.configDataObserver = configDataObserver;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "ConfiguratorRegistration{"
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
        + '}';
  }
}
