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
package com.alipay.sofa.registry.remoting;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2019/1/16
 */
public class RequestExceptionTest {
  @Test
  public void doRequestExceptionTest() {
    RequestException exception = new RequestException("error message");
    Assert.assertEquals("error message", exception.getMessage());

    RuntimeException runtimeException = new RuntimeException("error message");
    exception = new RequestException(runtimeException);
    Assert.assertEquals("java.lang.RuntimeException: error message", exception.getMessage());
    Assert.assertEquals(runtimeException, exception.getCause());
    Assert.assertEquals("java.lang.RuntimeException: error message", exception.getMessage());

    exception = new RequestException("error message", runtimeException);
    Assert.assertEquals("error message", exception.getMessage());
    Assert.assertEquals(runtimeException, exception.getCause());

    Request request =
        new Request() {
          @Override
          public Object getRequestBody() {
            return "request body";
          }

          @Override
          public URL getRequestUrl() {
            return null;
          }
        };
    exception = new RequestException("error message", request);
    Assert.assertEquals(
        "request url: null, body: request body, error message", exception.getMessage());

    exception = new RequestException("error message", request, runtimeException);
    Assert.assertEquals(
        "request url: null, body: request body, error message", exception.getMessage());

    exception =
        new RequestException(
            "test",
            new Request() {
              @Override
              public Object getRequestBody() {
                return 1;
              }

              @Override
              public URL getRequestUrl() {
                return null;
              }
            });

    Assert.assertTrue(exception.getMessage().contains(Integer.class.getSimpleName()));
  }

  public void requestChannelClosedExceptionTest() {
    RequestChannelClosedException exception =
        new RequestChannelClosedException(
            "test",
            new Request() {
              @Override
              public Object getRequestBody() {
                return 1;
              }

              @Override
              public URL getRequestUrl() {
                return null;
              }
            });
    Assert.assertTrue(exception.getMessage().contains(Integer.class.getSimpleName()));

    exception = new RequestChannelClosedException("test");
    Assert.assertTrue(exception.getMessage().contains("test"));
  }
}
