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
package com.alipay.sofa.registry.common.model.console;

import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : MultiSegmentSyncSwitch.java, v 0.1 2022年07月20日 11:07 xiaojian.xj Exp $
 */
public class MultiSegmentSyncSwitch {

  /** multi sync switch */
  private final boolean multiSync;

  /** multi sync switch */
  private final boolean multiPush;

  /** remote dataCenter */
  private final String remoteDataCenter;

  /** allow sync multi group */
  private final Set<String> synPublisherGroups;

  /** allow sync multi dataInfoId */
  private final Set<String> syncDataInfoIds;

  /** dataInfoId will not multi sync, this priority is higher than syncGroups and syncDataInfoIds */
  private final Set<String> ignoreDataInfoIds;

  private final long dataVersion;

  public boolean isMultiSync() {
    return this.multiSync;
  }

  public boolean isMultiPush() {
    return this.multiPush;
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
   * Getter method for property <tt>synPublisherGroups</tt>.
   *
   * @return property value of synPublisherGroups
   */
  public Set<String> getSynPublisherGroups() {
    return synPublisherGroups;
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
   * Getter method for property <tt>ignoreDataInfoIds</tt>.
   *
   * @return property value of ignoreDataInfoIds
   */
  public Set<String> getIgnoreDataInfoIds() {
    return ignoreDataInfoIds;
  }

  /**
   * Getter method for property <tt>dataVersion</tt>.
   *
   * @return property value of dataVersion
   */
  public long getDataVersion() {
    return dataVersion;
  }

  public MultiSegmentSyncSwitch(
      boolean multiSync,
      boolean multiPush,
      String remoteDataCenter,
      Set<String> synPublisherGroups,
      Set<String> syncDataInfoIds,
      Set<String> ignoreDataInfoIds,
      long dataVersion) {
    this.multiSync = multiSync;
    this.multiPush = multiPush;
    this.remoteDataCenter = remoteDataCenter;
    this.synPublisherGroups = synPublisherGroups;
    this.syncDataInfoIds = syncDataInfoIds;
    this.ignoreDataInfoIds = ignoreDataInfoIds;
    this.dataVersion = dataVersion;
  }

  @Override
  public String toString() {
    return "MultiSegmentSyncSwitch{"
        + "multiSync="
        + multiSync
        + ", multiPush="
        + multiPush
        + ", remoteDataCenter='"
        + remoteDataCenter
        + '\''
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
