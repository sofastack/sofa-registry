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

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author xiaojian.xj
 * @version $Id: SlotChaosTest.java, v 0.1 2021年02月02日 11:47 xiaojian.xj Exp $
 */
public class SlotChaosTest extends AbstractTest {

    private DataServerInjection dataServerInjection;

    private DefaultSlotManager       defaultSlotManager;

    private SlotManager raftSlotManager;

    private LocalSlotManager         localSlotManager;

    @Mock
    private DefaultDataServerManager dataServerManager;

    private ScheduledSlotArranger scheduledSlotArranger;

    @BeforeClass
    public static void beforeSlotMigrateChaosTestClass() throws Exception {
        Map<String, String> env = new HashMap<>();

        env.put("data.slot.num", "512");
        env.put("data.slot.replicas", "2");
        env.put("slot.frozen.milli", "1000");
        env.put("slot.leader.max.move", "2");
        setEnv(env);
    }

    @Before
    public void beforeSlotMigrateChaosTest() throws Exception {
        MockitoAnnotations.initMocks(this);

        int dataNodeNum = 20;
        logger.info("[slot-chaos data-node] size: {}", dataNodeNum);
        dataServerInjection = new DataServerInjection(dataNodeNum);

        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        raftSlotManager = localSlotManager = new LocalSlotManager(nodeConfig);
        defaultSlotManager = new DefaultSlotManager(localSlotManager, raftSlotManager);
        scheduledSlotArranger = new ScheduledSlotArranger(dataServerManager, localSlotManager, defaultSlotManager);

        scheduledSlotArranger.postConstruct();
    }


    @Test
    public void testChaos() throws TimeoutException, InterruptedException {
        logger.info("[slot-chaos slot] size: {}", localSlotManager.getSlotNums());
        int chaosRandom;

        do {
            chaosRandom = random.nextInt(100);
        } while (chaosRandom < 30);

        makeRaftLeader();
        logger.info("[slot-chaos slot] chaosRandom: {}", chaosRandom);

        List<DataNode> running;
        for (int i = 0; i < chaosRandom; i++) {
            // random up and down
            running = dataServerInjection.inject();
            when(dataServerManager.getClusterMembers()).thenReturn(running);
            scheduledSlotArranger.getTask().wakeup();
            Thread.sleep(3000);
        }

        String frozen = System.getenv("slot.frozen.milli");

        SlotTable finalSlotTable = null;
        for (int i = 0; i <= 100; i++) {
            Thread.sleep(Integer.parseInt(frozen));
            SlotTable slotTable = localSlotManager.getSlotTable();
            for (CheckEnum checker : CheckEnum.values()) {
                checker.getCheckerAction().doCheck(slotTable);
            }

            if (i == 100) {
                finalSlotTable = slotTable;
            }

        }

        for (CheckEnum checker : CheckEnum.values()) {
            Assert.assertTrue(checker.getCheckerAction().doCheck(finalSlotTable));
        }

    }

    public class DataServerInjection {
        private int dataNodeNum;

        private List<DataNode> dataNodes;

        private List<DataNode> runningDataNodes = new ArrayList<>();

        public DataServerInjection(int dataNodeNum) {
            this.dataNodeNum = dataNodeNum;
            this.dataNodes = randomDataNodes(dataNodeNum);
            logger.info("[slot-chaos data-node] dataNodes: {}", dataNodes);
        }

        public List<DataNode> inject() {
            InjectionEnum injectType;
            if (runningDataNodes.size() == 0) {
                injectType = InjectionEnum.START;
            } else if (runningDataNodes.size() == dataNodeNum) {
                injectType = InjectionEnum.STOP;
            } else {
                injectType = InjectionEnum.pickInject();
            }

            injectType.getInjectionAction().doInject(dataNodes, runningDataNodes);
            logger.info("[slot-chaos inject] type: {}, size: {}, after: {}", injectType, runningDataNodes.size(), runningDataNodes);
            return runningDataNodes;
        }


    }
}