package com.alipay.sofa.registry.server.meta.monitor.data;

import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.FollowerSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataServerStatsTest {

  private Logger logger = LoggerFactory.getLogger(DataServerStatsTest.class);

  @Test
  public void testSimpleCase() {
    DataServerStats dataServerStats = new DataServerStats("127.0.0.1", 1,
            Lists.newArrayList(new LeaderSlotStatus(2, 1, "127.0.0.1", BaseSlotStatus.LeaderStatus.UNHEALTHY),
                    new FollowerSlotStatus(1, 1, "127.0.0.1", System.currentTimeMillis() - 1, System.currentTimeMillis())));
    Assert.assertEquals("127.0.0.1", dataServerStats.getDataServer());
    Assert.assertEquals(1, dataServerStats.getSlotTableEpoch());
    Assert.assertEquals(2, dataServerStats.getSlotStatus().size());
    logger.info("{}", dataServerStats);
  }
}