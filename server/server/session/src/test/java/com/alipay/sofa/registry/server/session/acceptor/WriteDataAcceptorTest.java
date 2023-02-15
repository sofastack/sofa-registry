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
package com.alipay.sofa.registry.server.session.acceptor;

import com.alipay.sofa.registry.common.model.ClientOffPublishers;
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/** */
public class WriteDataAcceptorTest {

  @Test
  public void test() {
    Publisher p = TestUtils.createTestPublisher("testDataId");
    ConnectId connectId = p.connectId();
    WriteDataAcceptorImpl impl = new WriteDataAcceptorImpl();
    impl.dataNodeService = Mockito.mock(DataNodeService.class);

    ClientOffWriteDataRequest off = new ClientOffWriteDataRequest(connectId, Lists.newArrayList(p));
    ClientOffPublishers offPublishers = off.getRequestBody();
    Assert.assertEquals(off.getConnectId(), connectId);
    Assert.assertEquals(off.getRequestType(), WriteDataRequest.WriteDataRequestType.CLIENT_OFF);
    Assert.assertEquals(offPublishers.getConnectId(), connectId);
    Assert.assertEquals(offPublishers.getPublishers().size(), 1);
    Assert.assertEquals(offPublishers.getPublishers().get(0), p);
    Assert.assertFalse(offPublishers.isEmpty());

    impl.accept(off);
    Mockito.verify(impl.dataNodeService, Mockito.times(1)).clientOff(offPublishers);

    PublisherRegisterWriteDataRequest pub = new PublisherRegisterWriteDataRequest(p);
    Assert.assertEquals(pub.getConnectId(), connectId);
    Assert.assertEquals(pub.getRequestType(), WriteDataRequest.WriteDataRequestType.PUBLISHER);
    Assert.assertEquals(pub.getConnectId(), connectId);
    impl.accept(pub);
    Mockito.verify(impl.dataNodeService, Mockito.times(1)).register(p);

    PublisherUnregisterWriteDataRequest unpub = new PublisherUnregisterWriteDataRequest(p);
    Assert.assertEquals(unpub.getConnectId(), connectId);
    Assert.assertEquals(unpub.getRequestType(), WriteDataRequest.WriteDataRequestType.UN_PUBLISHER);
    Assert.assertEquals(unpub.getConnectId(), connectId);
    impl.accept(unpub);
    Mockito.verify(impl.dataNodeService, Mockito.times(1)).register(p);
  }
}
