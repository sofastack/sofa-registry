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

import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.task.TaskEvent;
import com.alipay.sofa.registry.client.task.Worker;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * The type Default publisher.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultPublisher.java, v 0.1 2017-11-23 20:45 zhuoyu.sjw Exp $$
 */
public class DefaultPublisher extends AbstractInternalRegister implements Publisher {

  private final String REGIST_ID;
  private PublisherRegistration registration;
  private Worker worker;
  private Collection<String> dataList;
  private RegistryClientConfig config;

  /**
   * Instantiates a new Default publisher.
   *
   * @param registration the publisher registration
   * @param worker the worker
   */
  DefaultPublisher(PublisherRegistration registration, Worker worker, RegistryClientConfig config) {
    this.registration = registration;
    this.worker = worker;
    this.config = config;
    this.REGIST_ID = UUID.randomUUID().toString();
  }

  /** @see Publisher#republish(String...) */
  @Override
  public void republish(String... data) {
    if (isRefused()) {
      throw new IllegalStateException(
          "Publisher is refused by server. Try to check your configuration.");
    }

    if (!isEnabled()) {
      throw new IllegalStateException("Unregistered publisher can not be reused.");
    }

    writeLock.lock();
    try {
      if (null != data) {
        this.dataList = Arrays.asList(data);
      }

      this.getPubVersion().incrementAndGet();
      this.setTimestamp(System.currentTimeMillis());
      this.waitToSync();
    } finally {
      writeLock.unlock();
    }
    this.worker.schedule(new TaskEvent(this));
  }

  /** Unregister. */
  @Override
  public void unregister() {
    if (isEnabled()) {
      super.unregister();
      this.worker.schedule(new TaskEvent(this));
    }
  }

  /**
   * Assembly publisher register.
   *
   * @return the publisher register
   */
  @Override
  public PublisherRegister assembly() {
    readLock.lock();
    PublisherRegister register;
    try {
      register = new PublisherRegister();
      register.setRegistId(REGIST_ID);
      setAttributes(register, registration, config);
      // auth signature
      setAuthSignature(register);

      if (isEnabled()) {
        register.setEventType(EventTypeConstants.REGISTER);
        if (null != dataList) {
          List<DataBox> dataBoxes = new ArrayList<DataBox>();
          for (String data : dataList) {
            dataBoxes.add(new DataBox(data));
          }
          register.setDataList(dataBoxes);
        }
      } else {
        register.setEventType(EventTypeConstants.UNREGISTER);
      }
    } finally {
      readLock.unlock();
    }
    return register;
  }

  /** @see Publisher#getDataId() */
  @Override
  public String getDataId() {
    return registration.getDataId();
  }

  @Override
  public String getGroup() {
    return registration.getGroup();
  }

  /** @see Publisher#getRegistId() */
  @Override
  public String getRegistId() {
    return REGIST_ID;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "DefaultPublisher{" + "registration=" + registration + '}' + super.toString();
  }
}
