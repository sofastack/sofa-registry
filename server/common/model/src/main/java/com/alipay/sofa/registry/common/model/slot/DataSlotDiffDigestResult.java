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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 17:04 yuzhi.lyz Exp $
 */
public class DataSlotDiffDigestResult implements Serializable {
  private long slotTableEpoch;
  private final List<String> updatedDataInfoIds;
  private final List<String> addedDataInfoIds;
  private final List<String> removedDataInfoIds;
  // if from session, return the sessionProcessId for lease
  private ProcessId sessionProcessId;

  public DataSlotDiffDigestResult(
      List<String> updatedDataInfoIds,
      List<String> addedDataInfoIds,
      List<String> removedDataInfoIds) {
    this.updatedDataInfoIds = Lists.newArrayList(updatedDataInfoIds);
    this.addedDataInfoIds = Lists.newArrayList(addedDataInfoIds);
    this.removedDataInfoIds = Lists.newArrayList(removedDataInfoIds);
  }

  /**
   * Getter method for property <tt>slotTableEpoch</tt>.
   *
   * @return property value of slotTableEpoch
   */
  public long getSlotTableEpoch() {
    return slotTableEpoch;
  }

  /**
   * Setter method for property <tt>slotTableEpoch</tt>.
   *
   * @param slotTableEpoch value to be assigned to property slotTableEpoch
   */
  public void setSlotTableEpoch(long slotTableEpoch) {
    this.slotTableEpoch = slotTableEpoch;
  }

  public List<String> getUpdatedDataInfoIds() {
    return Collections.unmodifiableList(updatedDataInfoIds);
  }

  /**
   * Getter method for property <tt>removedDataInfoIds</tt>.
   *
   * @return property value of removedDataInfoIds
   */
  public List<String> getRemovedDataInfoIds() {
    return Collections.unmodifiableList(removedDataInfoIds);
  }

  public List<String> getAddedDataInfoIds() {
    return Collections.unmodifiableList(addedDataInfoIds);
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
   * Setter method for property <tt>sessionProcessId</tt>.
   *
   * @param sessionProcessId value to be assigned to property sessionProcessId
   */
  public void setSessionProcessId(ProcessId sessionProcessId) {
    this.sessionProcessId = sessionProcessId;
  }

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(updatedDataInfoIds)
        && CollectionUtils.isEmpty(removedDataInfoIds)
        && CollectionUtils.isEmpty(addedDataInfoIds);
  }

  public int getUpdateAndAddSize() {
    return updatedDataInfoIds.size() + addedDataInfoIds.size();
  }
}
