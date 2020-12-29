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

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 14:24 yuzhi.lyz Exp $
 */
public class DataSlotDiffDataInfoIdRequest implements Serializable {
    private final long        slotTableEpoch;
    // all dataInfoIds, diff which was removed
    private final Set<String> allDataInfoIds;
    private final int         slotId;

    public DataSlotDiffDataInfoIdRequest(long slotTableEpoch, int slotId, Set<String> allDataInfoIds) {
        this.slotTableEpoch = slotTableEpoch;
        this.slotId = slotId;
        this.allDataInfoIds = allDataInfoIds;
    }

    /**
     * Getter method for property <tt>slotId</tt>.
     * @return property value of slotId
     */
    public int getSlotId() {
        return slotId;
    }

    /**
     * Getter method for property <tt>slotTableEpoch</tt>.
     * @return property value of slotTableEpoch
     */
    public long getSlotTableEpoch() {
        return slotTableEpoch;
    }

    /**
     * Getter method for property <tt>allDataInfoIds</tt>.
     * @return property value of allDataInfoIds
     */
    public Set<String> getAllDataInfoIds() {
        return allDataInfoIds;
    }

    @Override
    public String toString() {
        return "DataSlotDiffDataInfoIdRequest{" + ", slotId=" + slotId + ", slotTableEpoch="
               + slotTableEpoch + ", allDataInfoIds=" + allDataInfoIds + '}';
    }
}
