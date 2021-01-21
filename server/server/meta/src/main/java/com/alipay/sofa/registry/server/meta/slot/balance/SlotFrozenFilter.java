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
package com.alipay.sofa.registry.server.meta.slot.balance;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chen.zhu
 * <p>
 * Jan 18, 2021
 */
public class SlotFrozenFilter implements Filter<Integer> {

    private static final Logger logger              = LoggerFactory
                                                        .getLogger(SlotFrozenFilter.class);

    private DefaultSlotBalancer slotBalancer;

    // "final" instead of "static final" to make the config dynamic and flexible
    private final long   leastChangeInterval = Long.getLong("slot.frozen.milli",
                                                        TimeUnit.SECONDS.toMillis(5));

    public SlotFrozenFilter(DefaultSlotBalancer slotBalancer) {
        this.slotBalancer = slotBalancer;
    }

    @Override
    public List<Integer> filter(List<Integer> candidates) {
        return candidates
                .stream()
                .filter(slotId->{
                    Slot slot = slotBalancer.getPrevSlotTable().getSlot(slotId);
                    if(slot == null) {
                        return false;
                    }
                    long epoch = slot.getLeaderEpoch();
                    long lastUpdate = DatumVersionUtil.getRealTimestamp(epoch);
                    boolean result = System.currentTimeMillis() - lastUpdate > leastChangeInterval;
                    if(!result) {
                        if(logger.isInfoEnabled()) {
                            logger.info("[filter] slot[{}] cannot balance for update too frequent," +
                                            " current - lastUpdate ({} - {} = {}ms), leastChangeInterval ({} ms)",
                                    slotId,
                                    System.currentTimeMillis(), lastUpdate, System.currentTimeMillis() - lastUpdate,
                                    leastChangeInterval);
                        }
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }
}
