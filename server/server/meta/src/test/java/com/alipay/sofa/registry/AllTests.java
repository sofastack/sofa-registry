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
package com.alipay.sofa.registry;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
import com.alipay.sofa.registry.common.model.slot.Crc32CSlotFunctionTest;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModifiedTest;
import com.alipay.sofa.registry.server.meta.cluster.node.TestAbstractNodeEventTest;
import com.alipay.sofa.registry.server.meta.lease.LeaseTest;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManagerTest;
import com.alipay.sofa.registry.server.meta.lease.filter.DefaultRegistryForbiddenServerManagerTest;
import com.alipay.sofa.registry.server.meta.lease.impl.*;
import com.alipay.sofa.registry.server.meta.lease.session.DefaultSessionServerManagerTest;
import com.alipay.sofa.registry.server.meta.metaserver.impl.*;
import com.alipay.sofa.registry.server.meta.monitor.DefaultSlotTableMonitorTest;
import com.alipay.sofa.registry.server.meta.monitor.data.DataServerStatsTest;
import com.alipay.sofa.registry.server.meta.monitor.impl.DefaultSlotStatsTest;
import com.alipay.sofa.registry.server.meta.monitor.impl.DefaultSlotTableStatsTest;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifierTest;
import com.alipay.sofa.registry.server.meta.remoting.data.DefaultDataServerServiceTest;
import com.alipay.sofa.registry.server.meta.remoting.handler.HeartbeatRequestHandlerTest;
import com.alipay.sofa.registry.server.meta.remoting.session.DefaultSessionServerServiceTest;
import com.alipay.sofa.registry.server.meta.resource.*;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareFilterTest;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArrangerTest;
import com.alipay.sofa.registry.server.meta.slot.balance.LeaderOnlyBalancerTest;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManagerTest;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManagerTest;
import com.alipay.sofa.registry.server.meta.slot.tasks.BalanceTaskTest;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotMigrationIntegrationTest;
import com.alipay.sofa.registry.server.meta.slot.util.MigrateSlotGroupTest;
import com.alipay.sofa.registry.server.meta.slot.util.NodeComparatorTest;
import com.alipay.sofa.registry.server.meta.slot.util.SlotBuilderTest;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilderTest;
import com.alipay.sofa.registry.server.shared.slot.DiskSlotTableRecorderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  Crc32CSlotFunctionTest.class,
  DefaultCurrentDcMetaServerTest.class,
  DefaultSessionServerManagerTest.class,
  DefaultDataServerManagerTest.class,
  LeaseTest.class,
  HeartbeatRequestHandlerTest.class,
  DefaultMetaServerManagerTest.class,
  DefaultDataServerServiceTest.class,
  LocalMetaServerTest.class,
  DefaultSessionServerServiceTest.class,
  DefaultProvideDataNotifierTest.class,
  SimpleSlotManagerTest.class,
  NodeModifiedTest.class,
  BalanceTaskTest.class,
  TestAbstractNodeEventTest.class,
  DefaultSlotManagerTest.class,
  DefaultSlotTableMonitorTest.class,
  DiskSlotTableRecorderTest.class,
  SlotTableResourceTest.class,
  SlotMigrationIntegrationTest.class,
  SlotTableBuilderTest.class,
  SlotBuilderTest.class,
  NodeComparatorTest.class,
  MigrateSlotGroupTest.class,
  ScheduledSlotArrangerTest.class,
  LeaderOnlyBalancerTest.class,
  DefaultRegistryForbiddenServerManagerTest.class,
  TestAbstractEvictableLeaseManagerTest.class,
  SimpleLeaseManagerTest.class,
  LeaderAwareLeaseManagerTest.class,
  AbstractEvictableFilterableLeaseManagerTest.class,
  DefaultMetaLeaderElectorTest.class,
  DataServerStatsTest.class,
  DataServerStatsTest.class,
  DefaultSlotStatsTest.class,
  DefaultSlotTableStatsTest.class,
  LeaderAwareFilterTest.class,
  ProvideDataResourceTest.class,
  StopPushDataResourceTest.class,
  MetaDigestResourceTest.class,
  HealthResourceTest.class,
  MetaLeaderResourceTest.class,
  BlacklistDataResourceTest.class,
  RegistryCoreOpsResourceTest.class,
  SlotSyncResourceTest.class
})
public class AllTests {}
