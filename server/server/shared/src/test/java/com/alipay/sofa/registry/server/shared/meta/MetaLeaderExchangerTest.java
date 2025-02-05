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
package com.alipay.sofa.registry.server.shared.meta;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.elector.DistributeLockInfo;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.TestUtils;
import com.alipay.sofa.registry.server.shared.TestUtils.MockBaseMetaLeaderExchanger;
import com.alipay.sofa.registry.server.shared.constant.MetaLeaderLearnModeEnum;
import com.alipay.sofa.registry.store.api.elector.DistributeLockRepository;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : MetaLeaderExchangerTest.java, v 0.1 2023年02月03日 17:49 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class MetaLeaderExchangerTest {

  private static final Logger LOG = LoggerFactory.getLogger(MetaLeaderExchangerTest.class);

  private String testDc = "testDc";

  private DistributeLockInfo distributeLockInfo =
      new DistributeLockInfo(testDc, "testLock", "196.168.1.2", 10 * 1000);

  @InjectMocks private MockJdbcMetaLeaderExchanger mockJdbcMetaLeaderExchanger;

  private MockLbMetaLeaderExchanger mockLbMetaLeaderExchanger = new MockLbMetaLeaderExchanger();

  @Mock private DistributeLockRepository distributeLockRepository;

  @Before
  public void init() {
    distributeLockInfo.setGmtModifiedUnixNanos(System.currentTimeMillis() * 1000 * 1000);
    when(distributeLockRepository.queryDistLock(anyString())).thenReturn(distributeLockInfo);
  }

  @Test
  public void testLearnMetaLeader() {
    long timeMillis = System.currentTimeMillis();
    LeaderInfo leaderInfo1 = new LeaderInfo(timeMillis, "196.168.1.1");
    LeaderInfo leaderInfo2 = new LeaderInfo(timeMillis + 1, "196.168.1.2");
    LeaderInfo leaderInfo3 = new LeaderInfo(timeMillis - 1, "196.168.1.3");

    Assert.assertTrue(mockJdbcMetaLeaderExchanger.learn(testDc, leaderInfo1));
    Assert.assertTrue(mockJdbcMetaLeaderExchanger.learn(testDc, leaderInfo2));
    Assert.assertFalse(mockJdbcMetaLeaderExchanger.learn(testDc, leaderInfo3));

    Assert.assertEquals(leaderInfo2, mockJdbcMetaLeaderExchanger.getLeader(testDc));
    Assert.assertEquals(
        Collections.singleton(leaderInfo2.getLeader()), mockJdbcMetaLeaderExchanger.getServerIps());
  }

  @Test
  public void testResetLeaderFromJdbc() {
    LeaderInfo leader = mockJdbcMetaLeaderExchanger.getLeader(testDc);
    Assert.assertEquals(
        new LeaderInfo(
            distributeLockInfo.getGmtModifiedUnixMillis(), distributeLockInfo.getOwner()),
        leader);
  }

  private GenericResponse createSuccessGenericResponse() {
    GenericResponse genericResponse = new GenericResponse();
    genericResponse.setSuccess(true);
    HashMap<String, Object> data = Maps.newHashMap();
    data.put("epoch", System.currentTimeMillis());
    data.put("leader", "1.1.1.1");
    genericResponse.setData(data);
    return genericResponse;
  }

  private GenericResponse createFailGenericResponse() {
    GenericResponse genericResponse = new GenericResponse();
    genericResponse.setSuccess(false);
    return genericResponse;
  }

  private GenericResponse createEmptyLeaderResponse() {
    GenericResponse genericResponse = new GenericResponse();
    genericResponse.setSuccess(true);
    HashMap<String, Object> data = Maps.newHashMap();
    data.put("epoch", System.currentTimeMillis());
    genericResponse.setData(data);
    return genericResponse;
  }

  @Test
  public void testHandleRespFromLbFail() {

    Response response = TestUtils.createFailResponse(500);
    Assert.assertNull(mockLbMetaLeaderExchanger.handleResp(testDc, "1.1.1.1", response));
  }

  @Test
  public void testHandleRespFromLb() {
    Response response = TestUtils.createSuccessResponse(createFailGenericResponse());
    Assert.assertNull(mockLbMetaLeaderExchanger.handleResp(testDc, "1.1.1.1", response));

    response = TestUtils.createSuccessResponse(createEmptyLeaderResponse());
    Assert.assertNull(mockLbMetaLeaderExchanger.handleResp(testDc, "1.1.1.1", response));

    GenericResponse resp = createSuccessGenericResponse();
    response = TestUtils.createSuccessResponse(resp);
    LeaderInfo ret = mockLbMetaLeaderExchanger.handleResp(testDc, "1.1.1.1", response);
    Assert.assertEquals(((Map<String, String>) resp.getData()).get("leader"), ret.getLeader());
    Assert.assertEquals(((Map<String, String>) resp.getData()).get("epoch"), ret.getEpoch());
  }

  private class MockLbMetaLeaderExchanger extends MockBaseMetaLeaderExchanger {

    public MockLbMetaLeaderExchanger() {
      super("serverType", LOG);
    }

    @Override
    protected MetaLeaderLearnModeEnum getMode() {
      return MetaLeaderLearnModeEnum.LOADBALANCER;
    }
  }
}
