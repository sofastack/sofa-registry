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
package com.alipay.sofa.registry.server.meta.slot.manager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTest;
import com.alipay.sofa.registry.server.meta.remoting.notifier.Notifier;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class DefaultSlotManagerTest extends AbstractMetaServerTest {

  private DefaultSlotManager slotManager;

  @Before
  public void beforeDefaultSlotManagerTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);
    when(metaLeaderService.amILeader()).thenReturn(true);
    slotManager = new DefaultSlotManager(metaLeaderService);
    slotManager.postConstruct();
  }

  @Test
  public void testSlotChangeNotification() throws InterruptedException {
    Notifier notifier = mock(Notifier.class);
    slotManager.setNotifiers(Lists.newArrayList(notifier));
    slotManager.refresh(randomSlotTable());
    Thread.sleep(100);
    verify(notifier, atLeast(1)).notifySlotTableChange(any());
  }

  @Test
  public void testSlotChangeNotifyOneFailWontAffectOthers() throws InterruptedException {
    Notifier notifier1 = mock(Notifier.class);
    Notifier notifier2 = mock(Notifier.class);
    Notifier notifier3 = mock(Notifier.class);
    doThrow(new SofaRegistryRuntimeException("expected"))
        .when(notifier2)
        .notifySlotTableChange(any());
    slotManager.setNotifiers(Lists.newArrayList(notifier1, notifier2, notifier3));

    slotManager.refresh(randomSlotTable());
    Thread.sleep(100);
    verify(notifier1, atLeast(1)).notifySlotTableChange(any());
    verify(notifier3, atLeast(1)).notifySlotTableChange(any());
  }
}
