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
package com.alipay.sofa.registry.server.session.wrapper;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.session.filter.ProcessFilter;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author huicha
 * @date 2025/7/24
 */
public class BlacklistWrapperInterceptorTest {

  @Test
  public void testPublisherBlockedByAttribute() throws Exception {
    BlacklistWrapperInterceptor interceptor = createInterceptor(false);

    // Create a publisher with BLOCKED_REQUEST_KEY attribute set
    Publisher publisher = new Publisher();
    publisher.setDataInfoId("test-dataid#@#DEFAULT_INSTANCE_ID#@#test-group");
    publisher.setDataId("test-dataid");
    publisher.setGroup("test-group");
    publisher.setInstanceId("DEFAULT_INSTANCE_ID");

    Map<String, String> attributes = new HashMap<>();
    attributes.put(ValueConstants.BLOCKED_REQUEST_KEY, "true");
    publisher.setAttributes(attributes);

    RegisterInvokeData registerInvokeData = new RegisterInvokeData(publisher, null);
    TouchChecker touchChecker = new TouchChecker(registerInvokeData);

    WrapperInvocation<RegisterInvokeData, Boolean> invocation =
        new WrapperInvocation<>(touchChecker, Collections.singletonList(interceptor));
    Boolean result = invocation.proceed();

    // Publisher with BLOCKED_REQUEST_KEY should be blocked (return true without calling proceed)
    Assert.assertTrue(result);
    Assert.assertFalse(touchChecker.hasBeenChecked());
  }

  @Test
  public void testSubscriberBlockedByAttribute() throws Exception {
    SessionRegistry mockRegistry = Mockito.mock(SessionRegistry.class);
    Mockito.when(mockRegistry.isPushEmpty(Mockito.any(Subscriber.class))).thenReturn(true);
    Mockito.when(mockRegistry.getDataCenterWhenPushEmpty()).thenReturn("DEFAULT_DC");

    FirePushService mockFirePushService = Mockito.mock(FirePushService.class);

    BlacklistWrapperInterceptor interceptor =
        createInterceptor(false, mockRegistry, mockFirePushService);

    Subscriber subscriber = new Subscriber();
    subscriber.setDataInfoId("test-dataid#@#DEFAULT_INSTANCE_ID#@#test-group");
    subscriber.setDataId("test-dataid");
    subscriber.setGroup("test-group");
    subscriber.setInstanceId("DEFAULT_INSTANCE_ID");

    Map<String, String> attributes = new HashMap<>();
    attributes.put(ValueConstants.BLOCKED_REQUEST_KEY, "true");
    subscriber.setAttributes(attributes);

    RegisterInvokeData registerInvokeData = new RegisterInvokeData(subscriber, null);
    TouchChecker touchChecker = new TouchChecker(registerInvokeData);

    WrapperInvocation<RegisterInvokeData, Boolean> invocation =
        new WrapperInvocation<>(touchChecker, Collections.singletonList(interceptor));
    Boolean result = invocation.proceed();

    Assert.assertTrue(result);
    Assert.assertFalse(touchChecker.hasBeenChecked());
    Mockito.verify(mockFirePushService).fireOnPushEmpty(Mockito.any(), Mockito.anyString());
  }

  @Test
  public void testNotBlockedPassesThrough() throws Exception {
    BlacklistWrapperInterceptor interceptor = createInterceptor(false);

    // Publisher without BLOCKED_REQUEST_KEY and processFilter returns false
    Publisher publisher = new Publisher();
    publisher.setDataInfoId("test-dataid#@#DEFAULT_INSTANCE_ID#@#test-group");
    publisher.setDataId("test-dataid");
    publisher.setGroup("test-group");
    publisher.setInstanceId("DEFAULT_INSTANCE_ID");

    RegisterInvokeData registerInvokeData = new RegisterInvokeData(publisher, null);
    TouchChecker touchChecker = new TouchChecker(registerInvokeData);

    WrapperInvocation<RegisterInvokeData, Boolean> invocation =
        new WrapperInvocation<>(touchChecker, Collections.singletonList(interceptor));
    Boolean result = invocation.proceed();

    // Should pass through to the next wrapper
    Assert.assertTrue(result);
    Assert.assertTrue(touchChecker.hasBeenChecked());
  }

  @SuppressWarnings("unchecked")
  private BlacklistWrapperInterceptor createInterceptor(boolean processFilterMatch)
      throws Exception {
    return createInterceptor(
        processFilterMatch,
        Mockito.mock(SessionRegistry.class),
        Mockito.mock(FirePushService.class));
  }

  @SuppressWarnings("unchecked")
  private BlacklistWrapperInterceptor createInterceptor(
      boolean processFilterMatch, SessionRegistry sessionRegistry, FirePushService firePushService)
      throws Exception {
    BlacklistWrapperInterceptor interceptor = new BlacklistWrapperInterceptor();

    ProcessFilter processFilter = Mockito.mock(ProcessFilter.class);
    Mockito.when(processFilter.match(Mockito.any())).thenReturn(processFilterMatch);

    setField(interceptor, "sessionRegistry", sessionRegistry);
    setField(interceptor, "firePushService", firePushService);
    setField(interceptor, "processFilter", processFilter);

    return interceptor;
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
