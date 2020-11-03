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
import java.util.*;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 10:08 yuzhi.lyz Exp $
 */
public final class SlotTable implements Serializable {
    private final long             epoch;
    private final Map<Short, Slot> slots;

    public SlotTable(long epoch, Map<Short, Slot> slots) {
        this.epoch = epoch;
        this.slots = Collections.unmodifiableSortedMap(new TreeMap<>(slots));
    }

    public static Map<Short, Slot> getSlotsAdded(SlotTable from, SlotTable to) {
        Map<Short, Slot> m = new TreeMap<>(to.slots);
        from.slots.keySet().forEach(slotId -> {
            m.remove(slotId);
        });
        return m;
    }

    public static Map<Short, Slot> getSlotsDeled(SlotTable from, SlotTable to) {
        Map<Short, Slot> m = new TreeMap<>(from.slots);
        to.slots.keySet().forEach(slotId -> {
            m.remove(slotId);
        });
        return m;
    }

    public static Map<Short, Slot> getSlotUpdated(SlotTable from, SlotTable to) {
        Map<Short, Slot> m = new TreeMap<>();
        from.slots.forEach((slotId, fromSlot) -> {
            final Slot toSlot = to.slots.get(slotId);
            if (toSlot != null && !fromSlot.equals(toSlot)) {
                m.put(slotId, toSlot);
            }
        });
        return m;
    }

    public Set<Short> getSlotIds() {
        return new TreeSet<>(slots.keySet());
    }

    public Slot getSlot(short slotId) {
        return slots.get(slotId);
    }

    /**
     * Getter method for property <tt>epoch</tt>.
     * @return property value of epoch
     */
    public long getEpoch() {
        return epoch;
    }

    @Override
    public String toString() {
        return "SlotTable{" +
                "epoch=" + epoch +
                ", slotsNum=" + slots.size() +
                ", slotKeys=" + slots.keySet() +
                '}';
    }
}
