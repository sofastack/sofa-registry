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
package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.slot.SlotAllocator;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
public class NaiveRebalanceSlotAllocator implements SlotAllocator {

    private SlotManager slotManager;

    private SlotTable   slotTable;

    public NaiveRebalanceSlotAllocator(SlotManager slotManager) {
        this.slotManager = slotManager;
    }

    @Override
    public SlotTable getSlotTable() {
        return slotTable;
    }

    public NaiveRebalanceSlotAllocator execute() {

        return this;
    }
}
