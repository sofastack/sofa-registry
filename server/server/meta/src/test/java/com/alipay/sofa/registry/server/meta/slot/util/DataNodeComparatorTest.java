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
package com.alipay.sofa.registry.server.meta.slot.util;

import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.DataNodeComparator;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

public class DataNodeComparatorTest extends AbstractTest {

    @Test
    public void testCompare() {
        List<String> prev = NodeUtils.transferNodeToIpList(randomDataNodes(6));
        List<String> current = Lists.newArrayList(prev);
        List<String> added = NodeUtils.transferNodeToIpList(randomDataNodes(2));
        current.addAll(added);
        List<String> removed = prev.subList(0, 3);
        current.removeAll(removed);
        DataNodeComparator comparator = new DataNodeComparator(prev, current);
        Assert.assertEquals(new HashSet<>(added), comparator.getAdded());
        Assert.assertEquals(new HashSet<>(removed), comparator.getRemoved());
    }
}