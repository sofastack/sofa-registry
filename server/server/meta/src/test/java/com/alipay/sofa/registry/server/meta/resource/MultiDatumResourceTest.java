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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent;
import com.alipay.sofa.registry.server.meta.remoting.data.DefaultDataServerService;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import java.util.UUID;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MultiDatumResourceTest {
  private static final String PROPERTY = "sofa.registry.management.auth-token";
  private final String token = UUID.randomUUID().toString();
  private String previousToken;

  @Mock private DefaultDataServerService defaultDataServerService;
  @Mock private MultiClusterSyncRepository multiClusterSyncRepository;
  @InjectMocks private MultiDatumResource resource;

  @Before
  public void configureToken() {
    previousToken = System.getProperty(PROPERTY);
    System.setProperty(PROPERTY, token);
  }

  @After
  public void restoreToken() {
    if (previousToken == null) {
      System.clearProperty(PROPERTY);
    } else {
      System.setProperty(PROPERTY, previousToken);
    }
  }

  @Test
  public void rejectsUnauthorizedDeletionBeforeSideEffects() {
    for (String invalidToken : new String[] {null, "", "wrong-token", "6c62lk8dmQoE5B8X"}) {
      Assert.assertFalse(
          resource.clearDatumPublish("remote-dc", "data-id", invalidToken).isSuccess());
      Assert.assertFalse(
          resource.clearGroupDatumPublish("remote-dc", "group", invalidToken).isSuccess());
    }
    verifyZeroInteractions(defaultDataServerService, multiClusterSyncRepository);
  }

  @Test
  public void acceptsConfiguredTokenForDeletion() {
    Assert.assertTrue(resource.clearDatumPublish("remote-dc", "data-id", token).isSuccess());
    Assert.assertTrue(resource.clearGroupDatumPublish("remote-dc", "group", token).isSuccess());
    verify(defaultDataServerService, times(2))
        .notifyRemoteDatumClear(any(RemoteDatumClearEvent.class));
  }
}
