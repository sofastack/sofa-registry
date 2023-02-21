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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.test.TestUtils.MockMultiClusterSyncRepository;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import org.apache.logging.log4j.util.Strings;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncResourceTest.java, v 0.1 2023年02月20日 10:51 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiClusterSyncResourceTest {

  private static final String REMOTE_DC = "REMOTE_DC";

  private static final String AUTH_TOKEN = "6c62lk8dmQoE5B8X";
  private static final String FAIL_AUTH_TOKEN = "111";

  @InjectMocks private MultiClusterSyncResource multiClusterSyncResource;

  @Spy private MockMultiClusterSyncRepository mockMultiClusterSyncRepository;

  @Test
  public void testSaveAndQuery() {
    // save fail
    CommonResponse response =
        multiClusterSyncResource.saveConfig(REMOTE_DC, "test-address-1", FAIL_AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    response = multiClusterSyncResource.saveConfig(REMOTE_DC, "", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // query fail
    GenericResponse<MultiClusterSyncInfo> query = multiClusterSyncResource.query("");
    Assert.assertFalse(query.isSuccess());
    Assert.assertNull(query.getData());

    // query success, no data
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNull(query.getData());

    // save success
    response = multiClusterSyncResource.saveConfig(REMOTE_DC, "test-address-1", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());

    // query success, data exist
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // remove fail
    response =
        multiClusterSyncResource.removeConfig(
            REMOTE_DC, query.getData().getDataVersion() + "", FAIL_AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // remove fail
    response = multiClusterSyncResource.removeConfig(REMOTE_DC, "", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // remove fail
    response = multiClusterSyncResource.removeConfig(REMOTE_DC, "1", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // remove fail when sync enable = true
    response =
        multiClusterSyncResource.removeConfig(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update sync switch = false
    response =
        multiClusterSyncResource.syncDisable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertFalse(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // remove success when sync enable = false
    response =
        multiClusterSyncResource.removeConfig(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNull(query.getData());
  }

  @Test
  public void testUpdateSyncSwitch() {
    CommonResponse response =
        multiClusterSyncResource.saveConfig(REMOTE_DC, "test-address-1", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());

    // query success, data exist
    GenericResponse<MultiClusterSyncInfo> query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update sync switch fail
    response =
        multiClusterSyncResource.syncDisable(
            REMOTE_DC, query.getData().getDataVersion() + "", FAIL_AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // update sync switch fail
    response = multiClusterSyncResource.syncDisable(REMOTE_DC, "", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // update sync switch fail
    response = multiClusterSyncResource.syncDisable(REMOTE_DC, "1", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // update sync switch = false
    response =
        multiClusterSyncResource.syncDisable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertFalse(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update sync switch = true
    response =
        multiClusterSyncResource.syncEnable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());
  }

  @Test
  public void testUpdatePushSwitch() {
    CommonResponse response =
        multiClusterSyncResource.saveConfig(REMOTE_DC, "test-address-1", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());

    // query success, data exist
    GenericResponse<MultiClusterSyncInfo> query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update push switch fail
    response =
        multiClusterSyncResource.pushEnable(
            REMOTE_DC, query.getData().getDataVersion() + "", FAIL_AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // update push switch fail
    response = multiClusterSyncResource.pushEnable(REMOTE_DC, "", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // update push switch fail
    response = multiClusterSyncResource.pushEnable(REMOTE_DC, "1", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    // update sync switch = false, push switch = true, update fail
    response =
        multiClusterSyncResource.syncDisable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    response =
        multiClusterSyncResource.pushEnable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertFalse(response.isSuccess());

    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertFalse(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update sync switch = true, push switch = true, update fail
    response =
        multiClusterSyncResource.syncEnable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    response =
        multiClusterSyncResource.pushEnable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertTrue(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update push switch = false
    response =
        multiClusterSyncResource.pushDisable(
            REMOTE_DC, query.getData().getDataVersion() + "", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());
  }

  @Test
  public void testUpdateMetaAddress() {
    CommonResponse response =
        multiClusterSyncResource.saveConfig(REMOTE_DC, "test-address-1", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());

    // query success, data exist
    GenericResponse<MultiClusterSyncInfo> query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update fail
    response =
        multiClusterSyncResource.updateMetaAddress(
            REMOTE_DC, "test-address-2", FAIL_AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertFalse(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertEquals("test-address-1", query.getData().getRemoteMetaAddress());

    // update fail
    response =
        multiClusterSyncResource.updateMetaAddress(
            REMOTE_DC, "", AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertFalse(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertEquals("test-address-1", query.getData().getRemoteMetaAddress());

    // update fail
    response =
        multiClusterSyncResource.updateMetaAddress(REMOTE_DC, "test-address-2", AUTH_TOKEN, "1");
    Assert.assertFalse(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertEquals("test-address-1", query.getData().getRemoteMetaAddress());

    // update success
    response =
        multiClusterSyncResource.updateMetaAddress(
            REMOTE_DC, "test-address-2", AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertTrue(response.isSuccess());
    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertEquals("test-address-2", query.getData().getRemoteMetaAddress());
  }

  @Test
  public void testSyncDataInfoIds() {
    CommonResponse response =
        multiClusterSyncResource.saveConfig(REMOTE_DC, "test-address-1", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());

    // query success, data exist
    GenericResponse<MultiClusterSyncInfo> query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update fail
    Set<String> idSet1 = Collections.singleton("test-id1");
    String ids1 = Strings.join(idSet1, ',');

    response =
        multiClusterSyncResource.addSyncDataInfoIds(
            REMOTE_DC, ids1, FAIL_AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.addSyncDataInfoIds(REMOTE_DC, ids1, AUTH_TOKEN, "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.addSyncDataInfoIds(REMOTE_DC, ids1, AUTH_TOKEN, "1");
    Assert.assertFalse(response.isSuccess());

    // add success
    response =
        multiClusterSyncResource.addSyncDataInfoIds(
            REMOTE_DC, ids1, AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertTrue(response.isSuccess());

    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), idSet1);
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update fail
    response =
        multiClusterSyncResource.removeSyncDataInfoIds(
            REMOTE_DC, ids1, FAIL_AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.removeSyncDataInfoIds(REMOTE_DC, ids1, AUTH_TOKEN, "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.removeSyncDataInfoIds(REMOTE_DC, ids1, AUTH_TOKEN, "1");
    Assert.assertFalse(response.isSuccess());

    // remove success
    response =
        multiClusterSyncResource.removeSyncDataInfoIds(
            REMOTE_DC, ids1, AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertTrue(response.isSuccess());

    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());
  }

  @Test
  public void testSyncGroups() {
    CommonResponse response =
        multiClusterSyncResource.saveConfig(REMOTE_DC, "test-address-1", AUTH_TOKEN);
    Assert.assertTrue(response.isSuccess());

    // query success, data exist
    GenericResponse<MultiClusterSyncInfo> query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update fail
    String group = "test-group";
    Set<String> idSet1 = Collections.singleton(group.toUpperCase(Locale.ROOT));

    response =
        multiClusterSyncResource.addSyncGroup(
            REMOTE_DC, group, FAIL_AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.addSyncGroup(REMOTE_DC, group, AUTH_TOKEN, "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.addSyncGroup(REMOTE_DC, group, AUTH_TOKEN, "1");
    Assert.assertFalse(response.isSuccess());

    // add success
    response =
        multiClusterSyncResource.addSyncGroup(
            REMOTE_DC, group, AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertTrue(response.isSuccess());

    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), idSet1);
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());

    // update fail
    response =
        multiClusterSyncResource.removeSyncGroup(
            REMOTE_DC, group, FAIL_AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.removeSyncGroup(REMOTE_DC, group, AUTH_TOKEN, "");
    Assert.assertFalse(response.isSuccess());

    // update fail
    response = multiClusterSyncResource.removeSyncGroup(REMOTE_DC, group, AUTH_TOKEN, "1");
    Assert.assertFalse(response.isSuccess());

    // remove success
    response =
        multiClusterSyncResource.removeSyncGroup(
            REMOTE_DC, group, AUTH_TOKEN, query.getData().getDataVersion() + "");
    Assert.assertTrue(response.isSuccess());

    query = multiClusterSyncResource.query(REMOTE_DC);
    Assert.assertTrue(query.isSuccess());
    Assert.assertNotNull(query.getData());
    Assert.assertTrue(query.getData().isEnableSyncDatum());
    Assert.assertFalse(query.getData().isEnablePush());
    Assert.assertEquals(query.getData().getSyncDataInfoIds(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getSynPublisherGroups(), Sets.newHashSet());
    Assert.assertEquals(query.getData().getIgnoreDataInfoIds(), Sets.newHashSet());
  }
}
