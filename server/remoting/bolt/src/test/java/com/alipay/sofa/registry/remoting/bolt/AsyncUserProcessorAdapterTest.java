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
package com.alipay.sofa.registry.remoting.bolt;

import com.alipay.remoting.BizContext;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AsyncUserProcessorAdapterTest {
  @Test
  public void test() throws Exception {
    final ChannelHandler handler = Mockito.mock(ChannelHandler.class);
    AsyncUserProcessorAdapter adapter = new AsyncUserProcessorAdapter(handler);
    Assert.assertNull(adapter.interest());
    Mockito.when(handler.interest()).thenReturn(String.class);
    Assert.assertEquals(adapter.interest(), String.class.getName());
    Executor executor = Executors.newCachedThreadPool();
    Mockito.when(handler.getExecutor()).thenReturn(executor);
    Assert.assertEquals(adapter.getExecutor(), executor);

    final BizContext exceptionContext = Mockito.mock(BizContext.class);
    Mockito.when(exceptionContext.getConnection()).thenThrow(new RuntimeException());
    TestUtils.assertException(
        RuntimeException.class, () -> adapter.handleRequest(exceptionContext, null, "test"));
  }
}
