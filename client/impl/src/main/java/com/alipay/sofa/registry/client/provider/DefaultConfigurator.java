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
package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.api.ConfigDataObserver;
import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.model.ConfigData;
import com.alipay.sofa.registry.client.api.registration.ConfiguratorRegistration;
import com.alipay.sofa.registry.client.model.ConfiguratorData;
import com.alipay.sofa.registry.client.task.TaskEvent;
import com.alipay.sofa.registry.client.task.Worker;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.DataBox;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Default configurator.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultConfigurator.java, v 0.1 2018-04-18 14:41 zhuoyu.sjw Exp $$
 */
public class DefaultConfigurator extends AbstractInternalRegister implements Configurator {
  private final String REGIST_ID;

  private ConfiguratorRegistration registration;

  private ConfigDataObserver configDataObserver;

  private RegistryClientConfig config;

  private ConfiguratorData configuratorData;

  private Worker worker;

  private AtomicBoolean init = new AtomicBoolean(false);

  /**
   * Instantiates a new Default configurator.
   *
   * @param config the config
   * @param worker the worker
   */
  public DefaultConfigurator(
      ConfiguratorRegistration registration, RegistryClientConfig config, Worker worker) {
    if (null != registration) {
      this.configDataObserver = registration.getConfigDataObserver();
    }
    this.registration = registration;
    this.config = config;
    this.worker = worker;
    this.REGIST_ID = UUID.randomUUID().toString();
  }

  /**
   * Gets data observer.
   *
   * @return the data observer
   */
  @Override
  public ConfigDataObserver getDataObserver() {
    return configDataObserver;
  }

  /**
   * Setter method for property <tt>configDataObserver</tt>.
   *
   * @param configDataObserver value to be assigned to property configDataObserver
   */
  @Override
  public void setDataObserver(ConfigDataObserver configDataObserver) {
    this.configDataObserver = configDataObserver;
  }

  /**
   * Peek data config data.
   *
   * @return the config data
   */
  @Override
  public ConfigData peekData() {
    if (!init.get()) {
      throw new IllegalStateException("Config data is not ready yet.");
    }
    if (null != configuratorData) {
      DataBox dataBox = configuratorData.getDataBox();
      if (null != dataBox) {
        return new DefaultConfigData(dataBox.getData());
      }
    }
    return new DefaultConfigData(null);
  }

  /**
   * Assembly object.
   *
   * @return the object
   */
  @Override
  public Object assembly() {
    readLock.lock();
    ConfiguratorRegister register = new ConfiguratorRegister();
    try {
      register.setRegistId(REGIST_ID);
      setAttributes(register, registration, config);
      // auth signature
      setAuthSignature(register);

      if (isEnabled()) {
        register.setEventType(EventTypeConstants.REGISTER);
      } else {
        register.setEventType(EventTypeConstants.UNREGISTER);
      }
    } finally {
      readLock.unlock();
    }
    return register;
  }

  /**
   * Put configurator data.
   *
   * @param receivedConfigData the received config data
   */
  public void putConfiguratorData(ConfiguratorData receivedConfigData) {
    writeLock.lock();
    try {
      if (null == receivedConfigData) {
        return;
      }

      // default version set to zero
      if (null == receivedConfigData.getVersion()) {
        receivedConfigData.setVersion(0L);
      }

      if (null == configuratorData
          || receivedConfigData.getVersion() > configuratorData.getVersion()) {
        configuratorData = receivedConfigData;
        init.compareAndSet(false, true);
      }

    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Gets data id.
   *
   * @return the data id
   */
  @Override
  public String getDataId() {
    return registration.getDataId();
  }

  /**
   * Gets group.
   *
   * @return the group
   */
  @Override
  public String getGroup() {
    return registration.getGroup();
  }

  /**
   * Gets regist id.
   *
   * @return the regist id
   */
  @Override
  public String getRegistId() {
    return REGIST_ID;
  }

  /** Unregister. */
  @Override
  public void unregister() {
    if (isEnabled()) {
      super.unregister();
      this.worker.schedule(new TaskEvent(this));
    }
  }

  @Override
  public String toString() {
    return "DefaultConfigurator{" + "registration=" + registration + '}';
  }
}
