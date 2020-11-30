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

import com.alipay.sofa.registry.server.meta.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LeaseTest extends AbstractTest {

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
}