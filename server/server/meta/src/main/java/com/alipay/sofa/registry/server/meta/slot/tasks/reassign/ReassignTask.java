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
package com.alipay.sofa.registry.server.meta.slot.tasks.reassign;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.DataNodeComparator;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class ReassignTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReassignTask.class);

    private DataServerManager   dataServerManager;

    private LocalSlotManager    localSlotManager;

    private SlotManager         raftSlotManager;

    public ReassignTask(LocalSlotManager localSlotManager, SlotManager raftSlotManager,
                        DataServerManager dataServerManager) {
        this.dataServerManager = dataServerManager;
        this.localSlotManager = localSlotManager;
        this.raftSlotManager = raftSlotManager;
    }

    @Override
    public void run() {
        if (logger.isInfoEnabled()) {
            logger.info("[run] begin reassign slots");
        }
        SlotTable prevSlotTable = localSlotManager.getSlotTable();
        List<String> currentDataNodes = NodeUtils.transferNodeToIpList(dataServerManager
            .getClusterMembers());
        DataNodeComparator comparator = new DataNodeComparator(prevSlotTable.getDataServers(),
            currentDataNodes);
        if (!comparator.hasAnyChange()) {
            if (logger.isInfoEnabled()) {
                logger.info("[run] no changes happened, quit");
            }
            return;
        }
        logger.warn("[run][data servers] added: {}, removed: {}", comparator.getAdded(),
            comparator.getRemoved());
        SlotAssigner assigner = new SlotAssigner(prevSlotTable, currentDataNodes, comparator);
        assigner.run();
        SlotTable result = assigner.build();
        raftSlotManager.refresh(result);
    }
}
