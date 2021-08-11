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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.sessionserver.PubSubDataInfoIdResp;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : SessionDigestResourceTest.java, v 0.1 2021年08月04日 15:23 xiaojian.xj Exp $
 */
public class SessionDigestResourceTest {

  @Test
  public void testMerge() {
    SessionDigestResource sessionDigestResource = new SessionDigestResource();
    List<PubSubDataInfoIdResp> resps = Lists.newArrayList();

    PubSubDataInfoIdResp resp1 = new PubSubDataInfoIdResp();
    Map<String, Set<String>> pubDataInfoIds = Maps.newHashMap();
    pubDataInfoIds.put("127.0.0.1", Sets.newHashSet("dataInfoId-1"));
    Map<String, Set<String>> subDataInfoIds = Maps.newHashMap();
    subDataInfoIds.put("127.0.0.1", Sets.newHashSet("dataInfoId-1"));
    resp1.setPubDataInfoIds(pubDataInfoIds);
    resp1.setSubDataInfoIds(subDataInfoIds);
    resps.add(resp1);

    PubSubDataInfoIdResp merge = sessionDigestResource.merge(resps);
    Assert.assertTrue(merge.getPubDataInfoIds().containsKey("127.0.0.1"));
    Assert.assertEquals(merge.getPubDataInfoIds().get("127.0.0.1").size(), 1);
    Assert.assertTrue(merge.getSubDataInfoIds().containsKey("127.0.0.1"));
    Assert.assertEquals(merge.getSubDataInfoIds().get("127.0.0.1").size(), 1);

    PubSubDataInfoIdResp resp2 = new PubSubDataInfoIdResp();
    Map<String, Set<String>> pubDataInfoIds2 = Maps.newHashMap();
    pubDataInfoIds2.put("127.0.0.1", Sets.newHashSet("dataInfoId-2"));
    Map<String, Set<String>> subDataInfoIds2 = Maps.newHashMap();
    subDataInfoIds2.put("127.0.0.1", Sets.newHashSet("dataInfoId-1"));
    resp2.setPubDataInfoIds(pubDataInfoIds2);
    resp2.setSubDataInfoIds(subDataInfoIds2);
    resps.add(resp2);

    merge = sessionDigestResource.merge(resps);
    Assert.assertTrue(merge.getPubDataInfoIds().containsKey("127.0.0.1"));
    Assert.assertEquals(merge.getPubDataInfoIds().get("127.0.0.1").size(), 2);
    Assert.assertTrue(merge.getSubDataInfoIds().containsKey("127.0.0.1"));
    Assert.assertEquals(merge.getSubDataInfoIds().get("127.0.0.1").size(), 1);
  }
}
