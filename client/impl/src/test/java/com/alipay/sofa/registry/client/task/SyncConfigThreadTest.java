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
package com.alipay.sofa.registry.client.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import com.alipay.sofa.registry.client.provider.DefaultSubscriber;
import com.alipay.sofa.registry.client.provider.RegisterCache;
import com.alipay.sofa.registry.client.remoting.Client;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * @author zhuoyu.sjw
 * @version $Id: SyncConfigThreadTest.java, v 0.1 2018-03-15 14:33 zhuoyu.sjw Exp $$
 */
public class SyncConfigThreadTest {

  @Test
  public void syncConfigTest() throws RemotingException, InterruptedException {

    // mock
    RegistryClientConfig config =
        DefaultRegistryClientConfigBuilder.start()
            .setDataCenter("test-data-center")
            .setZone("test-zone")
            .setSyncConfigRetryInterval(1000)
            .build();

    Client client = mock(Client.class);
    RegisterCache registerCache = mock(RegisterCache.class);
    ObserverHandler observerHandler = mock(ObserverHandler.class);
    DefaultSubscriber subscriber = mock(DefaultSubscriber.class);

    // when
    when(client.isConnected()).thenReturn(true);

    SyncConfigResponse response = new SyncConfigResponse();
    response.setSuccess(true);
    response.setAvailableSegments(Arrays.asList("segment1", "segment2"));
    response.setRetryInterval(3000000);
    when(client.invokeSync(anyObject())).thenReturn(response);

    List<Subscriber> subscribers = new ArrayList<Subscriber>();
    subscribers.add(subscriber);
    when(registerCache.getAllSubscribers()).thenReturn(subscribers);

    when(subscriber.getAvailableSegments()).thenReturn(new ArrayList<String>());
    when(subscriber.isInited()).thenReturn(true);

    // do
    SyncConfigThread configThread =
        new SyncConfigThread(client, registerCache, config, observerHandler);
    configThread.start();

    Thread.sleep(2000L);

    // verify
    verify(client, times(1)).isConnected();
    verify(client, times(1)).invokeSync(any());

    verify(subscriber, times(1)).setAvailableSegments(anyListOf(String.class));

    verify(observerHandler, times(1)).notify(eq(subscriber));
  }
}
