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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: BackupTest.java, v 0.1 2018-03-20 12:12 shangyu.wh Exp $
 */
public class BackupTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashTest.class);

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() {
        List<TestNode> testNodes = new ArrayList<>();
        testNodes.add(new TestNode("A"));
        testNodes.add(new TestNode("B"));
        testNodes.add(new TestNode("C"));
        testNodes.add(new TestNode("D"));
        testNodes.add(new TestNode("E"));
        testNodes.add(new TestNode("F"));
    }

    @Test
    public void getNodeFor() {
        String key1 = "test";
        String key2 = "dataID";
        Map<String, List<String>> map = new HashMap<>();
        map.put(key1, Arrays.asList("1,2,3,4,5,6".split(",")));
        map.put(key2, Arrays.asList("5,6,7,8,9,0".split(",")));

        List<TestNode> list1 = Arrays.asList(new TestNode("A"), new TestNode("B"),
            new TestNode("C"), new TestNode("D"), new TestNode("E"), new TestNode("F"));

        List<TestNode> list2 = Arrays.asList(new TestNode("A"), new TestNode("B"),
            new TestNode("C"), new TestNode("E"), new TestNode("F"));

        printTest(list1, list2, key2, map.get(key2));
    }

    private void printTest(List<TestNode> before, List<TestNode> after, String dataId,
                           List<String> dataList) {
        List<TestNode> testNodes1 = getCalculateTestNodes(before, dataId, before.size());
        List<TestNode> testNodes2 = getCalculateTestNodes(after, dataId, after.size());

        Map<TestNode, List<String>> testNodeMap = bindData(dataList,
            getCalculateTestNodes(before, dataId, 3));

        LOGGER.info("data map {}", testNodeMap);

        binddataDec(testNodes1, testNodes2, testNodeMap);
    }

    private void binddataDec(List<TestNode> before, List<TestNode> after,
                             Map<TestNode, List<String>> testNodeMap) {
        List<TestNode> subTestNodesBefore = new ArrayList<>(before.subList(0, 3));
        List<TestNode> subTestNodesAfter = new ArrayList<>(after.subList(0, 3));
        TestNode master = subTestNodesBefore.get(0);
        if (subTestNodesAfter.contains(master)) {
            master = subTestNodesBefore.get(0);
        } else {
            master = subTestNodesAfter.get(0);
        }
        if (subTestNodesAfter.removeAll(subTestNodesBefore)) {
            List<String> dataMaster = testNodeMap.get(master);
            for (TestNode testNode : subTestNodesAfter) {
                if (testNodeMap.get(testNode) == null) {
                    testNodeMap.put(testNode, dataMaster);
                } else {
                    throw new RuntimeException("data error!");
                }
            }
        }
        List<TestNode> newBefore = new ArrayList<>(before);
        if (newBefore.removeAll(after)) {
            for (TestNode testNode : newBefore) {

                testNodeMap.remove(testNode);
            }
        }
        LOGGER.info("after map:{}", testNodeMap);

    }

    private List<TestNode> getCalculateTestNodes(List<TestNode> in, String dataId, int num) {
        ConsistentHash<TestNode> consistentHash = new ConsistentHash<>(100, in);
        List<TestNode> ret;
        if (num > 0) {
            ret = consistentHash.getNUniqueNodesFor(dataId, num);
        } else {
            ret = Arrays.asList(consistentHash.getNodeFor(dataId));
        }

        LOGGER.info("getNUniqueNodesFor dataId: {}, result: {}", dataId, ret);
        return ret;
    }

    private Map<TestNode, List<String>> bindData(List<String> dataList, List<TestNode> nodelist) {
        Map<TestNode, List<String>> map = new LinkedHashMap<>();
        nodelist.forEach((testNode) -> map.put(testNode, dataList));
        return map;
    }
}