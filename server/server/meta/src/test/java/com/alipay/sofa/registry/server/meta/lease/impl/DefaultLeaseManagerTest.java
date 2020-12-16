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
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DefaultLeaseManagerTest extends AbstractTest {

    private DefaultLeaseManager<SimpleNode> leaseManager;

    @Before
    public void beforeDefaultLeaseManagerTest() {
        leaseManager = new DefaultLeaseManager<>("snapshotFilePrefix");
        leaseManager.setLogger(logger);
    }

    @Test
    public void testCancel() {
        String ip = randomIp();
        leaseManager.renew(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
        leaseManager.cancel(new SimpleNode(ip));
        Assert.assertNull(leaseManager.repo.get(ip));
    }

    @Test
    public void testRenew() {
        String ip = randomIp();
        leaseManager.renew(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
        leaseManager.cancel(new SimpleNode(ip));
        Assert.assertNull(leaseManager.repo.get(ip));
        leaseManager.renew(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEvict() throws InterruptedException {
        String ip = randomIp();
        leaseManager.renew(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
        logger.info("[expired] {}", leaseManager.repo.get(ip).isExpired());
        leaseManager.evict();
    }

    @Test
    public void testGetEpoch() {
        Assert.assertEquals(0, leaseManager.getEpoch());
    }

    @Test
    public void testRefreshEpoch() {
        Assert.assertEquals(0, leaseManager.getEpoch());
        leaseManager.refreshEpoch(DatumVersionUtil.nextId());
        Assert.assertNotEquals(0, leaseManager.getEpoch());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNPE() {
        leaseManager.register(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCancelNPE() {
        leaseManager.cancel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenewNPE() {
        leaseManager.renew(null, 100);
    }

    @Test
    public void testRefreshEpochWithLowerVersion() {
        long epoch = DatumVersionUtil.nextId();
        leaseManager.refreshEpoch(epoch);
        Assert.assertEquals(epoch, leaseManager.getEpoch());
        leaseManager.refreshEpoch(epoch - 100);
        Assert.assertEquals(epoch, leaseManager.getEpoch());
    }

    @Test
    public void testSaveAndLoad() {
        int tasks = 100;
        new ConcurrentExecutor(tasks, executors).execute(new Runnable() {
            @Override
            public void run() {
                leaseManager.renew(new SimpleNode(randomIp()), Math.abs(random.nextInt()));
            }
        });
        String path = leaseManager.getSnapshotFileNames().iterator().next();
        leaseManager.copy().save(path);
        DefaultLeaseManager<SimpleNode> loadManager = new DefaultLeaseManager<>(path);
        loadManager.load(path);
        List<SimpleNode> expected = leaseManager.getClusterMembers();
        List<SimpleNode> actual = loadManager.getClusterMembers();
        Collections.sort(expected);
        Collections.sort(actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testEmptyLoad() {
        String path = randomString(100);
        try {
            FileUtils.writeByteArrayToFile(new File(path), new byte[0], true);
        } catch (Exception ignore) {

        }
        DefaultLeaseManager<SimpleNode> loadManager = new DefaultLeaseManager<>(path);
        Assert.assertFalse(loadManager.load(path));
    }

    public static class SimpleNode implements Node, Comparable<SimpleNode> {

        private final URL url;

        public SimpleNode(URL url) {
            this.url = url;
        }

        public SimpleNode(String ip) {
            this.url = new URL(ip);
        }

        @Override
        public NodeType getNodeType() {
            return NodeType.CLIENT;
        }

        @Override
        public URL getNodeUrl() {
            return url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SimpleNode that = (SimpleNode) o;
            return Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url);
        }

        @Override
        public int compareTo(SimpleNode o) {
            return this.url.getIpAddress().compareTo(o.url.getIpAddress());
        }
    }
}