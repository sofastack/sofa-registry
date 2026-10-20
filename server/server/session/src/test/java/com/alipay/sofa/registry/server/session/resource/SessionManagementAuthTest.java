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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.server.session.push.ChangeProcessor;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
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
public class SessionManagementAuthTest {
  private static final String PROPERTY = "sofa.registry.management.auth-token";
  private final String token = UUID.randomUUID().toString();
  private String previousToken;

  @Mock private ChangeProcessor changeProcessor;
  @Mock private ProvideDataRepository provideDataRepository;
  @Mock private MetaServerService metaServerService;
  @InjectMocks private PushEfficiencyConfigResource pushResource;
  @InjectMocks private EmergencyApiResource emergencyResource;

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
  public void rejectsUnauthorizedRequestsBeforeSideEffects() {
    for (String invalidToken : new String[] {null, "", "wrong-token", "6c62lk8dmQoE5B8X"}) {
      assertDenied(emergencyResource.shutdownByRepository(invalidToken));
      assertDenied(pushResource.getChangeDebouncingMillis(invalidToken));
      assertDenied(pushResource.isUseLargeAdapterDelayChangeWorker(invalidToken));
    }
    verifyZeroInteractions(changeProcessor, provideDataRepository, metaServerService);
  }

  @Test
  public void disablesManagementWhenConfigurationIsBlank() {
    System.setProperty(PROPERTY, " ");
    assertDenied(emergencyResource.shutdownByRepository(token));
    assertDenied(pushResource.getChangeDebouncingMillis(token));
    assertDenied(pushResource.isUseLargeAdapterDelayChangeWorker(token));
    verifyZeroInteractions(changeProcessor, provideDataRepository, metaServerService);
  }

  @Test
  public void acceptsConfiguredTokenForPushConfiguration() {
    Assert.assertTrue(pushResource.getChangeDebouncingMillis(token).isSuccess());
    Assert.assertTrue(pushResource.isUseLargeAdapterDelayChangeWorker(token).isSuccess());
    verify(changeProcessor).getChangeDebouncingMillis();
    verify(changeProcessor).isUseLargeAdapterDelayChangeWorker();
  }

  @Test
  public void authenticatedShutdownStillRequiresStopPush() {
    CommonResponse response = emergencyResource.shutdownByRepository(token);
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("push switch is open", response.getMessage());
    verify(provideDataRepository).get(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    verifyZeroInteractions(metaServerService);
  }

  private static void assertDenied(CommonResponse response) {
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("auth check fail", response.getMessage());
  }
}
