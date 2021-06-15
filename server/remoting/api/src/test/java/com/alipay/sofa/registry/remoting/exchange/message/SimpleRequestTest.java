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
package com.alipay.sofa.registry.remoting.exchange.message;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import java.util.concurrent.Executor;
import org.junit.Assert;
import org.junit.Test;

public class SimpleRequestTest {
  @Test
  public void test() {
    Object obj = new Object();
    URL url = new URL("192.168.1.1", 8888);
    SimpleRequest req = new SimpleRequest(obj, url);
    Assert.assertEquals(req.getRequestBody(), obj);
    Assert.assertEquals(req.getRequestUrl(), url);
    Assert.assertEquals(req.getCallBackHandler(), null);

    CallbackHandler callback =
        new CallbackHandler() {
          @Override
          public void onCallback(Channel channel, Object message) {}

          @Override
          public void onException(Channel channel, Throwable exception) {}

          @Override
          public Executor getExecutor() {
            return null;
          }
        };
    req = new SimpleRequest(obj, url, callback);
    Assert.assertEquals(req.getCallBackHandler(), callback);
  }
}
