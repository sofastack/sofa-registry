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
package com.alipay.sofa.registry.consistency.hash;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The type Consistent hash test.
 * @author zhuoyu.sjw
 * @version $Id : ConsistentHashTest.java, v 0.1 2018-03-07 11:24 zhuoyu.sjw Exp $$
 */
public class ConsistentHashTest {
    private static final Logger      LOGGER = LoggerFactory.getLogger(ConsistentHashTest.class);

    private ConsistentHash<TestNode> consistentHash;

    private List<TestNode>           testNodes;

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        testNodes = new ArrayList<>();
        testNodes.add(new TestNode("10.10.10.1"));
        testNodes.add(new TestNode("10.10.10.2"));
        testNodes.add(new TestNode("10.10.10.3"));
        testNodes.add(new TestNode("10.10.10.4"));
        testNodes.add(new TestNode("10.10.10.5"));
        consistentHash = new ConsistentHash<TestNode>(100, testNodes);
    }

    /**
     * Gets node for.
     */
    @Test
    public void getNodeFor() {
        String key = "test";
        TestNode testNode = consistentHash.getNodeFor(key);

        LOGGER.info("getNodeFor key: {}, result: {}", key, testNode);

        assertNotNull(testNode);
        assertTrue(testNodes.contains(testNode));
    }

    /**
     * Gets n unique nodes for.
     */
    @Test
    public void getNUniqueNodesFor() {
        String key = "test";
        List<TestNode> uniqueNodes = consistentHash.getNUniqueNodesFor(key, 3);

        LOGGER.info("getNUniqueNodesFor key: {}, result: {}", key, uniqueNodes);

        assertNotNull(uniqueNodes);
        assertEquals(uniqueNodes.size(), new HashSet<>(uniqueNodes).size());
    }

    /**
     * Gets n unique nodes for real nodes size.
     */
    @Test
    public void getNUniqueNodesForRealNodesSize() {
        String key = "test";
        List<TestNode> uniqueNodes = consistentHash.getNUniqueNodesFor(key, testNodes.size());

        LOGGER.info("getNUniqueNodesForRealNodesSize key: {}, result: {}", key, uniqueNodes);

        assertNotNull(uniqueNodes);
        assertEquals(testNodes.size(), new HashSet<>(uniqueNodes).size());
    }

    /**
     * Gets n unique nodes for more than real nodes size.
     */
    @Test
    public void getNUniqueNodesForMoreThanRealNodesSize() {
        String key = "test";
        List<TestNode> uniqueNodes = consistentHash.getNUniqueNodesFor(key, testNodes.size() + 1);

        LOGGER
            .info("getNUniqueNodesForMoreThanRealNodesSize key: {}, result: {}", key, uniqueNodes);

        assertNotNull(uniqueNodes);
        assertEquals(testNodes.size(), new HashSet<>(uniqueNodes).size());
    }

    /**
     * Gets n unique nodes for zero.
     */
    @Test
    public void getNUniqueNodesForZero() {
        String key = "test";
        List<TestNode> uniqueNodes = consistentHash.getNUniqueNodesFor(key, 0);

        LOGGER.info("getNUniqueNodesForZero key: {}, result: {}", key, uniqueNodes);

        assertNotNull(uniqueNodes);
        assertTrue(uniqueNodes.isEmpty());
    }
}