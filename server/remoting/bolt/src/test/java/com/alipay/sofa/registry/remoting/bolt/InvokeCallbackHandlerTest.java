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

import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import org.junit.Test;
import org.mockito.Mockito;

public class InvokeCallbackHandlerTest {
  @Test
  public void test() {
    Channel channel = Mockito.mock(Channel.class);
    CallbackHandler callbackHandler = Mockito.mock(CallbackHandler.class);
    InvokeCallbackHandler handler = new InvokeCallbackHandler(channel, callbackHandler);

    handler.onResponse(null);
    Mockito.verify(callbackHandler, Mockito.times(1))
        .onCallback(Mockito.anyObject(), Mockito.anyObject());
    Mockito.verify(callbackHandler, Mockito.times(0))
        .onException(Mockito.anyObject(), Mockito.anyObject());
    Mockito.verify(callbackHandler, Mockito.times(0)).getExecutor();

    handler.onException(null);
    Mockito.verify(callbackHandler, Mockito.times(1))
        .onCallback(Mockito.anyObject(), Mockito.anyObject());
    Mockito.verify(callbackHandler, Mockito.times(1))
        .onException(Mockito.anyObject(), Mockito.anyObject());
    Mockito.verify(callbackHandler, Mockito.times(0)).getExecutor();

    handler.getExecutor();
    Mockito.verify(callbackHandler, Mockito.times(1))
        .onCallback(Mockito.anyObject(), Mockito.anyObject());
    Mockito.verify(callbackHandler, Mockito.times(1))
        .onException(Mockito.anyObject(), Mockito.anyObject());
    Mockito.verify(callbackHandler, Mockito.times(1)).getExecutor();
  }
}
