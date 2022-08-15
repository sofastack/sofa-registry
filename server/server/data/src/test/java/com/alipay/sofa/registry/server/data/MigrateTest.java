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

import com.alipay.remoting.serialization.HessianSerializer;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import org.junit.Test;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-18 14:09 yuzhi.lyz Exp $
 */
public class MigrateTest {
  private static final SyncSlotAcceptorManager ACCEPT_ALL = request -> true;

  @Test
  public void testBody() throws Exception {
    HessianSerializer s = new HessianSerializer();
    List<DatumSummary> list = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      Map<String, RegisterVersion> publisherMap = Maps.newHashMap();
      for (int j = 0; j < 1000; j++) {
        publisherMap.put(
            UUID.randomUUID().toString(), RegisterVersion.of(0, System.currentTimeMillis()));
      }
      DatumSummary summary = new DatumSummary("app" + System.currentTimeMillis(), publisherMap);
      list.add(summary);
    }
    DataSlotDiffPublisherRequest request =
        new DataSlotDiffPublisherRequest("testdc", 100, 200, ACCEPT_ALL, list);
    byte[] bs = s.serialize(request);
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    GZIPOutputStream zip = new GZIPOutputStream(b);
    OutputStream out = new BufferedOutputStream(zip);

    out.write(bs);
    out.flush();
    out.close();
    System.out.println(bs.length);
    System.out.println(b.toByteArray().length);
  }
}
