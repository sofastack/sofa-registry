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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author xiaojian.xj
 * @version $Id: SlotChaosTest.java, v 0.1 2021年02月02日 11:47 xiaojian.xj Exp $
 */
public class SlotChaosTest extends AbstractMetaServerTestBase {

  private DataServerInjection dataServerInjection;

  private SimpleSlotManager slotManager;

  @Mock private DefaultDataServerManager dataServerManager;

  private ScheduledSlotArranger scheduledSlotArranger;

  @Mock private SlotTableMonitor slotTableMonitor;

  private int dataNodeNum = 20;

  private int chaosRandom;

  @Before
  public void beforeSlotMigrateChaosTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    makeMetaLeader();

    logger.info("[slot-chaos data-node] size: " + dataNodeNum);
    dataServerInjection = new DataServerInjection(dataNodeNum);

    NodeConfig nodeConfig = mock(NodeConfig.class);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
    slotManager = new SimpleSlotManager();
    slotManager.setSlotNums(256);
    slotManager.setSlotReplicas(2);
    scheduledSlotArranger =
        new ScheduledSlotArranger(
            dataServerManager, slotManager, slotTableMonitor, metaLeaderService, metaServerConfig);

    when(slotTableMonitor.isStableTableStable()).thenReturn(true);
    when(dataServerManager.getDataServerMetaInfo()).thenReturn(VersionedList.EMPTY);
  }

  @Test
  public void testChaosLoop() throws Exception {
    for (int i = 0; i < 100; i++) {
      testChaos();
    }
  }

  @Test
  public void testChaos() throws Exception {
    logger.info("[slot-chaos slot] size: " + slotManager.getSlotNums());

    chaosRandom = random.nextInt(20) + 30;

    makeMetaLeader();
    logger.info("[slot-chaos slot] chaosRandom: " + chaosRandom);

    List<DataNode> running = null;

    for (int i = 1; i <= chaosRandom; i++) {
      // random up and down
      running = dataServerInjection.inject(i);
      when(dataServerManager.getDataServerMetaInfo())
          .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), running));
      scheduledSlotArranger.arrangeSync();
    }
    logger.info("[slot-chaos slot] begin balance");
    int count = 0;
    for (; count < slotManager.getSlotNums() * 4; count++) {
      if (!scheduledSlotArranger.arrangeSync()) {
        break;
      }
    }
    Assert.assertFalse("unbalance after:" + count, scheduledSlotArranger.arrangeSync());
    SlotTable finalSlotTable = slotManager.getSlotTable();
    List<String> datas = NodeUtils.transferNodeToIpList(running);
    for (CheckEnum checker : CheckEnum.values()) {
      checker
          .getCheckerAction()
          .doCheck(
              finalSlotTable, datas, slotManager.getSlotNums(), slotManager.getSlotReplicaNums());
    }
  }

  public class DataServerInjection {
    private int dataNodeNum;

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

      logger.info(
          "[slot-chaos inject before] chaos: {}, type: {}, size: {}, nodes: {}",
          chaos,
          injectType,
          runningDataNodes.size(),
          runningDataNodes);
      injectType.getInjectionAction().doInject(dataNodes, runningDataNodes);
      logger.info(
          "[slot-chaos inject after] chaos: {}, type: {}, size: {}, nodes: {}",
          chaos,
          injectType,
          runningDataNodes.size(),
          runningDataNodes);
      return runningDataNodes;
    }
  }
}
