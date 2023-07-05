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
package com.alipay.sofa.registry.server.meta.slot.status;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.SlotTableStatusResponse;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.balance.BalancePolicy;
import com.alipay.sofa.registry.server.meta.slot.balance.NaiveBalancePolicy;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.util.MathUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: SlotTableStatusService.java, v 0.1 2021年07月02日 7:42 PM xiaojian.xj Exp $
 */
public class SlotTableStatusService {

  @Autowired private SlotManager slotManager;

  @Autowired private SlotTableMonitor slotTableMonitor;

  @Autowired private DataServerManager dataServerManager;

  @Autowired private ScheduledSlotArranger slotArranger;

  public SlotTableStatusResponse getSlotTableStatus() {
    boolean isSlotStable = slotTableMonitor.isStableTableStable();
    SlotTable slotTable = slotManager.getSlotTable();
    Map<String, Integer> leaderCounter = SlotTableUtils.getSlotTableLeaderCount(slotTable);
    Map<String, Integer> followerCounter = SlotTableUtils.getSlotTableSlotCount(slotTable);
    boolean isLeaderSlotBalanced =
        isSlotTableLeaderBalanced(
            leaderCounter, dataServerManager.getDataServerMetaInfo().getClusterMembers());
    boolean isFollowerSlotBalanced =
        isSlotTableFollowerBalanced(
            followerCounter, dataServerManager.getDataServerMetaInfo().getClusterMembers());
    return new SlotTableStatusResponse(
        slotTable.getEpoch(),
        isLeaderSlotBalanced,
        isFollowerSlotBalanced,
        isSlotStable,
        slotArranger.isSlotTableProtectionMode(),
        leaderCounter,
        followerCounter);
  }

  public boolean isSlotTableLeaderBalanced(
      Map<String, Integer> leaderCounter, List<DataNode> dataNodes) {

    if (CollectionUtils.isEmpty(dataNodes)) {
      return false;
    }

    BalancePolicy balancePolicy = new NaiveBalancePolicy();
    int expectedLeaderTotal = slotManager.getSlotNums();
    if (leaderCounter.values().stream().mapToInt(Integer::intValue).sum() < expectedLeaderTotal) {
      return false;
    }
    int leaderHighAverage = MathUtils.divideCeil(expectedLeaderTotal, dataNodes.size());
    int leaderHighWaterMark = balancePolicy.getHighWaterMarkSlotLeaderNums(leaderHighAverage);

    for (DataNode dataNode : dataNodes) {
      String dataIp = dataNode.getIp();
      if (leaderCounter.get(dataIp) == null) {
        return false;
      }
      int leaderCount = leaderCounter.getOrDefault(dataIp, 0);
      if (leaderCount > leaderHighWaterMark) {
        return false;
      }
    }
    return true;
  }

  public boolean isSlotTableFollowerBalanced(
      Map<String, Integer> followerCounter, List<DataNode> dataNodes) {

    if (CollectionUtils.isEmpty(dataNodes)) {
      return false;
    }
    BalancePolicy balancePolicy = new NaiveBalancePolicy();
    int expectedFollowerTotal = slotManager.getSlotNums() * (slotManager.getSlotReplicaNums() - 1);
    if (slotManager.getSlotReplicaNums() < dataNodes.size()) {
      if (followerCounter.values().stream().mapToInt(Integer::intValue).sum()
          < expectedFollowerTotal) {
        return false;
      }
    }
    int followerHighAverage = MathUtils.divideCeil(expectedFollowerTotal, dataNodes.size());
    int followerHighWaterMark = balancePolicy.getHighWaterMarkSlotFollowerNums(followerHighAverage);

    for (DataNode dataNode : dataNodes) {
      String dataIp = dataNode.getIp();
      int followerCount = followerCounter.getOrDefault(dataIp, 0);
      if (followerCount > followerHighWaterMark) {
        return false;
      }
    }
    return true;
  }

  /**
   * Setter method for property <tt>slotManager</tt>.
   *
   * @param slotManager value to be assigned to property slotManager
   * @return SlotTableStatusService
   */
  @VisibleForTesting
  public SlotTableStatusService setSlotManager(SlotManager slotManager) {
    this.slotManager = slotManager;
    return this;
  }

  /**
   * Setter method for property <tt>slotTableMonitor</tt>.
   *
   * @param slotTableMonitor value to be assigned to property slotTableMonitor
   * @return SlotTableStatusService
   */
  @VisibleForTesting
  public SlotTableStatusService setSlotTableMonitor(SlotTableMonitor slotTableMonitor) {
    this.slotTableMonitor = slotTableMonitor;
    return this;
  }

  /**
   * Setter method for property <tt>dataServerManager</tt>.
   *
   * @param dataServerManager value to be assigned to property dataServerManager
   * @return SlotTableStatusService
   */
  @VisibleForTesting
  public SlotTableStatusService setDataServerManager(DataServerManager dataServerManager) {
    this.dataServerManager = dataServerManager;
    return this;
  }

  /**
   * Setter method for property <tt>slotArranger</tt>.
   *
   * @param slotArranger value to be assigned to property slotArranger
   * @return SlotTableStatusService
   */
  @VisibleForTesting
  public SlotTableStatusService setSlotArranger(ScheduledSlotArranger slotArranger) {
    this.slotArranger = slotArranger;
    return this;
  }
}
