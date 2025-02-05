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
package com.alipay.sofa.registry.test;

import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.slot.balance.NaiveBalancePolicy;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.util.MathUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.springframework.beans.BeanUtils;

public final class TestUtils {
  private static final AtomicInteger IP_INT = new AtomicInteger();

  private TestUtils() {}

  public static List<DataNode> createDataNodes(int num) {
    List<DataNode> list = Lists.newArrayList();
    for (int i = 0; i < num; i++) {
      String ip = int2Ip(IP_INT.incrementAndGet());
      list.add(new DataNode(new URL(ip, 9600), getDc()));
    }
    return list;
  }

  public static void assertBalance(
      SlotTable slotTable,
      List<String> dataNodes,
      int slotNum,
      int replicas,
      boolean checkLowLeader,
      String tag) {
    Assert.assertTrue(SlotTableUtils.isValidSlotTable(slotTable));
    Map<String, Integer> leaderCount = SlotTableUtils.getSlotTableLeaderCount(slotTable);
    Map<String, Integer> followerCount = SlotTableUtils.getSlotTableFollowerCount(slotTable);
    Assert.assertEquals(dataNodes.size(), leaderCount.size());
    Assert.assertTrue(leaderCount.keySet().containsAll(dataNodes));
    if (dataNodes.size() > 1) {
      Assert.assertEquals(dataNodes.size(), followerCount.size());
      Assert.assertTrue(followerCount.keySet().containsAll(dataNodes));
    }

    int leaderHighAvg = MathUtils.divideCeil(slotNum, dataNodes.size());
    leaderHighAvg = new NaiveBalancePolicy().getHighWaterMarkSlotLeaderNums(leaderHighAvg);
    int leaderLowAvg = Math.floorDiv(slotNum, dataNodes.size());
    leaderLowAvg = new NaiveBalancePolicy().getLowWaterMarkSlotLeaderNums(leaderLowAvg);
    for (Map.Entry<String, Integer> e : leaderCount.entrySet()) {
      String k = e.getKey();
      int v = e.getValue();
      Assert.assertTrue(
          StringFormatter.format(
              "{}, slots={}, data={}, ip={}, L={}, High={}, {}",
              tag,
              slotNum,
              dataNodes.size(),
              k,
              v,
              leaderHighAvg,
              slotTable.transfer(null, true)),
          v <= leaderHighAvg);
      if (checkLowLeader) {
        Assert.assertTrue(
            StringFormatter.format(
                "{}, slots={}, data={}, ip={}, L={}, Low={}, {}",
                tag,
                slotNum,
                dataNodes.size(),
                k,
                v,
                leaderLowAvg,
                slotTable.transfer(null, true)),
            v >= leaderLowAvg / 2);
      }
    }

    if (dataNodes.size() <= 3) {
      return;
    }
    int followers = slotNum * (replicas - 1);
    int followerHighAvg = MathUtils.divideCeil(followers, dataNodes.size());
    int followerLowAvg = Math.floorDiv(followers, dataNodes.size());
    for (Map.Entry<String, Integer> e : followerCount.entrySet()) {
      String k = e.getKey();
      int v = e.getValue();
      Assert.assertTrue(
          StringFormatter.format(
              "{}, slots={}, data={}, ip={}, F={}, High={}",
              tag,
              slotNum,
              dataNodes.size(),
              k,
              v,
              followerHighAvg),
          v <= followerHighAvg * 1.5);

      Assert.assertTrue(
          StringFormatter.format(
              "{}, slots={}, data={}, ip={}, F={}, Low={}",
              tag,
              slotNum,
              dataNodes.size(),
              k,
              v,
              followerLowAvg),
          v >= followerLowAvg / 2);
    }
  }

  public static String getDc() {
    return "testDc";
  }

  public static String int2Ip(int ip) {
    StringBuilder builder = new StringBuilder(String.valueOf(ip >>> 24));
    builder.append(".");
    builder.append(String.valueOf((ip & 0X00FFFFFF) >>> 16));
    builder.append(".");
    builder.append(String.valueOf((ip & 0X0000FFFF) >>> 8));
    builder.append(".");
    builder.append(String.valueOf(ip & 0X000000FF));
    return builder.toString();
  }

  public static void assertException(Class<? extends Throwable> eclazz, Runnable runnable) {
    try {
      runnable.run();
      Assert.assertTrue(false);
    } catch (Throwable exception) {
      Assert.assertEquals(exception.getClass(), eclazz);
    }
  }

  public static SlotTable newTable_0_1(long tableEpoch, long leaderEpoch) {
    Slot slot0 = createSelfLeader(0, leaderEpoch);
    Slot slot1 = createSelfLeader(1, leaderEpoch);
    return new SlotTable(tableEpoch, Lists.newArrayList(slot0, slot1));
  }

  public static Slot createLeader(int slotId, long leaderEpoch, String address) {
    return new Slot(slotId, address, leaderEpoch, Lists.newArrayList("xxx"));
  }

  public static Slot createSelfLeader(int slotId, long leaderEpoch) {
    return createLeader(slotId, leaderEpoch, ServerEnv.IP);
  }

  public static class MockMultiClusterSyncRepository implements MultiClusterSyncRepository {

    private final Map<String, MultiClusterSyncInfo> map = Maps.newHashMap();
    /**
     * insert
     *
     * @param syncInfo
     * @return
     */
    @Override
    public synchronized boolean insert(MultiClusterSyncInfo syncInfo) {
      return map.putIfAbsent(syncInfo.getRemoteDataCenter(), syncInfo) == null;
    }

    /**
     * update with cas
     *
     * @param syncInfo
     * @param expectVersion
     * @return
     */
    @Override
    public synchronized boolean update(MultiClusterSyncInfo syncInfo, long expectVersion) {
      MultiClusterSyncInfo multiClusterSyncInfo = map.get(syncInfo.getRemoteDataCenter());

      if (multiClusterSyncInfo != null && multiClusterSyncInfo.getDataVersion() == expectVersion) {
        map.put(syncInfo.getRemoteDataCenter(), syncInfo);
        return true;
      }
      return false;
    }

    /** query MultiClusterSyncInfo */
    @Override
    public synchronized Set<MultiClusterSyncInfo> queryLocalSyncInfos() {
      return Sets.newHashSet(map.values());
    }

    /**
     * remove provideData
     *
     * @param remoteDataCenter
     * @param dataVersion
     */
    @Override
    public synchronized int remove(String remoteDataCenter, long dataVersion) {
      MultiClusterSyncInfo multiClusterSyncInfo = map.get(remoteDataCenter);
      if (multiClusterSyncInfo == null || multiClusterSyncInfo.getDataVersion() != dataVersion) {
        return 0;
      }
      map.remove(remoteDataCenter);
      return 1;
    }

    @Override
    public synchronized MultiClusterSyncInfo query(String remoteDataCenter) {
      MultiClusterSyncInfo multiClusterSyncInfo = map.get(remoteDataCenter);
      return of(multiClusterSyncInfo);
    }

    private MultiClusterSyncInfo of(MultiClusterSyncInfo multiClusterSyncInfo) {
      if (multiClusterSyncInfo == null) {
        return null;
      }
      MultiClusterSyncInfo ret = new MultiClusterSyncInfo();
      BeanUtils.copyProperties(multiClusterSyncInfo, ret);
      return ret;
    }
  }
}
