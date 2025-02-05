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
package com.alipay.sofa.registry.common.model.dataserver;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * datum store in dataserver
 *
 * @author qian.lqlq
 * @version $Id: Datum.java, v 0.1 2017-12-07 11:19 qian.lqlq Exp $
 */
public class Datum implements Serializable {

  private static final long serialVersionUID = 5307489721610438103L;

  private String dataInfoId;

  private String dataCenter;

  private String dataId;

  private String instanceId;

  private String group;
  // key=registerId
  private final Map<String, Publisher> pubMap = Maps.newHashMap();

  private long version;

  private List<Long> recentVersions;

  /** constructor */
  public Datum() {}

  /**
   * constructor
   *
   * @param dataInfoId dataInfoId
   * @param dataCenter dataCenter
   */
  public Datum(String dataInfoId, String dataCenter) {
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
    this.dataCenter = WordCache.getWordCache(dataCenter);
    this.recentVersions = Collections.EMPTY_LIST;
    updateVersion();
  }

  /**
   * constructor
   *
   * @param publisher publisher
   * @param dataCenter dataCenter
   */
  public Datum(Publisher publisher, String dataCenter) {
    this(publisher.getDataInfoId(), dataCenter);
    this.dataId = publisher.getDataId();
    this.instanceId = publisher.getInstanceId();
    this.group = publisher.getGroup();
    addPublisher(publisher);
  }

  /**
   * constructor
   *
   * @param publisher publisher
   * @param dataCenter dataCenter
   * @param version version
   */
  public Datum(Publisher publisher, String dataCenter, long version) {
    this.dataInfoId = publisher.getDataInfoId();
    this.dataCenter = WordCache.getWordCache(dataCenter);
    this.version = version;
    this.dataId = publisher.getDataId();
    this.instanceId = publisher.getInstanceId();
    this.group = publisher.getGroup();
    this.recentVersions = Collections.EMPTY_LIST;
    addPublisher(publisher);
  }

  public void updateVersion() {
    if (DatumVersionUtil.useConfregVersionGen()) {
      this.version = DatumVersionUtil.confregNextId(0);
    } else {
      this.version = DatumVersionUtil.nextId();
    }
  }

  /**
   * Getter method for property <tt>dataInfoId</tt>.
   *
   * @return property value of dataInfoId
   */
  public String getDataInfoId() {
    return dataInfoId;
  }

  /**
   * Setter method for property <tt>dataInfoId</tt>.
   *
   * @param dataInfoId value to be assigned to property dataInfoId
   */
  public void setDataInfoId(String dataInfoId) {
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  /**
   * Setter method for property <tt>dataCenter</tt>.
   *
   * @param dataCenter value to be assigned to property dataCenter
   */
  public void setDataCenter(String dataCenter) {
    this.dataCenter = WordCache.getWordCache(dataCenter);
  }

  /**
   * Getter method for property <tt>dataId</tt>.
   *
   * @return property value of dataId
   */
  public String getDataId() {
    return dataId;
  }

  /**
   * Setter method for property <tt>dataId</tt>.
   *
   * @param dataId value to be assigned to property dataId
   */
  public void setDataId(String dataId) {
    this.dataId = WordCache.getWordCache(dataId);
  }

  /**
   * Getter method for property <tt>instanceId</tt>.
   *
   * @return property value of instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Setter method for property <tt>instanceId</tt>.
   *
   * @param instanceId value to be assigned to property instanceId
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = WordCache.getWordCache(instanceId);
  }

  /**
   * Getter method for property <tt>group</tt>.
   *
   * @return property value of group
   */
  public String getGroup() {
    return group;
  }

  /**
   * Setter method for property <tt>group</tt>.
   *
   * @param group value to be assigned to property group
   */
  public void setGroup(String group) {
    this.group = WordCache.getWordCache(group);
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(long version) {
    this.version = version;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(128);
    sb.append("Datum={")
        .append(dataInfoId)
        .append(", dataCenter=")
        .append(dataCenter)
        .append(", size=")
        .append(publisherSize())
        .append(", ver=")
        .append(version)
        .append('}');
    return sb.toString();
  }

  public synchronized boolean addPublisher(Publisher publisher) {
    Publisher existing = pubMap.computeIfAbsent(publisher.getRegisterId(), k -> publisher);
    if (existing == publisher) {
      return true;
    }
    if (!existing.registerVersion().orderThan(publisher.registerVersion())) {
      return false;
    }
    pubMap.put(publisher.getRegisterId(), publisher);
    return true;
  }

  public synchronized int publisherSize() {
    return pubMap.size();
  }

  public synchronized void addPublishers(Map<String, Publisher> publisherMap) {
    if (publisherMap != null) {
      publisherMap.values().forEach(p -> addPublisher(p));
    }
  }

  public synchronized Map<String, Publisher> getPubMap() {
    return Collections.unmodifiableMap(Maps.newHashMap(pubMap));
  }

  /**
   * should not call that, just for json serde
   *
   * @param pubMap pubMap
   */
  public synchronized void setPubMap(Map<String, Publisher> pubMap) {
    this.pubMap.clear();
    if (pubMap != null) {
      this.pubMap.putAll(pubMap);
    }
  }

  public List<Long> getRecentVersions() {
    return recentVersions;
  }

  public void setRecentVersions(List<Long> recentVersions) {
    this.recentVersions = recentVersions;
  }
}
