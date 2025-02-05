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

import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.filter.SyncAcceptorRequest;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-24 16:44 yuzhi.lyz Exp $
 */
public class DataSlotDiffSyncResultTest {
  private static final Logger LOG = LoggerFactory.getLogger(DataSlotDiffSyncResultTest.class);
  private static final AtomicLong SEQ = new AtomicLong();

  private static final SyncSlotAcceptorManager ACCEPT_ALL =
      new SyncSlotAcceptorManager() {
        @Override
        public boolean accept(SyncAcceptorRequest request) {
          return true;
        }
      };

  @Test
  public void testDiffDigestResult_emptyTarget() {
    Map<String, Integer> m = Maps.newHashMap();
    m.put("a", 100);
    m.put("b", 200);
    Map<String, Map<String, Publisher>> publishers = randPublishers(m);
    DataSlotDiffDigestResult result =
        DataSlotDiffUtils.diffDigestResult(Collections.emptyMap(), publishers, ACCEPT_ALL);
    Assert.assertFalse(result.getAddedDataInfoIds().isEmpty());
    Assert.assertEquals(result.getAddedDataInfoIds(), Lists.newArrayList("a", "b"));
    Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());
  }

  @Test
  public void testDiffDigestResult_target() {
    Map<String, Integer> m = Maps.newHashMap();
    m.put("a", 100);
    m.put("b", 200);
    Map<String, Map<String, Publisher>> publishers = randPublishers(m);
    Map<String, DatumSummary> summaryMap = PublisherUtils.getDatumSummary(publishers, ACCEPT_ALL);
    Map<String, DatumDigest> digestMap = PublisherDigestUtil.digest(summaryMap);
    DataSlotDiffDigestResult result =
        DataSlotDiffUtils.diffDigestResult(digestMap, publishers, ACCEPT_ALL);
    Assert.assertTrue(result.getAddedDataInfoIds().isEmpty());
    Assert.assertTrue(result.getRemovedDataInfoIds().isEmpty());

    Map<String, DatumDigest> test = Maps.newHashMap(digestMap);

    test.put("c", new DatumDigest(1, 0, 0, 0, (short) 0, (short) 0));
    result = DataSlotDiffUtils.diffDigestResult(test, publishers, ACCEPT_ALL);
    Assert.assertTrue(result.getAddedDataInfoIds().isEmpty());
    Assert.assertFalse(result.getRemovedDataInfoIds().isEmpty());

    Assert.assertEquals(result.getRemovedDataInfoIds(), Lists.newArrayList("c"));

    test = Maps.newHashMap(digestMap);
    test.remove("b");
    test.put("c", new DatumDigest(1, 0, 0, 0, (short) 0, (short) 0));
    result = DataSlotDiffUtils.diffDigestResult(test, publishers, ACCEPT_ALL);
    // add "b"
    Assert.assertEquals(result.getAddedDataInfoIds(), Lists.newArrayList("b"));

    // delete "c"
    Assert.assertTrue(result.getRemovedDataInfoIds().size() == 1);
    Assert.assertTrue(result.getRemovedDataInfoIds().contains("c"));
    DataSlotDiffUtils.logDiffResult("testDC", result, 10, LOG);
  }

  @Test
  public void testDiffPublishersResult() {
    Map<String, Integer> m = Maps.newHashMap();
    m.put("a", 100);
    m.put("b", 200);

    Map<String, Map<String, Publisher>> publishers = randPublishers(m);

    // the same
    Map<String, DatumSummary> summaryMap = PublisherUtils.getDatumSummary(publishers, ACCEPT_ALL);
    DataSlotDiffPublisherResult result =
        DataSlotDiffUtils.diffPublishersResult(summaryMap.values(), publishers, 400, ACCEPT_ALL);
    Assert.assertFalse(result.isHasRemain());
    Assert.assertTrue(result.getUpdatedPublishers().isEmpty());
    Assert.assertTrue(result.getRemovedPublishers().isEmpty());

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
    result =
        DataSlotDiffUtils.diffPublishersResult(summaryMap.values(), publishers, 1000, ACCEPT_ALL);
    Assert.assertFalse(result.isHasRemain());
    Assert.assertTrue(result.getRemovedPublishers().isEmpty());

    Assert.assertEquals(result.getUpdatedPublishers().keySet(), Sets.newHashSet("c", "d"));
    checkUpdatedSize(m, result);
    checkAddedPublisher(publishers, result);

    // reach max.publishers
    result =
        DataSlotDiffUtils.diffPublishersResult(summaryMap.values(), publishers, 100, ACCEPT_ALL);
    Assert.assertTrue(result.isHasRemain());
    Assert.assertTrue(result.getRemovedPublishers().isEmpty());

    Assert.assertEquals(result.getUpdatedPublishers().size(), 1);
    Assert.assertTrue(
        result.getUpdatedPublishers().keySet().contains("c")
            || result.getUpdatedPublishers().keySet().contains("d"));

    checkUpdatedSize(m, result);
    checkAddedPublisher(publishers, result);

    // delete a dataInfoId, delete b.publisher, modify c.publisher,  add d.publisher
    summaryMap = PublisherUtils.getDatumSummary(publishers, ACCEPT_ALL);
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
    result =
        DataSlotDiffUtils.diffPublishersResult(summaryMap.values(), publishers, 400, ACCEPT_ALL);
    Assert.assertFalse(result.isHasRemain());
    Assert.assertTrue(result.getRemovedPublishers().get("b").size() == 1);
    Assert.assertEquals(result.getRemovedPublishers().get("b").get(0), removedB.getRegisterId());

    Assert.assertEquals(result.getUpdatedPublishers().keySet(), Sets.newHashSet("c", "d"));
    Assert.assertEquals(result.getUpdatedPublishers().get("c").size(), 1);
    Assert.assertEquals(result.getUpdatedPublishers().get("d").size(), 1);
    Assert.assertEquals(result.getUpdatedPublishers().get("c").get(0), modifyC);
    Assert.assertEquals(result.getUpdatedPublishers().get("d").get(0), addD);

    DataSlotDiffUtils.logDiffResult("testDC", result, 10, LOG);

    // reach max
    result = DataSlotDiffUtils.diffPublishersResult(summaryMap.values(), publishers, 1, ACCEPT_ALL);
    Assert.assertTrue(result.isHasRemain());
    Assert.assertTrue(result.getRemovedPublishers().get("b").size() == 1);
    Assert.assertEquals(result.getRemovedPublishers().get("b").get(0), removedB.getRegisterId());

    Assert.assertTrue(
        result.getUpdatedPublishers().containsKey("c")
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
    DataSlotDiffUtils.logDiffResult("testDC", result, 10, LOG);
  }

  private static void checkAddedPublisher(
      Map<String, Map<String, Publisher>> publishers, DataSlotDiffPublisherResult result) {
    for (Map.Entry<String, List<Publisher>> e : result.getUpdatedPublishers().entrySet()) {
      Map<String, Publisher> publisherMap = publishers.get(e.getKey());
      Assert.assertEquals(publisherMap.size(), e.getValue().size());
      for (Publisher p : e.getValue()) {
        Assert.assertTrue(p == publisherMap.get(p.getRegisterId()));
      }
    }
  }

  private static void checkUpdatedSize(Map<String, Integer> m, DataSlotDiffPublisherResult result) {
    for (Map.Entry<String, Integer> e : m.entrySet()) {
      List<Publisher> publisherList = result.getUpdatedPublishers().get(e.getKey());
      if (publisherList != null) {
        Assert.assertEquals(e.getValue().intValue(), publisherList.size());
      }
    }
  }

  private static Publisher randPublisher() {
    Publisher p = new Publisher();
    p.setRegisterTimestamp(System.nanoTime());
    p.setRegisterId(System.currentTimeMillis() + "_" + SEQ.incrementAndGet());
    p.setVersion(1L);
    return p;
  }

  private static Map<String, Map<String, Publisher>> randPublishers(
      Map<String, Integer> publishers) {
    Map<String, Map<String, Publisher>> m = Maps.newHashMap();
    publishers.forEach(
        (k, i) -> {
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
