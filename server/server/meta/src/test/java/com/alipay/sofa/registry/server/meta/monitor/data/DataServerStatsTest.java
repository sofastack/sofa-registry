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
package com.alipay.sofa.registry.server.meta.monitor.data;

import static org.junit.Assert.*;

import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.FollowerSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class DataServerStatsTest {

  private Logger logger = LoggerFactory.getLogger(DataServerStatsTest.class);

  @Test
  public void testSimpleCase() {
    DataServerStats dataServerStats =
        new DataServerStats(
            "127.0.0.1",
            1,
            Lists.newArrayList(
                new LeaderSlotStatus(2, 1, "127.0.0.1", BaseSlotStatus.LeaderStatus.UNHEALTHY),
                new FollowerSlotStatus(
                    1,
                    1,
                    "127.0.0.1",
                    System.currentTimeMillis() - 1,
                    System.currentTimeMillis())));
    Assert.assertEquals("127.0.0.1", dataServerStats.getDataServer());
    Assert.assertEquals(1, dataServerStats.getSlotTableEpoch());
    Assert.assertEquals(2, dataServerStats.getSlotStatus().size());
    logger.info("{}", dataServerStats);
  }
}
