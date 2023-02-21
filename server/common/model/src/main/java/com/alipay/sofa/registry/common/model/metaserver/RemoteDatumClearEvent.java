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

import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.io.Serializable;

/**
 * @author xiaojian.xj
 * @version : RemoteDatumClearEvent.java, v 0.1 2022年09月08日 17:05 xiaojian.xj Exp $
 */
public class RemoteDatumClearEvent implements Serializable {

  private final String remoteDataCenter;

  private final DatumType datumType;

  private final String dataInfoId;

  private final String group;

  private RemoteDatumClearEvent(
      String remoteDataCenter, DatumType datumType, String dataInfoId, String group) {
    this.remoteDataCenter = remoteDataCenter;
    this.datumType = datumType;
    this.dataInfoId = dataInfoId;
    this.group = group;
  }

  public static RemoteDatumClearEvent dataInfoIdEvent(String remoteDataCenter, String dataInfoId) {
    ParaCheckUtil.checkNotBlank(remoteDataCenter, "remoteDataCenter");
    ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
    return new RemoteDatumClearEvent(remoteDataCenter, DatumType.DATA_INFO_ID, dataInfoId, null);
  }

  public static RemoteDatumClearEvent groupEvent(String remoteDataCenter, String group) {
    ParaCheckUtil.checkNotBlank(remoteDataCenter, "remoteDataCenter");
    ParaCheckUtil.checkNotBlank(group, "group");
    return new RemoteDatumClearEvent(remoteDataCenter, DatumType.GROUP, null, group);
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
   * Getter method for property <tt>datumType</tt>.
   *
   * @return property value of datumType
   */
  public DatumType getDatumType() {
    return datumType;
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
   * Getter method for property <tt>group</tt>.
   *
   * @return property value of group
   */
  public String getGroup() {
    return group;
  }

  public enum DatumType {
    DATA_INFO_ID,
    GROUP,
    ;
  }

  @Override
  public String toString() {
    return "RemoteDatumClearEvent{"
        + "remoteDataCenter='"
        + remoteDataCenter
        + '\''
        + ", datumType="
        + datumType
        + ", dataInfoId='"
        + dataInfoId
        + '\''
        + ", group='"
        + group
        + '\''
        + '}';
  }
}
