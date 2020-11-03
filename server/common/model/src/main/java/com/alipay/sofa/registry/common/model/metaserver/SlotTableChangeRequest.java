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

import com.alipay.sofa.registry.common.model.slot.SlotTable;

import java.io.Serializable;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-02 17:30 yuzhi.lyz Exp $
 */
public final class SlotTableChangeRequest implements Serializable {
    private SlotTable slotTable;

    /**
     * Getter method for property <tt>slotTable</tt>.
     * @return property value of slotTable
     */
    public SlotTable getSlotTable() {
        return slotTable;
    }

    /**
     * Setter method for property <tt>slotTable</tt>.
     * @param slotTable value to be assigned to property slotTable
     */
    public void setSlotTable(SlotTable slotTable) {
        this.slotTable = slotTable;
    }

    @Override
    public String toString() {
        return "SlotTableChangeRequest{" +
                "slotTable=" + slotTable +
                '}';
    }
}
