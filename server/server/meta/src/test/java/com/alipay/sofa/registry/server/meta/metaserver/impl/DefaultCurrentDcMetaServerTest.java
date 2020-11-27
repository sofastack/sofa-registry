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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultCurrentDcMetaServerTest extends AbstractTest {

    private DefaultCurrentDcMetaServer metaServer;

    @Test
    public void testGetSessionServers() {
    }

    //manually test
    @Test
    @Ignore
    public void testUpdateClusterMembers() throws Exception {
        RaftExchanger raftExchanger = startRaftExchanger();
        metaServer = new DefaultCurrentDcMetaServer().setRaftExchanger(raftExchanger);

        LifecycleHelper.initializeIfPossible(metaServer);
        LifecycleHelper.startIfPossible(metaServer);

        List<MetaNode> prevClusterNodes = metaServer.getClusterMembers();
        long prevEpoch = metaServer.getEpoch();
        metaServer.updateClusterMembers(Lists.newArrayList(new MetaNode(randomURL(randomIp()),
            getDc()), new MetaNode(randomURL(randomIp()), getDc()), new MetaNode(
            randomURL(randomIp()), getDc()), new MetaNode(randomURL(randomIp()), getDc()),
            new MetaNode(randomURL(randomIp()), getDc()), new MetaNode(randomURL(randomIp()),
                getDc())));
        long currentEpoch = metaServer.getEpoch();
        // wait for raft communication
        Thread.sleep(200);
        List<MetaNode> currentClusterNodes = metaServer.getClusterMembers();

        LifecycleHelper.stopIfPossible(metaServer);
        LifecycleHelper.disposeIfPossible(metaServer);

        Assert.assertTrue(currentEpoch > prevEpoch);
        Assert.assertNotEquals(currentClusterNodes.size(), prevClusterNodes.size());

    }

    @Test
    public void testGetSlotTable() {
    }

    @Test
    public void testCancel() {
    }

    @Test
    public void testRenew() {
    }

    @Test
    public void testEvict() {
    }

    @Test
    public void testGetLeaseManager() {
    }

    @Test
    public void testGetEpoch() {
    }
}