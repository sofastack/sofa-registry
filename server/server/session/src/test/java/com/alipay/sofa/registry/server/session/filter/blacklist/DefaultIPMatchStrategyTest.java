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
package com.alipay.sofa.registry.server.session.filter.blacklist;

import com.alipay.sofa.registry.server.session.provideData.FetchBlackListService;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

public class DefaultIPMatchStrategyTest {
  @Test
  public void test() {
    DefaultIPMatchStrategy strategy = new DefaultIPMatchStrategy();
    FetchBlackListService mgr = new FetchBlackListService();
    strategy.setFetchBlackListService(mgr);
    mgr.getBlacklistConfigList()
        .add(getIpConfig(BlacklistConstants.FORBIDDEN_PUB + "1", Collections.emptyList()));

    Assert.assertFalse(strategy.match("192.168.1.1", () -> BlacklistConstants.FORBIDDEN_PUB));

    mgr.getBlacklistConfigList()
        .add(getIpConfig(BlacklistConstants.FORBIDDEN_PUB, Collections.emptyList()));
    Assert.assertFalse(strategy.match("192.168.1.1", () -> BlacklistConstants.FORBIDDEN_PUB));

    List<MatchType> types = Lists.newArrayList();
    MatchType m = new MatchType();
    m.setType(BlacklistConstants.IP_FULL);
    types.add(m);
    mgr.getBlacklistConfigList().add(getIpConfig(BlacklistConstants.FORBIDDEN_PUB, types));
    Assert.assertFalse(strategy.match("192.168.1.1", () -> BlacklistConstants.FORBIDDEN_PUB));

    m.setPatternSet(Sets.newSet("192.168.1.2"));
    Assert.assertFalse(strategy.match("192.168.1.1", () -> BlacklistConstants.FORBIDDEN_PUB));

    m.setPatternSet(Sets.newSet("192.168.1.2", "192.168.1.1"));
    Assert.assertTrue(m.toString(), m.toString().contains("192.168.1.1"));
    Assert.assertTrue(
        mgr.getBlacklistConfigList().toString(),
        mgr.getBlacklistConfigList().toString().contains("192.168.1.1"));

    Assert.assertTrue(strategy.match("192.168.1.1", () -> BlacklistConstants.FORBIDDEN_PUB));
  }

  private BlacklistConfig getIpConfig(String type, List<MatchType> matchTypes) {
    BlacklistConfig cfg = new BlacklistConfig();
    cfg.setType(type);
    Assert.assertEquals(cfg.getType(), type);
    cfg.setMatchTypes(matchTypes);
    List<MatchType> got = cfg.getMatchTypes();
    Assert.assertEquals(matchTypes.size(), got.size());
    for (int i = 0; i < got.size(); i++) {
      MatchType left = got.get(i);
      MatchType right = matchTypes.get(i);
      Assert.assertEquals(left.getType(), right.getType());
      Assert.assertEquals(left.getPatternSet(), right.getPatternSet());
      Assert.assertEquals(left.toString(), right.toString());
    }
    return cfg;
  }
}
