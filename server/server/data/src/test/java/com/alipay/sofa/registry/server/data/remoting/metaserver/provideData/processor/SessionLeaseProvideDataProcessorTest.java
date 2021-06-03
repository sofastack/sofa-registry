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
package com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.processor;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessorManager;
import org.junit.Assert;
import org.junit.Test;

public class SessionLeaseProvideDataProcessorTest {

  @Test
  public void test() {
    final SessionLeaseProvideDataProcessor processor = new SessionLeaseProvideDataProcessor();
    DataServerConfig cfg = TestBaseUtils.newDataConfig("testDc");
    processor.setDataServerConfig(cfg);
    ProvideData provideData = new ProvideData(null, "test", 100L);
    Assert.assertFalse(processor.support(provideData.getDataInfoId()));

    provideData = new ProvideData(null, ValueConstants.DATA_SESSION_LEASE_SEC, 100L);
    Assert.assertTrue(processor.support(provideData.getDataInfoId()));

    int prev = cfg.getSessionLeaseSecs();
    processor.processData(null);
    Assert.assertEquals(prev, cfg.getSessionLeaseSecs());

    processor.processData(new ProvideData(null, ValueConstants.DATA_SESSION_LEASE_SEC, 100L));
    Assert.assertEquals(prev, cfg.getSessionLeaseSecs());

    TestBaseUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          ServerDataBox box = new ServerDataBox("3");
          processor.processData(new ProvideData(box, ValueConstants.DATA_SESSION_LEASE_SEC, 100L));
        });

    ServerDataBox box = new ServerDataBox("10");
    processor.processData(new ProvideData(box, ValueConstants.DATA_SESSION_LEASE_SEC, 100L));
    Assert.assertEquals(10, cfg.getSessionLeaseSecs());

    ProvideDataProcessorManager mgr = new ProvideDataProcessorManager();
    mgr.addProvideDataProcessor(processor);
    Assert.assertFalse(mgr.support(null));

    box = new ServerDataBox("20");
    mgr.processData(new ProvideData(box, ValueConstants.DATA_SESSION_LEASE_SEC, 100L));
    Assert.assertEquals(20, cfg.getSessionLeaseSecs());
  }
}
