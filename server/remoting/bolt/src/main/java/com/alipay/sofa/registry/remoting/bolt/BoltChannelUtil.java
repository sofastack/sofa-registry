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

import com.alipay.remoting.InvokeContext;
import com.alipay.sofa.registry.remoting.Channel;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class BoltChannelUtil {
  public static Byte getBoltCustomSerializer(Channel channel) {
    if (channel instanceof BoltChannel) {
      BoltChannel boltChannel = (BoltChannel) channel;
      InvokeContext invokeContext = boltChannel.getBizContext().getInvokeContext();

      if (null != invokeContext) {
        // set client custom codec for request command if not null
        Object clientCustomCodec = invokeContext.get(InvokeContext.BOLT_CUSTOM_SERIALIZER);
        if (null != clientCustomCodec) {
          try {
            return (Byte) clientCustomCodec;
          } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                "Illegal custom codec ["
                    + clientCustomCodec
                    + "], the type of value should be [byte], but now is ["
                    + clientCustomCodec.getClass().getName()
                    + "].");
          }
        }
      }
    }
    return null;
  }
}
