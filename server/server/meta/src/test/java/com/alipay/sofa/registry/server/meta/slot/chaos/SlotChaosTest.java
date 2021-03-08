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
package com.alipay.sofa.registry.server.meta.slot.chaos;

import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTest;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author xiaojian.xj
 * @version $Id: SlotChaosTest.java, v 0.1 2021年02月02日 11:47 xiaojian.xj Exp $
 */
public class SlotChaosTest extends AbstractMetaServerTest {

    private DataServerInjection      dataServerInjection;

    private SlotManager              slotManager;

    @Mock
    private DefaultDataServerManager dataServerManager;

    private ScheduledSlotArranger    scheduledSlotArranger;

    @Mock
    private SlotTableMonitor         slotTableMonitor;

    private int                      dataNodeNum = 20;

    private int                      chaosRandom;

    @BeforeClass
    public static void beforeSlotMigrateChaosTestClass() throws Exception {
        Map<String, String> env = new HashMap<>();

        env.put("data.slot.num", "512");
        env.put("data.slot.replicas", "2");
        env.put("slot.leader.max.move", "2");
        setEnv(env);
    }

    @Before
    public void beforeSlotMigrateChaosTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        makeMetaLeader();

        logger.info("[slot-chaos data-node] size: " + dataNodeNum);
        dataServerInjection = new DataServerInjection(dataNodeNum);

        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new SimpleSlotManager();

        scheduledSlotArranger = new ScheduledSlotArranger(dataServerManager,
                slotManager, slotTableMonitor, metaLeaderService);

        when(slotTableMonitor.isStableTableStable()).thenReturn(true);
        when(dataServerManager.getDataServerMetaInfo()).thenReturn(VersionedList.EMPTY);
        scheduledSlotArranger.postConstruct();
    }

    @Test
    public void testChaos() throws TimeoutException, InterruptedException {
        logger.info("[slot-chaos slot] size: " + slotManager.getSlotNums());

        do {
            chaosRandom = random.nextInt(100);
        } while (chaosRandom < 30);

        makeMetaLeader();
        logger.info("[slot-chaos slot] chaosRandom: " + chaosRandom);

        List<DataNode> running;
        for (int i = 1; i <= chaosRandom; i++) {
            // random up and down
            running = dataServerInjection.inject(i);
            when(dataServerManager.getDataServerMetaInfo())
                    .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), running));
            scheduledSlotArranger.arrangeAsync();
            Thread.sleep(2000);
        }
        for (int i = 0; i < 500; i++) {
            scheduledSlotArranger.arrangeAsync();
            Thread.sleep(20);
        }
        SlotTable finalSlotTable = slotManager.getSlotTable();
        for (CheckEnum checker : CheckEnum.values()) {
            checker.getCheckerAction().doCheck(finalSlotTable);
        }
    }

    public class DataServerInjection {
        private int            dataNodeNum;

        private List<DataNode> dataNodes;

        private List<DataNode> runningDataNodes = new ArrayList<>();

        public DataServerInjection(int dataNodeNum) {
            this.dataNodeNum = dataNodeNum;
            this.dataNodes = randomDataNodes(dataNodeNum);
            logger.info("[slot-chaos data-node] dataNodes: " + dataNodes);
        }

        public List<DataNode> inject(int chaos) {
            InjectionEnum injectType;
            if (chaos == 1 || runningDataNodes.size() == 0) {
                injectType = InjectionEnum.FIRST;
            } else if (chaos == chaosRandom) {
                injectType = InjectionEnum.FINAL;
            } else if (runningDataNodes.size() == dataNodeNum) {
                injectType = InjectionEnum.STOP;
            } else {
                injectType = InjectionEnum.pickInject();
            }

            logger.info("[slot-chaos inject before] chaos: {}, type: {}, size: {}, nodes: {}",
                chaos, injectType, runningDataNodes.size(), runningDataNodes);
            injectType.getInjectionAction().doInject(dataNodes, runningDataNodes);
            logger.info("[slot-chaos inject after] chaos: {}, type: {}, size: {}, nodes: {}",
                chaos, injectType, runningDataNodes.size(), runningDataNodes);
            return runningDataNodes;
        }

    }
}