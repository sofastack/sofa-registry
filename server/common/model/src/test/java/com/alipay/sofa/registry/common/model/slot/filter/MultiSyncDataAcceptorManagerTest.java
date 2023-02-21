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
package com.alipay.sofa.registry.common.model.slot.filter;

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.console.MultiSegmentSyncSwitch;
import com.alipay.sofa.registry.common.model.slot.filter.MultiSyncDataAcceptorManager.RemoteSyncDataAcceptorManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : MultiSyncDataAcceptorManagerTest.java, v 0.1 2023年02月02日 19:52 xiaojian.xj Exp $
 */
public class MultiSyncDataAcceptorManagerTest {

  @Test
  public void testMultiSyncDataAcceptorManager() {
    MultiSegmentSyncSwitch syncSwitch1 =
        new MultiSegmentSyncSwitch(
            true,
            true,
            "testRemoteDc1",
            Sets.newHashSet("SOFA", "SOFA_APP"),
            Sets.newHashSet("com.registry.dataid1#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP"),
            Sets.newHashSet(
                "com.registry.dataid1#@#DEFAULT_INSTANCE_ID#@#SOFA",
                "com.registry.dataid1#@#DEFAULT_INSTANCE_ID#@#SOFA_APP"),
            System.currentTimeMillis());
    MultiSegmentSyncSwitch syncSwitch2 =
        new MultiSegmentSyncSwitch(
            true,
            true,
            "testRemoteDc2",
            Sets.newHashSet("SOFA", "SOFA_APP"),
            Sets.newHashSet(),
            Sets.newHashSet(),
            System.currentTimeMillis());

    MultiSyncDataAcceptorManager manager = new MultiSyncDataAcceptorManager();
    manager.updateFrom(Lists.newArrayList(syncSwitch1, syncSwitch2));

    SyncSlotAcceptorManager slotAcceptorManager =
        manager.getSyncSlotAcceptorManager("testRemoteDc1");
    Assert.assertNotNull(slotAcceptorManager);
    Assert.assertNull(manager.getSyncSlotAcceptorManager("testRemoteDc3"));

    Assert.assertFalse(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA", PublishSource.DATUM_SYNCER)));
    Assert.assertFalse(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest("com.registry.dataid1#@#DEFAULT_INSTANCE_ID#@#SOFA")));

    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest("com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA")));
    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA_APP")));
    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid1#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP")));

    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA", PublishSource.CLIENT)));
    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA_APP", PublishSource.CLIENT)));
    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid1#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP",
                PublishSource.CLIENT)));

    slotAcceptorManager = manager.getSyncSlotAcceptorManager("testRemoteDc2");
    Assert.assertNotNull(slotAcceptorManager);

    Assert.assertFalse(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA", PublishSource.DATUM_SYNCER)));
    Assert.assertFalse(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP",
                PublishSource.CLIENT)));

    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest("com.registry.dataid1#@#DEFAULT_INSTANCE_ID#@#SOFA")));
    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest("com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA")));
    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA_APP")));

    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA", PublishSource.CLIENT)));
    Assert.assertTrue(
        slotAcceptorManager.accept(
            SyncAcceptorRequest.buildRequest(
                "com.registry.dataid2#@#DEFAULT_INSTANCE_ID#@#SOFA_APP", PublishSource.CLIENT)));
  }

  @Test
  public void testEqualsByName() {
    Set<SyncSlotAcceptor> acceptors = Sets.newHashSet();

    SyncPublishSourceAcceptor publishSourceAcceptor1 =
        new SyncPublishSourceAcceptor(Sets.newHashSet());
    SyncPublishSourceAcceptor publishSourceAcceptor2 =
        new SyncPublishSourceAcceptor(Sets.newHashSet(PublishSource.DATUM_SYNCER));
    acceptors.add(publishSourceAcceptor1);
    acceptors.add(publishSourceAcceptor2);

    SyncPublisherGroupAcceptor groupAcceptor1 = new SyncPublisherGroupAcceptor(Sets.newHashSet());
    SyncPublisherGroupAcceptor groupAcceptor2 =
        new SyncPublisherGroupAcceptor(Sets.newHashSet("SOFA"));
    acceptors.add(groupAcceptor1);
    acceptors.add(groupAcceptor2);

    SyncSlotDataInfoIdAcceptor dataInfoIdAcceptor1 =
        new SyncSlotDataInfoIdAcceptor(Sets.newHashSet());
    SyncSlotDataInfoIdAcceptor dataInfoIdAcceptor2 =
        new SyncSlotDataInfoIdAcceptor(Sets.newHashSet("aaa#@#DEFAULT_INSTANCE_ID#@#SOFA"));
    acceptors.add(dataInfoIdAcceptor1);
    acceptors.add(dataInfoIdAcceptor2);

    Assert.assertEquals(3, acceptors.size());

    MultiSyncDataAcceptorManager manager = new MultiSyncDataAcceptorManager();
    RemoteSyncDataAcceptorManager acceptorManager =
        manager.new RemoteSyncDataAcceptorManager(acceptors);
    Assert.assertFalse(
        acceptorManager.accept(
            SyncAcceptorRequest.buildRequest("aaa#@#DEFAULT_INSTANCE_ID#@#SOFA")));
  }
}
