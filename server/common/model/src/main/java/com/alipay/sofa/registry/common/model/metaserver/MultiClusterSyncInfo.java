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
package com.alipay.sofa.registry.common.model.metaserver;

import com.google.common.base.Objects;
import java.util.Set;
import org.glassfish.jersey.internal.guava.Sets;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncInfo.java, v 0.1 2022年04月13日 17:09 xiaojian.xj Exp $
 */
public class MultiClusterSyncInfo {

  /** local data center */
  private String dataCenter;

  /** sync remote data center */
  private String remoteDataCenter;

  /** remote meta address, use to get meta leader */
  private String remoteMetaAddress;

  /** multi sync switch */
  private boolean enableSyncDatum;

  /** push switch */
  private boolean enablePush;

  /** allow sync multi group */
  private Set<String> synPublisherGroups = Sets.newHashSet();

  /** allow sync multi dataInfoId */
  private Set<String> syncDataInfoIds = Sets.newHashSet();

  /** dataInfoId will not multi sync, this priority is higher than syncGroups and syncDataInfoIds */
  private Set<String> ignoreDataInfoIds = Sets.newHashSet();

  /** data version */
  private long dataVersion;

  public MultiClusterSyncInfo() {}

  public MultiClusterSyncInfo(String remoteDataCenter, String remoteMetaAddress, long dataVersion) {
    this.remoteDataCenter = remoteDataCenter;
    this.remoteMetaAddress = remoteMetaAddress;
    this.dataVersion = dataVersion;
  }

  public MultiClusterSyncInfo(
      String dataCenter, String remoteDataCenter, String remoteMetaAddress, long dataVersion) {
    this.dataCenter = dataCenter;
    this.remoteDataCenter = remoteDataCenter;
    this.remoteMetaAddress = remoteMetaAddress;
    this.dataVersion = dataVersion;
  }

  public MultiClusterSyncInfo(
      String dataCenter,
      String remoteDataCenter,
      String remoteMetaAddress,
      boolean enableSyncDatum,
      boolean enablePush,
      Set<String> syncDataInfoIds,
      Set<String> synPublisherGroups,
      Set<String> ignoreDataInfoIds,
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
   * Getter method for property <tt>dataVersion</tt>.
   *
   * @return property value of dataVersion
   */
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

  public boolean isEnableSyncDatum() {
    return this.enableSyncDatum;
  }

  /**
   * Setter method for property <tt>enableSyncDatum</tt>.
   *
   * @param enableSyncDatum value to be assigned to property enableSyncDatum
   */
  public void setEnableSyncDatum(boolean enableSyncDatum) {
    this.enableSyncDatum = enableSyncDatum;
  }

  public boolean isEnablePush() {
    return this.enablePush;
  }

  /**
   * Setter method for property <tt>enablePush</tt>.
   *
   * @param enablePush value to be assigned to property enablePush
   */
  public void setEnablePush(boolean enablePush) {
    this.enablePush = enablePush;
  }

  /**
   * Getter method for property <tt>synPublisherGroups</tt>.
   *
   * @return property value of synPublisherGroups
   */
  public Set<String> getSynPublisherGroups() {
    return synPublisherGroups;
  }

  /**
   * Setter method for property <tt>synPublisherGroups</tt>.
   *
   * @param synPublisherGroups value to be assigned to property synPublisherGroups
   */
  public void setSynPublisherGroups(Set<String> synPublisherGroups) {
    this.synPublisherGroups = synPublisherGroups;
  }

  /**
   * Getter method for property <tt>syncDataInfoIds</tt>.
   *
   * @return property value of syncDataInfoIds
   */
  public Set<String> getSyncDataInfoIds() {
    return syncDataInfoIds;
  }

  /**
   * Setter method for property <tt>syncDataInfoIds</tt>.
   *
   * @param syncDataInfoIds value to be assigned to property syncDataInfoIds
   */
  public void setSyncDataInfoIds(Set<String> syncDataInfoIds) {
    this.syncDataInfoIds = syncDataInfoIds;
  }

  /**
   * Getter method for property <tt>ignoreDataInfoIds</tt>.
   *
   * @return property value of ignoreDataInfoIds
   */
  public Set<String> getIgnoreDataInfoIds() {
    return ignoreDataInfoIds;
  }

  /**
   * Setter method for property <tt>ignoreDataInfoIds</tt>.
   *
   * @param ignoreDataInfoIds value to be assigned to property ignoreDataInfoIds
   */
  public void setIgnoreDataInfoIds(Set<String> ignoreDataInfoIds) {
    this.ignoreDataInfoIds = ignoreDataInfoIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MultiClusterSyncInfo that = (MultiClusterSyncInfo) o;
    return Objects.equal(dataCenter, that.dataCenter)
        && Objects.equal(remoteDataCenter, that.remoteDataCenter);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dataCenter, remoteDataCenter);
  }

  @Override
  public String toString() {
    return "MultiClusterSyncInfo{"
        + "dataCenter='"
        + dataCenter
        + '\''
        + ", remoteDataCenter='"
        + remoteDataCenter
        + '\''
        + ", remoteMetaAddress='"
        + remoteMetaAddress
        + '\''
        + ", enableSyncDatum="
        + enableSyncDatum
        + '\''
        + ", enablePush="
        + enablePush
        + '\''
        + ", enablePush="
        + enablePush
        + ", synPublisherGroups="
        + synPublisherGroups
        + ", syncDataInfoIds="
        + syncDataInfoIds
        + ", ignoreDataInfoIds="
        + ignoreDataInfoIds
        + ", dataVersion="
        + dataVersion
        + '}';
  }
}
