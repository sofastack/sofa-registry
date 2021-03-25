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
package com.alipay.sofa.registry.server.meta.provide.data;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.server.meta.remoting.data.DefaultDataServerService;
import com.alipay.sofa.registry.server.meta.remoting.session.DefaultSessionServerService;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultProvideDataNotifierTest {

  private DefaultProvideDataNotifier notifier = new DefaultProvideDataNotifier();

  @Mock private DefaultDataServerService defaultDataServerService;

  @Mock private DefaultSessionServerService defaultSessionServerService;

  @Before
  public void beforeDefaultProvideDataNotifierTest() {
    MockitoAnnotations.initMocks(this);
    notifier
        .setDataServerProvideDataNotifier(defaultDataServerService)
        .setSessionServerProvideDataNotifier(defaultSessionServerService);
  }

  @Test
  public void testNotifyProvideDataChange() {
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(
            "message", System.currentTimeMillis(), Sets.newHashSet(Node.NodeType.DATA)));
    verify(defaultDataServerService, times(1)).notifyProvideDataChange(any());
    verify(defaultSessionServerService, never()).notifyProvideDataChange(any());

    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(
            "message", System.currentTimeMillis(), Sets.newHashSet(Node.NodeType.SESSION)));
    verify(defaultDataServerService, times(1)).notifyProvideDataChange(any());
    verify(defaultSessionServerService, times(1)).notifyProvideDataChange(any());

    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(
            "message",
            System.currentTimeMillis(),
            Sets.newHashSet(Node.NodeType.SESSION, Node.NodeType.DATA)));
    verify(defaultDataServerService, times(2)).notifyProvideDataChange(any());
    verify(defaultSessionServerService, times(2)).notifyProvideDataChange(any());
  }
}
