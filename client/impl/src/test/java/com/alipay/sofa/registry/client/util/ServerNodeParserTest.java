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

import com.alipay.sofa.registry.client.remoting.ServerNode;
import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class ServerNodeParserTest {

  @Test
  public void parse() {
    boolean error = false;
    try {
      ServerNodeParser.parse(null);
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      error = true;
    }
    Assert.assertTrue(error);

    error = false;
    try {
      ServerNodeParser.parse("127.0.0.1:");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      error = true;
    }
    Assert.assertTrue(error);

    error = false;
    try {
      ServerNodeParser.parse("127.0.0.1");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      error = true;
    }
    Assert.assertTrue(error);

    error = false;
    try {
      ServerNodeParser.parse("127.0.0.1:12345?");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      error = true;
    }
    Assert.assertTrue(error);

    error = false;
    try {
      ServerNodeParser.parse("127.0.0.1:12345?x");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      error = true;
    }
    Assert.assertTrue(error);

    error = false;
    try {
      ServerNodeParser.parse("127.0.0.1:12345?x=");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      error = true;
    }
    Assert.assertTrue(error);

    error = false;
    try {
      ServerNodeParser.parse("127.0.0.1:12345?x=1&");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalArgumentException);
      error = true;
    }
    Assert.assertTrue(error);

    error = false;
    String url = "127.0.0.2:12345";
    ServerNode node = ServerNodeParser.parse(url);

    Assert.assertEquals("127.0.0.2", node.getHost());
    Assert.assertEquals(12345, node.getPort());

    url = "127.0.0.3:23456?x=1&y=2&z=3";
    node = ServerNodeParser.parse(url);
    Assert.assertEquals("127.0.0.3", node.getHost());
    Assert.assertEquals(23456, node.getPort());
  }
}
