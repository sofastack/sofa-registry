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
package com.alipay.sofa.registry.remoting.bolt.exchange;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class BoltExchangeTest {
  @Test
  public void testException() {
    BoltExchange exchange = new BoltExchange();
    TestUtils.assertException(
        IllegalArgumentException.class,
        () -> exchange.connect("test", new URL("127.0.0.1", 8888), null));

    TestUtils.assertException(
        IllegalArgumentException.class,
        () -> exchange.open(new URL("127.0.0.1", 8888), 1024, 1024 * 2, null));
  }

  @Test
  public void test() {
    BoltExchange exchange = new BoltExchange();
    Assert.assertNull(exchange.getServer(9999));
    Server srv = null;
    try {
      srv = exchange.open(new URL("localhost", 9999), 1024, 1024 * 2, new ChannelHandler[0]);
      Assert.assertEquals(exchange.getServer(9999), srv);
    } finally {
      if (srv != null) {
        srv.close();
      }
    }

    Assert.assertNull(exchange.getServer(9998));
    try {
      srv = exchange.open(new URL("localhost", 9998), new ChannelHandler[0]);
      Assert.assertEquals(exchange.getServer(9998), srv);
    } finally {
      if (srv != null) {
        srv.close();
      }
    }
  }
}
