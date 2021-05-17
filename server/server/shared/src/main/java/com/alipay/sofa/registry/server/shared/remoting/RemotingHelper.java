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
package com.alipay.sofa.registry.server.shared.remoting;

import com.alipay.sofa.registry.remoting.Channel;
import java.net.InetSocketAddress;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-14 11:39 yuzhi.lyz Exp $
 */
public final class RemotingHelper {
  private RemotingHelper() {}

  public static InetSocketAddress getChannelRemoteAddress(Channel channel) {
    return channel == null ? null : channel.getRemoteAddress();
  }

  public static String getRemoteHostAddress(Channel channel) {
    return channel == null ? null : channel.getRemoteAddress().getAddress().getHostAddress();
  }
}
