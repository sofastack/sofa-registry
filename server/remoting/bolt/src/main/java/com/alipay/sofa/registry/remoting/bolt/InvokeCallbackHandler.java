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

import com.alipay.remoting.InvokeCallback;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import java.util.concurrent.Executor;

public final class InvokeCallbackHandler implements InvokeCallback {
  private final Channel channel;
  private final CallbackHandler callbackHandler;

  public InvokeCallbackHandler(Channel channel, CallbackHandler callbackHandler) {
    this.channel = channel;
    this.callbackHandler = callbackHandler;
  }

  @Override
  public void onResponse(Object result) {
    callbackHandler.onCallback(channel, result);
  }

  @Override
  public void onException(Throwable e) {
    callbackHandler.onException(channel, e);
  }

  @Override
  public Executor getExecutor() {
    return callbackHandler.getExecutor();
  }
}
