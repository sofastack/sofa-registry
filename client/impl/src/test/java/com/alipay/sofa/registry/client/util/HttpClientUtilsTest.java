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
package com.alipay.sofa.registry.client.util;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class HttpClientUtilsTest {

  private static HttpServer httpServer;

  @BeforeClass
  public static void start() throws IOException {
    httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(5678), 0);
    httpServer.createContext("/xxx", new MockHandler());
    httpServer.createContext("/yyy", new ErrorHandler());
    httpServer.setExecutor(null);
    httpServer.start();
  }

  @AfterClass
  public static void stop() {
    httpServer.stop(1);
  }

  @Test
  public void testGet() {
    String url = "http://127.0.0.1:56789/xxx";
    Map<String, String> map = new HashMap<String, String>();
    map.put("x", "1");
    map.put("y", "2");
    RegistryClientConfig clientConfig = new DefaultRegistryClientConfigBuilder().build();
    try {
      // wrong url
      HttpClientUtils.get(url, map, clientConfig);
      Assert.fail();
    } catch (Exception e) {
    }

    url = "http://127.0.0.1:5678/xxx";
    try {
      HttpClientUtils.get(url, map, clientConfig);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    url = "http://127.0.0.1:5678/yyy";
    try {
      Assert.assertNull(HttpClientUtils.get(url, map, clientConfig));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  static class MockHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String response = "hello world";
      exchange.sendResponseHeaders(200, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

  static class ErrorHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String response = "ServerDown";
      exchange.sendResponseHeaders(500, response.getBytes().length);
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }
}
