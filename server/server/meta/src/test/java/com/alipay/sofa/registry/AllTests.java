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

import com.alipay.sofa.registry.common.model.slot.Crc32CSlotFunctionTest;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModifiedTest;
import com.alipay.sofa.registry.server.meta.cluster.node.TestAbstractNodeEventTest;
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
import com.alipay.sofa.registry.server.meta.slot.arrange.*;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManagerTest;
import com.alipay.sofa.registry.server.meta.slot.tasks.BalanceTaskTest;
import com.alipay.sofa.registry.server.meta.slot.tasks.SlotMigrationIntegrationTest;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilderTest;
import com.alipay.sofa.registry.server.shared.slot.DiskSlotTableRecorderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ Crc32CSlotFunctionTest.class, DefaultCrossDcMetaServerTest.class,
                     CrossDcSlotAllocatorTest.class, DefaultCurrentDcMetaServerTest.class,
                     DefaultSessionServerManagerTest.class, DefaultDataServerManagerTest.class,
                     DefaultLeaseManagerTest.class, CrossDcMetaServerManagerTest.class,
                     TestAbstractRaftEnabledLeaseManager.class, LeaseTest.class,
                     DefaultMetaServerManagerTest.class, DataServerProvideDataNotifierTest.class,
                     DefaultLocalMetaServerTest.class, DataLeaseManagerTest.class,
                     SessionLeaseManagerTest.class, SessionServerProvideDataNotifierTest.class,
                     DefaultProvideDataNotifierTest.class, LocalSlotManagerTest.class,
                     NodeModifiedTest.class, BalanceTaskTest.class,
                     TestAbstractNodeEventTest.class, DefaultSlotManagerTest.class,
                     DefaultSlotTableMonitorTest.class, DiskSlotTableRecorderTest.class,
                     SlotTableResourceTest.class, SlotMigrationIntegrationTest.class,
                     SlotTableBuilderTest.class })
public class AllTests {
}
