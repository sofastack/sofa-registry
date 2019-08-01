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
package com.alipay.sofa.registry.server.data;

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.BackupTriad;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author xuanbei
 * @since 2019/1/16
 */
public class BackupTriadTest {
    @Test
    public void doTest() {
        List<DataNode> nodeList = new ArrayList<>();
        nodeList.add(new DataNode(new URL("192.168.0.1", 9632), "DefaultDataCenter"));
        nodeList.add(new DataNode(new URL("192.168.0.2", 9632), "DefaultDataCenter"));
        nodeList.add(new DataNode(new URL("192.168.0.3", 9632), "DefaultDataCenter"));
        BackupTriad backupTriad = new BackupTriad("TestDataInfoId", nodeList);
        assertFalse(backupTriad.containsSelf());

        List<DataNode> newTriad = new ArrayList<>();
        newTriad.add(new DataNode(new URL("192.168.0.2", 9632), "DefaultDataCenter"));
        newTriad.add(new DataNode(new URL("192.168.0.4", 9632), "DefaultDataCenter"));
        Set<String> notWorking = new HashSet<>();
        notWorking.add("192.168.0.2");
        assertEquals(2, backupTriad.getNewJoined(newTriad, notWorking).size());
        assertEquals("DataNode{ip=192.168.0.2}", backupTriad.getNewJoined(newTriad, notWorking)
            .get(0).toString());
        assertEquals("DataNode{ip=192.168.0.4}", backupTriad.getNewJoined(newTriad, notWorking)
            .get(1).toString());

        assertEquals("TestDataInfoId", backupTriad.getDataInfoId());
        backupTriad.setDataInfoId("AnotherTestDataInfoId");
        assertEquals("AnotherTestDataInfoId", backupTriad.getDataInfoId());

        nodeList = new ArrayList<>();
        nodeList.add(new DataNode(new URL("192.168.0.1", 9632), "DefaultDataCenter"));
        nodeList.add(new DataNode(new URL(DataServerConfig.IP, 9632), "DefaultDataCenter"));
        backupTriad.setTriad(nodeList);
        assertTrue(backupTriad.containsSelf());
        assertEquals(2, backupTriad.getTriad().size());
        assertEquals("DataNode{ip=192.168.0.1}", backupTriad.getTriad().get(0).toString());
        assertEquals("DataNode{ip=" + DataServerConfig.IP + "}", backupTriad.getTriad().get(1)
            .toString());
        assertTrue(backupTriad.toString().contains(
            "BackupTriad{dataInfoId='AnotherTestDataInfoId', ipSetOfNode=")
                   && backupTriad.toString().contains("192.168.0.1")
                   && backupTriad.toString().contains(DataServerConfig.IP));
    }
}
