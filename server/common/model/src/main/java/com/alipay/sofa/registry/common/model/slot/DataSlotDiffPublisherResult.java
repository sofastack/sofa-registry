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
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.MapUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 17:04 yuzhi.lyz Exp $
 */
public class DataSlotDiffPublisherResult implements Serializable {
  private long slotTableEpoch;
  private final Map<String, List<Publisher>> updatedPublishers;
  private final Map<String, List<String>> removedPublishers;
  // if from session, return the sessionProcessId for lease
  private ProcessId sessionProcessId;
  // contains all the updated/added publishers
  private final boolean hasRemain;

  public DataSlotDiffPublisherResult(
      boolean hasRemain,
      Map<String, List<Publisher>> updatedPublishers,
      Map<String, List<String>> removedPublishers) {
    this.updatedPublishers = Maps.newHashMap(updatedPublishers);
    this.removedPublishers = Maps.newHashMap(removedPublishers);
    this.hasRemain = hasRemain;
  }

  public boolean isHasRemain() {
    return hasRemain;
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

  /**
   * Getter method for property <tt>updatedPublishers</tt>.
   *
   * @return property value of updatedPublishers
   */
  public Map<String, List<Publisher>> getUpdatedPublishers() {
    return Collections.unmodifiableMap(updatedPublishers);
  }

  /**
   * Getter method for property <tt>removedPublishers</tt>.
   *
   * @return property value of removedPublishers
   */
  public Map<String, List<String>> getRemovedPublishers() {
    return Collections.unmodifiableMap(removedPublishers);
  }

  public int getRemovedPublishersCount() {
    int count = 0;
    for (List<String> list : removedPublishers.values()) {
      count += list.size();
    }
    return count;
  }

  public int getUpdatedPublishersCount() {
    int count = 0;
    for (List<Publisher> list : updatedPublishers.values()) {
      count += list.size();
    }
    return count;
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
    return MapUtils.isEmpty(updatedPublishers) && MapUtils.isEmpty(removedPublishers);
  }

  public Set<String> syncDataInfoIds() {
    Set<String> ret = Sets.newHashSet();
    ret.addAll(updatedPublishers.keySet());
    ret.addAll(removedPublishers.keySet());
    return ret;
  }
}
