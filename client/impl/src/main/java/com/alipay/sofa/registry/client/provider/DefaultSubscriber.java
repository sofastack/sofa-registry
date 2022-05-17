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

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.SubscriberDataObserver;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.client.model.SegmentData;
import com.alipay.sofa.registry.client.task.TaskEvent;
import com.alipay.sofa.registry.client.task.Worker;
import com.alipay.sofa.registry.client.util.CommonUtils;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Default subscriber multi.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultSubscriber.java, v 0.1 2017-11-23 22:13 zhuoyu.sjw Exp $$
 */
public class DefaultSubscriber extends AbstractInternalRegister implements Subscriber {

  private final String REGIST_ID;
  private SubscriberDataObserver dataObserver;
  private ConcurrentHashMap<String, SegmentData> data =
      new ConcurrentHashMap<String, SegmentData>();
  private AtomicBoolean init = new AtomicBoolean(false);
  private RegistryClientConfig config;
  private SubscriberRegistration registration;
  private Worker worker;
  private volatile String localZone;
  private List<String> availableSegments = new ArrayList<String>();

  /**
   * Instantiates a new Default subscriber multi.
   *
   * @param registration the registration
   */
  DefaultSubscriber(
      SubscriberRegistration registration, Worker worker, RegistryClientConfig config) {
    if (null != registration) {
      this.dataObserver = registration.getSubscriberDataObserver();
    }
    this.registration = registration;
    this.worker = worker;
    this.config = config;
    this.REGIST_ID = UUID.randomUUID().toString();
    this.localZone = config.getZone();
    this.getPubVersion().incrementAndGet();
  }

  /** @see Subscriber#getDataObserver() */
  @Override
  public SubscriberDataObserver getDataObserver() {
    return dataObserver;
  }

  /** @see Subscriber#setDataObserver(SubscriberDataObserver) */
  @Override
  public void setDataObserver(SubscriberDataObserver dataObserver) {
    this.dataObserver = dataObserver;
  }

  /** @see Subscriber#peekData() */
  @Override
  public UserData peekData() {
    readLock.lock();
    try {
      if (!init.get()) {
        // todo sync read from server
        return new DefaultUserData();
      }
      Set<Entry<String, SegmentData>> values = data.entrySet();
      DefaultUserData userData = new DefaultUserData();
      if (null == localZone) {
        userData.setLocalZone(config.getZone());
      } else {
        userData.setLocalZone(localZone);
      }
      Map<String, List<String>> zoneMap = new HashMap<String, List<String>>();
      for (Entry<String, SegmentData> segmentDataEntry : values) {
        String segment = segmentDataEntry.getKey();

        // only accept available segments, when available segments is empty accept all
        if (CommonUtils.isNotEmpty(availableSegments) && !availableSegments.contains(segment)) {
          continue;
        }

        SegmentData segmentData = segmentDataEntry.getValue();

        if (null == segmentData) {
          continue;
        }

        Map<String, List<DataBox>> data = segmentData.getData();
        for (Entry<String, List<DataBox>> entry : data.entrySet()) {
          String zone = entry.getKey();
          List<String> resultList = zoneMap.get(zone);
          if (null == resultList) {
            resultList = new ArrayList<String>();
            zoneMap.put(zone, resultList);
          }
          List<DataBox> dataList = entry.getValue();
          for (DataBox dataBox : dataList) {
            resultList.add(dataBox.getData());
          }
        }
      }
      userData.setZoneData(zoneMap);
      return userData;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Gets scope enum.
   *
   * @return the scope enum
   */
  @Override
  public ScopeEnum getScopeEnum() {
    return registration.getScopeEnum();
  }

  /** Unregister. */
  @Override
  public void unregister() {
    if (isEnabled()) {
      super.unregister();
      worker.schedule(new TaskEvent(this));
    }
  }

  /**
   * Assembly subscriber register.
   *
   * @return the subscriber register
   */
  @Override
  public SubscriberRegister assembly() {
    readLock.lock();
    SubscriberRegister register;
    try {
      if (null == registration.getScopeEnum()) {
        registration.setScopeEnum(ScopeEnum.zone);
      }

      register = new SubscriberRegister();
      register.setRegistId(REGIST_ID);
      register.setScope(registration.getScopeEnum().name());
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

  public void putReceivedData(SegmentData segmentData, String localZone) {
    writeLock.lock();
    try {
      putSegmentData(segmentData);
      this.localZone = localZone;
    } finally {
      writeLock.unlock();
    }
  }

  private void putSegmentData(SegmentData segmentData) {
    if (null != segmentData) {

      SegmentData existsData = data.putIfAbsent(segmentData.getSegment(), segmentData);
      if (null == existsData) {
        init.compareAndSet(false, true);
        return;
      }

      if (existsData.getVersion() < segmentData.getVersion()) {
        boolean result = data.replace(segmentData.getSegment(), existsData, segmentData);
        if (!result) {
          putSegmentData(segmentData);
        }
        init.compareAndSet(false, true);
      }
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

  /**
   * Getter method for property <tt>availableSegments</tt>.
   *
   * @return property value of availableSegments
   */
  public List<String> getAvailableSegments() {
    readLock.lock();
    try {
      return new ArrayList<String>(availableSegments);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Setter method for property <tt>availableSegments</tt>.
   *
   * @param availableSegments value to be assigned to property availableSegments
   */
  public void setAvailableSegments(List<String> availableSegments) {
    writeLock.lock();
    try {
      if (null == availableSegments) {
        this.availableSegments = new ArrayList<String>();
      } else {
        this.availableSegments = new ArrayList<String>(availableSegments);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public boolean isInited() {
    return init.get();
  }

  @Override
  public String toString() {
    return "DefaultSubscriber{" + "registration=" + registration + '}' + super.toString();
  }
}
