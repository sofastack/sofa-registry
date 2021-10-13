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

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.util.StringFormatter;

/**
 * request to get specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataRequest.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class GetDataRequest extends AbstractSlotRequest {

  private static final long serialVersionUID = 8133437572926931258L;

  private final String dataInfoId;

  /** if datacenter is null, means all datacenters */
  private final String dataCenter;

  private String[] acceptEncodes;

  public GetDataRequest(
      ProcessId sessionProcessId, String dataInfoId, String dataCenter, int slotId) {
    super(slotId, sessionProcessId);
    this.dataInfoId = dataInfoId;
    this.dataCenter = dataCenter;
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
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "GetData:{},{},{},{},{}",
        dataInfoId,
        dataCenter,
        getSlotId(),
        getSlotLeaderEpoch(),
        getSlotTableEpoch());
  }

  public String[] getAcceptEncodes() {
    return acceptEncodes;
  }

  public void setAcceptEncodes(String[] encodes) {
    acceptEncodes = encodes;
  }
}
