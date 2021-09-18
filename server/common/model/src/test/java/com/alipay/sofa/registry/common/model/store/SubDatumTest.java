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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

public class SubDatumTest {

  private SubPublisher newPublisher() {
    SubPublisher pub =
        new SubPublisher(
            "registerId",
            "cell",
            Lists.newArrayList(new ServerDataBox(new byte[] {1, 2, 3})),
            "clientId",
            1,
            "srcAddr",
            1234,
            PublishSource.CLIENT);
    return pub;
  }

  @Test
  public void testInit() {
    SubDatum.emptyOf("dataInfoId", "dc", 1, "dataId", "instanceId", "group");
    SubDatum d1 =
        SubDatum.normalOf(
            "dataInfoId",
            "dc",
            1,
            Lists.newArrayList(newPublisher()),
            "dataId",
            "instanceId",
            "group",
            Lists.newArrayList());
    SubDatum d2 =
        SubDatum.zipOf(
            "dataInfoId",
            "dc",
            1,
            "dataId",
            "instanceId",
            "group",
            Lists.newArrayList(),
            new ZipSubPublisherList(new byte[] {1, 2, 3, 4, 5}, 1024, "zstd", 1));
    Assert.assertEquals(d2.getDataInfoId(), "dataInfoId");
    Assert.assertEquals(d2.getDataCenter(), "dc");
    Assert.assertEquals(d2.getDataId(), "dataId");
    Assert.assertEquals(d2.getInstanceId(), "instanceId");
    Assert.assertEquals(d2.getGroup(), "group");
    Assert.assertEquals(1, d1.getPubNum());
    Assert.assertEquals(1, d2.getPubNum());
    Assert.assertEquals(1, d1.mustGetPublishers().size());
    Assert.assertEquals(3, d1.getDataBoxBytes());
    Assert.assertEquals(1, d1.getVersion());
    Assert.assertEquals(1024, d2.getDataBoxBytes());
    Assert.assertNotNull(d1.toString());
    Assert.assertEquals(3, SubDatum.intern(d1).getDataBoxBytes());
    Assert.assertEquals(1024, SubDatum.intern(d2).getDataBoxBytes());
    Assert.assertEquals(0, d1.getRecentVersions().size());
    Assert.assertEquals(191, d1.size());
    Assert.assertEquals(135, d2.size());

    Assert.assertEquals(d1.compressKey("zstd"), d1.compressKey("zstd"));
    Assert.assertNotEquals(d1.compressKey("zstd"), d1.compressKey("gzip"));
    Assert.assertNull(d1.getZipPublishers());
    Assert.assertNotNull(d2.getZipPublishers());
    d1.mustUnzipped();
    assertException(IllegalArgumentException.class, d2::mustGetPublishers);
  }

  public static void assertException(Class<? extends Throwable> eclazz, Runnable runnable) {
    try {
      runnable.run();
      Assert.assertTrue(false);
    } catch (Throwable exception) {
      Assert.assertEquals(exception.getClass(), eclazz);
    }
  }
}
