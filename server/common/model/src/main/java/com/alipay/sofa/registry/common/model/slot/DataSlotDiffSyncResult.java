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

import com.alipay.sofa.registry.common.model.store.Publisher;

import java.util.List;
import java.util.Map;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 17:04 yuzhi.lyz Exp $
 */
public class DataSlotDiffSyncResult {
    private long                         slotTableEpoch;
    // contains all the updated/added publishers
    private boolean                      hasRemain;
    private Map<String, List<Publisher>> updatedPublishers;
    private List<String>                 removedDataInfoIds;
    private Map<String, List<String>>    removedPublishers;

    // if from session, return the sessionProcessId for lease
    private String                       sessionProcessId;

    public DataSlotDiffSyncResult(Map<String, List<Publisher>> updatedPublishers,
                                  List<String> removedDataInfoIds,
                                  Map<String, List<String>> removedPublishers) {
        this.updatedPublishers = updatedPublishers;
        this.removedDataInfoIds = removedDataInfoIds;
        this.removedPublishers = removedPublishers;
    }

    public boolean isHasRemain() {
        return hasRemain;
    }

    public void setHasRemain(boolean hasRemain) {
        this.hasRemain = hasRemain;
    }

    /**
     * Getter method for property <tt>slotTableEpoch</tt>.
     * @return property value of slotTableEpoch
     */
    public long getSlotTableEpoch() {
        return slotTableEpoch;
    }

    /**
     * Setter method for property <tt>slotTableEpoch</tt>.
     * @param slotTableEpoch value to be assigned to property slotTableEpoch
     */
    public void setSlotTableEpoch(long slotTableEpoch) {
        this.slotTableEpoch = slotTableEpoch;
    }

    /**
     * Getter method for property <tt>updatedPublishers</tt>.
     * @return property value of updatedPublishers
     */
    public Map<String, List<Publisher>> getUpdatedPublishers() {
        return updatedPublishers;
    }

    /**
     * Setter method for property <tt>updatedPublishers</tt>.
     * @param updatedPublishers value to be assigned to property updatedPublishers
     */
    public void setUpdatedPublishers(Map<String, List<Publisher>> updatedPublishers) {
        this.updatedPublishers = updatedPublishers;
    }

    /**
     * Getter method for property <tt>removedPublishers</tt>.
     * @return property value of removedPublishers
     */
    public Map<String, List<String>> getRemovedPublishers() {
        return removedPublishers;
    }

    /**
     * Setter method for property <tt>removedPublishers</tt>.
     * @param removedPublishers value to be assigned to property removedPublishers
     */
    public void setRemovedPublishers(Map<String, List<String>> removedPublishers) {
        this.removedPublishers = removedPublishers;
    }

    /**
     * Getter method for property <tt>removedDataInfoIds</tt>.
     * @return property value of removedDataInfoIds
     */
    public List<String> getRemovedDataInfoIds() {
        return removedDataInfoIds;
    }

    /**
     * Setter method for property <tt>removedDataInfoIds</tt>.
     * @param removedDataInfoIds value to be assigned to property removedDataInfoIds
     */
    public void setRemovedDataInfoIds(List<String> removedDataInfoIds) {
        this.removedDataInfoIds = removedDataInfoIds;
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
}
