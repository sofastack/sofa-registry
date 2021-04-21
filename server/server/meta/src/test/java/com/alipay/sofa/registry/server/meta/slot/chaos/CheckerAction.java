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

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.test.TestUtils;
import com.alipay.sofa.registry.util.MathUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.*;
import java.util.Map.Entry;
import org.junit.Assert;

/**
 * @author xiaojian.xj
 * @version $Id: CheckerAction.java, v 0.1 2021年02月03日 10:25 xiaojian.xj Exp $
 */
public interface CheckerAction {

  boolean doCheck(SlotTable slotTable, List<String> dataNodes, int slotNum, int replicas);

  default Tuple<String, Integer> max(Map<String, Integer> count) {
    Optional<Entry<String, Integer>> max =
        count.entrySet().stream().max((Comparator.comparing(Entry::getValue)));
    return new Tuple<>(max.get().getKey(), max.get().getValue());
  }

  default Tuple<String, Integer> min(Map<String, Integer> count) {
    Optional<Entry<String, Integer>> max =
        count.entrySet().stream().min((Comparator.comparing(Entry::getValue)));
    return new Tuple<>(max.get().getKey(), max.get().getValue());
  }

  default double average(Map<String, Integer> count) {
    OptionalDouble average =
        count.entrySet().stream().mapToInt(entry -> entry.getValue()).average();
    return average.getAsDouble();
  }

  default int sum(Map<String, Integer> count) {
    return (int) count.entrySet().stream().mapToInt(entry -> entry.getValue()).sum();
  }
}

class SlotLeaderChecker implements CheckerAction {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public boolean doCheck(SlotTable slotTable, List<String> dataNodes, int slotNum, int replicas) {

    Map<String, Integer> leaderCount = SlotTableUtils.getSlotTableLeaderCount(slotTable);
    logger.info("[slot leader checker] leaderCount: " + leaderCount);
    String msg =
        StringFormatter.format(
            "datas={},counts={}",
            new TreeSet<String>(dataNodes),
            new TreeSet<String>(leaderCount.keySet()));
    Assert.assertEquals(msg, dataNodes.size(), leaderCount.size());
    Assert.assertTrue(leaderCount.keySet().containsAll(dataNodes));
    Tuple<String, Integer> max = max(leaderCount);
    Tuple<String, Integer> min = min(leaderCount);
    int average = MathUtils.divideCeil(sum(leaderCount), leaderCount.size());
    logger.info(
        "[slot leader checker] max-ip: {}, max-count:{}, min-ip: {}, min-count:{}, average: {}",
        max.getFirst(),
        max.getSecond(),
        min.getFirst(),
        min.getSecond(),
        (int) average);
    TestUtils.assertBalance(slotTable, dataNodes, slotNum, replicas, false, "");
    return max.getSecond() < min.getSecond() * 2;
  }
}

class SlotChecker implements CheckerAction {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public boolean doCheck(SlotTable slotTable, List<String> dataNodes, int slotNum, int replicas) {
    Map<String, Integer> slotCount = SlotTableUtils.getSlotTableSlotCount(slotTable);
    logger.info("[slot checker] slotCount: " + slotCount);
    Assert.assertEquals(dataNodes.size(), slotCount.size());
    Assert.assertTrue(slotCount.keySet().containsAll(dataNodes));
    Tuple<String, Integer> max = max(slotCount);
    Tuple<String, Integer> min = min(slotCount);
    double average = average(slotCount);
    logger.info(
        "[slot checker] max-ip: {}, max-count:{}, min-ip: {}, min-count:{}, average: {}",
        max.getFirst(),
        max.getSecond(),
        min.getFirst(),
        min.getSecond(),
        (int) average);
    TestUtils.assertBalance(slotTable, dataNodes, slotNum, replicas, false, "");
    return max.getSecond() < min.getSecond() * 2;
  }
}

enum CheckEnum {
  SLOT_LEADER_CHECKER(new SlotLeaderChecker()),

  SLOT_CHECKER(new SlotChecker()),
  ;

  private CheckerAction checkerAction;

  CheckEnum(CheckerAction checkerAction) {
    this.checkerAction = checkerAction;
  }

  /**
   * Getter method for property <tt>checkerAction</tt>.
   *
   * @return property value of checkerAction
   */
  public CheckerAction getCheckerAction() {
    return checkerAction;
  }
}
