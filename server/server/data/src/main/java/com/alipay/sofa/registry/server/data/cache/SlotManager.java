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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.Publisher;

import java.util.List;
import java.util.Map;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 10:46 yuzhi.lyz Exp $
 */
public interface SlotManager {
    SlotAccess checkSlotAccess(String dataInfoId, long srcSlotEpoch);

    int slotOf(String dataInfoId);

    boolean updateSlotTable(SlotTable slotTable);

    void addSlotChangeListener(SlotChangeListener listener);

    interface SlotChangeListener {
        void onSlotAdd(int slotId, Slot.Role role);

        void onSlotRemove(int slotId, Slot.Role role);
    }

    void setSlotDatumStorageProvider(SlotDatumStorageProvider provider);

    interface SlotDatumStorageProvider {
        Map<String, DatumSummary> getDatumSummary(int slotId, String dataCenter, String targetIpAddress);

        void merge(int slotId, String dataCenter, Map<String, Datum> puts, Map<String, List<String>> remove);

    }

}
