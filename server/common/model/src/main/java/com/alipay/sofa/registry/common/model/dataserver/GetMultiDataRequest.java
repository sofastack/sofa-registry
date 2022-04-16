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
import java.io.Serializable;
import java.util.Map;

/**
 * @author xiaojian.xj
 * @version : GetMultiDataRequest.java, v 0.1 2022年06月20日 15:52 xiaojian.xj Exp $
 */
public class GetMultiDataRequest implements Serializable {
  private static final long serialVersionUID = -4353547075497551438L;

  private final ProcessId sessionProcessId;

  private final int slotId;

  private final String dataInfoId;

  private final String[] acceptEncodes;

  private final Map<String, Long> slotTableEpochs;

  private final Map<String, Long> slotLeaderEpochs;

  public GetMultiDataRequest(
      ProcessId sessionProcessId,
      int slotId,
      String dataInfoId,
      String[] acceptEncodes,
      Map<String, Long> slotTableEpochs,
      Map<String, Long> slotLeaderEpochs) {
    this.sessionProcessId = sessionProcessId;
    this.slotId = slotId;
    this.dataInfoId = dataInfoId;
    this.acceptEncodes = acceptEncodes;
    this.slotTableEpochs = slotTableEpochs;
    this.slotLeaderEpochs = slotLeaderEpochs;
  }

  /**
   * Getter method for property <tt>sessionProcessId</tt>.
   *
   * @return property value of sessionProcessId
   */
  public ProcessId getSessionProcessId() {
    return sessionProcessId;
  }

  /**
   * Getter method for property <tt>slotId</tt>.
   *
   * @return property value of slotId
   */
  public int getSlotId() {
    return slotId;
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
   * Getter method for property <tt>acceptEncodes</tt>.
   *
   * @return property value of acceptEncodes
   */
  public String[] getAcceptEncodes() {
    return acceptEncodes;
  }

  /**
   * Getter method for property <tt>slotTableEpochs</tt>.
   *
   * @return property value of slotTableEpochs
   */
  public Map<String, Long> getSlotTableEpochs() {
    return slotTableEpochs;
  }

  /**
   * Getter method for property <tt>slotLeaderEpochs</tt>.
   *
   * @return property value of slotLeaderEpochs
   */
  public Map<String, Long> getSlotLeaderEpochs() {
    return slotLeaderEpochs;
  }
}
