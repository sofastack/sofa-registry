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
 * <p>
 * Nov 13, 2020
 */

import com.alipay.sofa.registry.common.model.slot.CRC16SlotFunctionTest;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModifiedTest;
import com.alipay.sofa.registry.server.meta.lease.LeaseTest;
import com.alipay.sofa.registry.server.meta.lease.data.DataLeaseManagerTest;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManagerTest;
import com.alipay.sofa.registry.server.meta.lease.impl.CrossDcMetaServerManagerTest;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultLeaseManagerTest;
import com.alipay.sofa.registry.server.meta.lease.impl.TestAbstractRaftEnabledLeaseManager;
import com.alipay.sofa.registry.server.meta.lease.session.DefaultSessionServerManagerTest;
import com.alipay.sofa.registry.server.meta.lease.session.SessionLeaseManagerTest;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCrossDcMetaServerTest;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCurrentDcMetaServerTest;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultLocalMetaServerTest;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultMetaServerManagerTest;
import com.alipay.sofa.registry.server.meta.monitor.DefaultSlotTableMonitorTest;
import com.alipay.sofa.registry.server.meta.provide.data.DataServerProvideDataNotifierTest;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifierTest;
import com.alipay.sofa.registry.server.meta.provide.data.SessionServerProvideDataNotifierTest;
import com.alipay.sofa.registry.server.meta.resource.SlotTableResourceTest;
import com.alipay.sofa.registry.server.meta.slot.impl.*;
import com.alipay.sofa.registry.server.meta.slot.tasks.InitReshardingTaskTest;
import com.alipay.sofa.registry.server.meta.slot.tasks.ServerDeadRebalanceWorkTest;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotLeaderRebalanceTaskTest;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotReassignTaskTest;
import com.alipay.sofa.registry.server.shared.slot.DiskSlotTableRecorderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CRC16SlotFunctionTest.class, DefaultCrossDcMetaServerTest.class,
                     CrossDcSlotAllocatorTest.class, DefaultCurrentDcMetaServerTest.class,
                     DefaultSessionServerManagerTest.class, DefaultDataServerManagerTest.class,
                     DefaultLeaseManagerTest.class, CrossDcMetaServerManagerTest.class,
                     TestAbstractRaftEnabledLeaseManager.class, LeaseTest.class,
                     DefaultMetaServerManagerTest.class, ArrangeTaskExecutorTest.class,
                     DataServerProvideDataNotifierTest.class, DefaultLocalMetaServerTest.class,
                     DataLeaseManagerTest.class, SessionLeaseManagerTest.class,
                     SessionServerProvideDataNotifierTest.class,
                     DefaultProvideDataNotifierTest.class, LocalSlotManagerTest.class,
                     NodeModifiedTest.class, DefaultSlotManagerTest.class,
                     DataServerArrangeTaskDispatcherTest.class, DefaultSlotArrangerTest.class,
                     ServerDeadRebalanceWorkTest.class, InitReshardingTaskTest.class,
                     SlotLeaderRebalanceTaskTest.class, SlotReassignTaskTest.class,
                     DefaultSlotTableMonitorTest.class, DiskSlotTableRecorderTest.class,
                     SlotTableResourceTest.class})
public class AllTests {
}
