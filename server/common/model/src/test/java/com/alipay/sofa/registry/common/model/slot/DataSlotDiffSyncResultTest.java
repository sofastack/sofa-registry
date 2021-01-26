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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-24 16:44 yuzhi.lyz Exp $
 */
public class DataSlotDiffSyncResultTest {
    private static final Logger     LOG = LoggerFactory.getLogger(DataSlotDiffSyncResultTest.class);
    private static final AtomicLong SEQ = new AtomicLong();

    @Test
    public void testDiffDataInfoIdsResult_emptyTarget() {
        Map<String, Integer> m = Maps.newHashMap();
        m.put("a", 100);
        m.put("b", 200);
        Map<String, Map<String, Publisher>> publishers = randPublishers(m);
        DataSlotDiffSyncResult result = DataSlotDiffUtils.diffDataInfoIdsResult(Sets.newHashSet(),
            publishers);
        Assert.assertFalse(result.isHasRemain());
        Assert.assertFalse(result.getAddedDataInfoIds().isEmpty());
        Assert.assertEquals(result.getAddedDataInfoIds(), Lists.newArrayList("a", "b"));
        Assert.assertTrue(result.getUpdatedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());
    }

    @Test
    public void testDiffDataInfoIdsResult_target() {
        Map<String, Integer> m = Maps.newHashMap();
        m.put("a", 100);
        m.put("b", 200);
        Map<String, Map<String, Publisher>> publishers = randPublishers(m);

        DataSlotDiffSyncResult result = DataSlotDiffUtils.diffDataInfoIdsResult(
            Sets.newHashSet("a", "b"), publishers);
        Assert.assertFalse(result.isHasRemain());
        Assert.assertTrue(result.getAddedDataInfoIds().isEmpty());
        Assert.assertTrue(result.getUpdatedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());

        result = DataSlotDiffUtils
            .diffDataInfoIdsResult(Sets.newHashSet("a", "b", "c"), publishers);
        Assert.assertFalse(result.isHasRemain());
        Assert.assertTrue(result.getAddedDataInfoIds().isEmpty());
        Assert.assertTrue(result.getUpdatedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedPublishers().isEmpty());
        Assert.assertFalse(result.getRemovedDataInfoIds().isEmpty());

        Assert.assertEquals(result.getRemovedDataInfoIds(), Lists.newArrayList("c"));

        result = DataSlotDiffUtils.diffDataInfoIdsResult(Sets.newHashSet("a", "c"), publishers);
        Assert.assertFalse(result.isHasRemain());
        Assert.assertTrue(result.getRemovedPublishers().isEmpty());
        // add "b"
        Assert.assertEquals(result.getAddedDataInfoIds(), Lists.newArrayList("b"));

        // delete "c"
        Assert.assertTrue(result.getRemovedDataInfoIds().size() == 1);
        Assert.assertTrue(result.getRemovedDataInfoIds().contains("c"));
        DataSlotDiffUtils.logDiffResult(result, 10, LOG);

    }

    @Test
    public void testDiffPublishersResult() {
        Map<String, Integer> m = Maps.newHashMap();
        m.put("a", 100);
        m.put("b", 200);

        Map<String, Map<String, Publisher>> publishers = randPublishers(m);

        // the same
        Map<String, DatumSummary> summaryMap = createSummary(publishers);
        DataSlotDiffSyncResult result = DataSlotDiffUtils.diffPublishersResult(summaryMap,
            publishers, 400);
        Assert.assertFalse(result.isHasRemain());
        Assert.assertTrue(result.getUpdatedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());

        // now add c, d
        Map<String, Integer> newM = Maps.newHashMap();
        newM.put("c", 300);
        newM.put("d", 400);
        m.putAll(newM);
        Map<String, Map<String, Publisher>> addPublishers = randPublishers(newM);
        publishers.putAll(addPublishers);

        // not reach max.publishers
        summaryMap.put("c", new DatumSummary("c"));
        summaryMap.put("d", new DatumSummary("d"));
        result = DataSlotDiffUtils.diffPublishersResult(summaryMap, publishers, 1000);
        Assert.assertFalse(result.isHasRemain());
        Assert.assertTrue(result.getRemovedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());

        Assert.assertEquals(result.getUpdatedPublishers().keySet(), Sets.newHashSet("c", "d"));
        checkUpdatedSize(m, result);
        checkAddedPublisher(publishers, result);

        // reach max.publishers
        result = DataSlotDiffUtils.diffPublishersResult(summaryMap, publishers, 100);
        Assert.assertTrue(result.isHasRemain());
        Assert.assertTrue(result.getRemovedPublishers().isEmpty());
        Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());

        Assert.assertEquals(result.getUpdatedPublishers().size(), 1);
        Assert.assertTrue(result.getUpdatedPublishers().keySet().contains("c")
                          || result.getUpdatedPublishers().keySet().contains("d"));

        checkUpdatedSize(m, result);
        checkAddedPublisher(publishers, result);

        // delete a dataInfoId, delete b.publisher, modify c.publisher,  add d.publisher
        summaryMap = createSummary(publishers);
        publishers.remove("a");

        Iterator<Map.Entry<String, Publisher>> iter = publishers.get("b").entrySet().iterator();
        Publisher removedB = iter.next().getValue();
        iter.remove();

        Publisher modifyC = null;
        for (Map.Entry<String, Publisher> e : publishers.get("c").entrySet()) {
            modifyC = e.getValue();
            modifyC.setRegisterTimestamp(System.nanoTime());
            break;
        }
        Publisher addD = randPublisher();
        publishers.get("d").put(addD.getRegisterId(), addD);

        // not reach max
        result = DataSlotDiffUtils.diffPublishersResult(summaryMap, publishers, 400);
        Assert.assertFalse(result.isHasRemain());
        Assert.assertEquals(result.getRemovedDataInfoIds().size(), 1);
        Assert.assertEquals(result.getRemovedDataInfoIds().get(0), "a");
        Assert.assertTrue(result.getRemovedPublishers().get("b").size() == 1);
        Assert
            .assertEquals(result.getRemovedPublishers().get("b").get(0), removedB.getRegisterId());

        Assert.assertEquals(result.getUpdatedPublishers().keySet(), Sets.newHashSet("c", "d"));
        Assert.assertEquals(result.getUpdatedPublishers().get("c").size(), 1);
        Assert.assertEquals(result.getUpdatedPublishers().get("d").size(), 1);
        Assert.assertEquals(result.getUpdatedPublishers().get("c").get(0), modifyC);
        Assert.assertEquals(result.getUpdatedPublishers().get("d").get(0), addD);

        DataSlotDiffUtils.logDiffResult(result, 10, LOG);

        // reach max
        result = DataSlotDiffUtils.diffPublishersResult(summaryMap, publishers, 1);
        Assert.assertTrue(result.isHasRemain());
        Assert.assertEquals(result.getRemovedDataInfoIds().size(), 1);
        Assert.assertEquals(result.getRemovedDataInfoIds().get(0), "a");
        Assert.assertTrue(result.getRemovedPublishers().get("b").size() == 1);
        Assert
            .assertEquals(result.getRemovedPublishers().get("b").get(0), removedB.getRegisterId());

        Assert.assertTrue(result.getUpdatedPublishers().containsKey("c")
                          || result.getUpdatedPublishers().containsKey("d"));

        Assert.assertEquals(result.getUpdatedPublishers().size(), 1);

        if (result.getUpdatedPublishers().containsKey("c")) {
            Assert.assertEquals(result.getUpdatedPublishers().get("c").size(), 1);
            Assert.assertEquals(result.getUpdatedPublishers().get("c").get(0), modifyC);
        }

        if (result.getUpdatedPublishers().containsKey("d")) {
            Assert.assertEquals(result.getUpdatedPublishers().get("d").size(), 1);
            Assert.assertEquals(result.getUpdatedPublishers().get("d").get(0), addD);
        }
        DataSlotDiffUtils.logDiffResult(result, 10, LOG);

    }

    private static void checkAddedPublisher(Map<String, Map<String, Publisher>> publishers,
                                            DataSlotDiffSyncResult result) {
        for (Map.Entry<String, List<Publisher>> e : result.getUpdatedPublishers().entrySet()) {
            Map<String, Publisher> publisherMap = publishers.get(e.getKey());
            Assert.assertEquals(publisherMap.size(), e.getValue().size());
            for (Publisher p : e.getValue()) {
                Assert.assertTrue(p == publisherMap.get(p.getRegisterId()));
            }
        }
    }

    private static void checkUpdatedSize(Map<String, Integer> m, DataSlotDiffSyncResult result) {
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            List<Publisher> publisherList = result.getUpdatedPublishers().get(e.getKey());
            if (publisherList != null) {
                Assert.assertEquals(e.getValue().intValue(), publisherList.size());
            }
        }
    }

    private static Map<String, DatumSummary> createSummary(Map<String, Map<String, Publisher>> publishers) {
        Map<String, DatumSummary> summaryMap = Maps.newHashMap();
        for (Map.Entry<String, Map<String, Publisher>> e : publishers.entrySet()) {
            final DatumSummary summary = new DatumSummary(e.getKey());
            e.getValue().forEach((k, p) -> {
                summary.addPublisherVersion(p.getRegisterId(), p.publisherVersion());
            });
            summaryMap.put(e.getKey(), summary);
        }
        return summaryMap;
    }

    private static Publisher randPublisher() {
        Publisher p = new Publisher();
        p.setRegisterTimestamp(System.nanoTime());
        p.setRegisterId(System.currentTimeMillis() + "_" + SEQ.incrementAndGet());
        p.setVersion(1L);
        return p;
    }

    private static Map<String, Map<String, Publisher>> randPublishers(Map<String, Integer> publishers) {
        Map<String, Map<String, Publisher>> m = Maps.newHashMap();
        publishers.forEach((k, i) -> {
            Map<String, Publisher> publisherMap = Maps.newHashMap();
            for (int j = 0; j < i; j++) {
                Publisher p = randPublisher();
                publisherMap.put(p.getRegisterId(), p);
            }
            m.put(k, publisherMap);
        });
        return m;
    }
}
