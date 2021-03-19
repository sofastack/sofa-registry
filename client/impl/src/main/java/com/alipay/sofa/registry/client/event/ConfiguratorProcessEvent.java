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

import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.model.Event;

/**
 * @author zhuoyu.sjw
 * @version $Id: ConfiguratorProcessEvent.java, v 0.1 2018-07-13 18:44 zhuoyu.sjw Exp $$
 */
public class ConfiguratorProcessEvent implements Event {

  private Configurator configurator;

  private RegistryClientConfig config;

  private long start;

  private long end;

  private Throwable throwable;

  /**
   * Getter method for property <tt>configurator</tt>.
   *
   * @return property value of configurator
   */
  public Configurator getConfigurator() {
    return configurator;
  }

  /**
   * Setter method for property <tt>configurator</tt>.
   *
   * @param configurator value to be assigned to property configurator
   */
  public void setConfigurator(Configurator configurator) {
    this.configurator = configurator;
  }

  /**
   * Getter method for property <tt>config</tt>.
   *
   * @return property value of config
   */
  public RegistryClientConfig getConfig() {
    return config;
  }

  /**
   * Setter method for property <tt>config</tt>.
   *
   * @param config value to be assigned to property config
   */
  public void setConfig(RegistryClientConfig config) {
    this.config = config;
  }

  /**
   * Getter method for property <tt>start</tt>.
   *
   * @return property value of start
   */
  public long getStart() {
    return start;
  }

  /**
   * Setter method for property <tt>start</tt>.
   *
   * @param start value to be assigned to property start
   */
  public void setStart(long start) {
    this.start = start;
  }

  /**
   * Getter method for property <tt>end</tt>.
   *
   * @return property value of end
   */
  public long getEnd() {
    return end;
  }

  /**
   * Setter method for property <tt>end</tt>.
   *
   * @param end value to be assigned to property end
   */
  public void setEnd(long end) {
    this.end = end;
  }

  /**
   * Getter method for property <tt>throwable</tt>.
   *
   * @return property value of throwable
   */
  public Throwable getThrowable() {
    return throwable;
  }

  /**
   * Setter method for property <tt>throwable</tt>.
   *
   * @param throwable value to be assigned to property throwable
   */
  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "ConfiguratorProcessEvent{"
        + "configurator="
        + configurator
        + ", start="
        + start
        + ", end="
        + end
        + ", throwable="
        + throwable
        + '}';
  }
}
