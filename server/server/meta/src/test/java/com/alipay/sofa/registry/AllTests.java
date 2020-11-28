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
import com.alipay.sofa.registry.server.meta.lease.impl.CrossDcMetaServerManagerTest;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultDataServerManagerTest;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultLeaseManagerTest;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultSessionManagerTest;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCrossDcMetaServerTest;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCurrentDcMetaServerTest;
import com.alipay.sofa.registry.server.meta.slot.impl.CrossDcSlotAllocatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CRC16SlotFunctionTest.class, DefaultCrossDcMetaServerTest.class,
                     CrossDcSlotAllocatorTest.class, DefaultCurrentDcMetaServerTest.class,
                     DefaultSessionManagerTest.class, DefaultDataServerManagerTest.class,
                     DefaultLeaseManagerTest.class, CrossDcMetaServerManagerTest.class })
public class AllTests {
}
