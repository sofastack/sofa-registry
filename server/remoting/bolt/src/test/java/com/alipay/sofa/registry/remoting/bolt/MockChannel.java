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

import com.alipay.sofa.registry.remoting.Channel;
import java.net.InetSocketAddress;
import javax.ws.rs.client.WebTarget;

/**
 * @author xuanbei
 * @since 2019/3/26
 */
public class MockChannel implements Channel {
  @Override
  public InetSocketAddress getRemoteAddress() {
    return null;
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return null;
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public Object getAttribute(String key) {
    return null;
  }

  @Override
  public void setAttribute(String key, Object value) {}

  @Override
  public Object getConnAttribute(String key) {
    return null;
  }

  @Override
  public void setConnAttribute(String key, Object value) {}

  @Override
  public WebTarget getWebTarget() {
    return null;
  }

  @Override
  public void close() {}
}
