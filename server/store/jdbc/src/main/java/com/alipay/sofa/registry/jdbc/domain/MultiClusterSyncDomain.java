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
package com.alipay.sofa.registry.jdbc.domain;

import com.alipay.sofa.registry.jdbc.version.config.ConfigEntry;
import com.alipay.sofa.registry.store.api.meta.DbEntry;
import java.util.Date;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncInfo.java, v 0.1 2022年04月13日 14:41 xiaojian.xj Exp $
 */
public class MultiClusterSyncDomain implements DbEntry, ConfigEntry {
  /** primary key */
  private long id;

  /** local data center */
  private String dataCenter;

  /** sync remote data center */
  private String remoteDataCenter;

  /** remote meta address, use to get meta leader */
  private String remoteMetaAddress;

  /** true/false */
  private String enableSyncDatum;

  /** true/false */
  private String enablePush;

  /** sync dataInfoIds */
  private String syncDataInfoIds;

  /** sync publish groups */
  private String synPublisherGroups;

  /** ignore sync dataInfoIds */
  private String ignoreDataInfoIds;

  /** data version */
  private long dataVersion;

  /** create time */
  private Date gmtCreate;

  /** last update time */
  private Date gmtModified;

  @Override
  public long getId() {
    return id;
  }

  @Override
  public Date getGmtCreate() {
    return gmtCreate;
  }

  public MultiClusterSyncDomain() {}

  public MultiClusterSyncDomain(
      String dataCenter,
      String remoteDataCenter,
      String remoteMetaAddress,
      String enableSyncDatum,
      String enablePush,
      String syncDataInfoIds,
      String synPublisherGroups,
      String ignoreDataInfoIds,
      long dataVersion) {
    this.dataCenter = dataCenter;
    this.remoteDataCenter = remoteDataCenter;
    this.remoteMetaAddress = remoteMetaAddress;
    this.enableSyncDatum = enableSyncDatum;
    this.enablePush = enablePush;
    this.syncDataInfoIds = syncDataInfoIds;
    this.synPublisherGroups = synPublisherGroups;
    this.ignoreDataInfoIds = ignoreDataInfoIds;
    this.dataVersion = dataVersion;
  }

  /**
   * Setter method for property <tt>id</tt>.
   *
   * @param id value to be assigned to property id
   */
  public void setId(long id) {
    this.id = id;
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
    this.dataCenter = dataCenter;
  }

  /**
   * Getter method for property <tt>remoteDataCenter</tt>.
   *
   * @return property value of remoteDataCenter
   */
  public String getRemoteDataCenter() {
    return remoteDataCenter;
  }

  /**
   * Setter method for property <tt>remoteDataCenter</tt>.
   *
   * @param remoteDataCenter value to be assigned to property remoteDataCenter
   */
  public void setRemoteDataCenter(String remoteDataCenter) {
    this.remoteDataCenter = remoteDataCenter;
  }

  /**
   * Getter method for property <tt>remoteMetaAddress</tt>.
   *
   * @return property value of remoteMetaAddress
   */
  public String getRemoteMetaAddress() {
    return remoteMetaAddress;
  }

  /**
   * Setter method for property <tt>remoteMetaAddress</tt>.
   *
   * @param remoteMetaAddress value to be assigned to property remoteMetaAddress
   */
  public void setRemoteMetaAddress(String remoteMetaAddress) {
    this.remoteMetaAddress = remoteMetaAddress;
  }

  /**
   * Getter method for property <tt>enableSyncDatum</tt>.
   *
   * @return property value of enableSyncDatum
   */
  public String getEnableSyncDatum() {
    return enableSyncDatum;
  }

  /**
   * Setter method for property <tt>enableSyncDatum</tt>.
   *
   * @param enableSyncDatum value to be assigned to property enableSyncDatum
   */
  public void setEnableSyncDatum(String enableSyncDatum) {
    this.enableSyncDatum = enableSyncDatum;
  }

  /**
   * Getter method for property <tt>enablePush</tt>.
   *
   * @return property value of enablePush
   */
  public String getEnablePush() {
    return enablePush;
  }

  /**
   * Setter method for property <tt>enablePush</tt>.
   *
   * @param enablePush value to be assigned to property enablePush
   */
  public void setEnablePush(String enablePush) {
    this.enablePush = enablePush;
  }

  /**
   * Getter method for property <tt>syncDataInfoIds</tt>.
   *
   * @return property value of syncDataInfoIds
   */
  public String getSyncDataInfoIds() {
    return syncDataInfoIds;
  }

  /**
   * Setter method for property <tt>syncDataInfoIds</tt>.
   *
   * @param syncDataInfoIds value to be assigned to property syncDataInfoIds
   */
  public void setSyncDataInfoIds(String syncDataInfoIds) {
    this.syncDataInfoIds = syncDataInfoIds;
  }

  /**
   * Getter method for property <tt>synPublisherGroups</tt>.
   *
   * @return property value of synPublisherGroups
   */
  public String getSynPublisherGroups() {
    return synPublisherGroups;
  }

  /**
   * Setter method for property <tt>synPublisherGroups</tt>.
   *
   * @param synPublisherGroups value to be assigned to property synPublisherGroups
   */
  public void setSynPublisherGroups(String synPublisherGroups) {
    this.synPublisherGroups = synPublisherGroups;
  }

  /**
   * Getter method for property <tt>ignoreDataInfoIds</tt>.
   *
   * @return property value of ignoreDataInfoIds
   */
  public String getIgnoreDataInfoIds() {
    return ignoreDataInfoIds;
  }

  /**
   * Setter method for property <tt>ignoreDataInfoIds</tt>.
   *
   * @param ignoreDataInfoIds value to be assigned to property ignoreDataInfoIds
   */
  public void setIgnoreDataInfoIds(String ignoreDataInfoIds) {
    this.ignoreDataInfoIds = ignoreDataInfoIds;
  }

  /**
   * Setter method for property <tt>gmtCreate</tt>.
   *
   * @param gmtCreate value to be assigned to property gmtCreate
   */
  public void setGmtCreate(Date gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  /**
   * Getter method for property <tt>gmtModified</tt>.
   *
   * @return property value of gmtModified
   */
  public Date getGmtModified() {
    return gmtModified;
  }

  /**
   * Setter method for property <tt>gmtModified</tt>.
   *
   * @param gmtModified value to be assigned to property gmtModified
   */
  public void setGmtModified(Date gmtModified) {
    this.gmtModified = gmtModified;
  }

  /**
   * Getter method for property <tt>dataVersion</tt>.
   *
   * @return property value of dataVersion
   */
  @Override
  public long getDataVersion() {
    return dataVersion;
  }

  /**
   * Setter method for property <tt>dataVersion</tt>.
   *
   * @param dataVersion value to be assigned to property dataVersion
   */
  public void setDataVersion(long dataVersion) {
    this.dataVersion = dataVersion;
  }
}
