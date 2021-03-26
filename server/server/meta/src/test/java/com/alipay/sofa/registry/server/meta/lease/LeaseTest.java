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
package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class LeaseTest extends AbstractMetaServerTestBase {

  private Lease<Object> lease;

  @Test
  public void testIsExpired() throws InterruptedException {
    lease = new Lease<>(new Object(), 1, TimeUnit.MILLISECONDS);
    Thread.sleep(2);
    Assert.assertTrue(lease.isExpired());
    lease.renew(1);
    Assert.assertFalse(lease.isExpired());
  }

  @Test
  public void testGetBeginTimestamp() throws InterruptedException {
    lease = new Lease<>(new Object(), 1);
    long begin = lease.getBeginTimestamp();
    lease.renew();
    Thread.sleep(5);
    lease.renew();
    Assert.assertEquals(begin, lease.getBeginTimestamp());
  }

  @Test
  public void testGetLastUpdateTimestamp() throws InterruptedException {
    lease = new Lease<>(new Object(), 1);
    long firstUpdate = lease.getLastUpdateTimestamp();
    lease.renew();
    Thread.sleep(5);
    lease.renew();
    Assert.assertNotEquals(firstUpdate, lease.getLastUpdateTimestamp());
  }

  @Test
  public void testSetRenewal() {
    Object first = new Object();
    lease = new Lease<>(first, 1);
    lease.renew();
    Object second = new Object();
    lease.setRenewal(second);
    Assert.assertNotEquals(first, lease.getRenewal());
  }

  @Test
  public void testEquals() throws InterruptedException {
    DataNode sample = new DataNode(randomURL(randomIp()), getDc());
    Lease<Object> lease1 = new Lease<>(new Object(), 1, TimeUnit.MILLISECONDS);
    Lease<Object> lease2 = new Lease<>(sample, 1, TimeUnit.MILLISECONDS);
    Thread.sleep(5);
    Lease<DataNode> lease3 =
        new Lease<>(new DataNode(sample.getNodeUrl(), getDc()), 1, TimeUnit.MILLISECONDS);
    Lease<Object> lease4 = lease2;
    lease4.setRenewal(lease3.getRenewal());

    Assert.assertNotEquals(lease1, lease2);
    Assert.assertNotEquals(lease2, lease3);
    Assert.assertEquals(lease2, lease4);
    Assert.assertNotEquals(lease2.hashCode(), lease3.hashCode());
  }
}
