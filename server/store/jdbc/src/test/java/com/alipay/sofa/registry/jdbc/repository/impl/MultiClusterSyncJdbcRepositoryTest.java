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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.google.common.collect.Sets;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncJdbcRepositoryTest.java, v 0.1 2022年09月26日 19:25 xiaojian.xj Exp $
 */
public class MultiClusterSyncJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Resource private MultiClusterSyncRepository multiClusterSyncRepository;

  private MultiClusterSyncInfo createMultiClusterSyncInfo(String remoteDataCenter) {
    MultiClusterSyncInfo info = new MultiClusterSyncInfo();
    info.setRemoteDataCenter("testDC-remote-" + remoteDataCenter);
    info.setRemoteMetaAddress("testAddress");
    info.setEnableSyncDatum(true);
    info.setSyncDataInfoIds(Sets.newHashSet("testId1", "testId2"));
    info.setSynPublisherGroups(Sets.newHashSet("testGroup1", "testGroup2"));
    info.setIgnoreDataInfoIds(Sets.newHashSet("testId3", "testId4"));
    info.setDataVersion(System.currentTimeMillis());
    return info;
  }

  @Test
  public void testSaveAndUpdate() {
    MultiClusterSyncInfo info = createMultiClusterSyncInfo("testSaveAndUpdate");

    long exceptVersion = info.getDataVersion();
    Assert.assertTrue(multiClusterSyncRepository.insert(info));
    MultiClusterSyncInfo query = multiClusterSyncRepository.query(info.getRemoteDataCenter());

    info.setDataCenter(query.getDataCenter());
    Assert.assertEquals(info, query);

    info.getSyncDataInfoIds().add("testId5");
    info.setDataVersion(System.currentTimeMillis() + 1);
    Assert.assertTrue(multiClusterSyncRepository.update(info, exceptVersion));

    query = multiClusterSyncRepository.query(info.getRemoteDataCenter());
    Assert.assertEquals(info, query);
  }

  @Test
  public void testRemove() {
    MultiClusterSyncInfo info = createMultiClusterSyncInfo("testRemove");
    Assert.assertTrue(multiClusterSyncRepository.insert(info));
    MultiClusterSyncInfo query = multiClusterSyncRepository.query(info.getRemoteDataCenter());
    info.setDataCenter(query.getDataCenter());
    Assert.assertEquals(info, query);

    Assert.assertEquals(
        1, multiClusterSyncRepository.remove(info.getRemoteDataCenter(), info.getDataVersion()));
    Assert.assertNull(multiClusterSyncRepository.query(info.getRemoteDataCenter()));
  }
}
