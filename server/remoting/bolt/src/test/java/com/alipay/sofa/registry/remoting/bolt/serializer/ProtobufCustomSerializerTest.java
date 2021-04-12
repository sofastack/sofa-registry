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
package com.alipay.sofa.registry.remoting.bolt.serializer;

import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.rpc.HeartbeatAckCommand;
import com.alipay.remoting.rpc.HeartbeatCommand;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.remoting.rpc.protocol.RpcResponseCommand;
import org.junit.Assert;
import org.junit.Test;

public class ProtobufCustomSerializerTest {

  @Test
  public void testHeader() {
    ProtobufCustomSerializer serializer = new ProtobufCustomSerializer();
    Assert.assertFalse(serializer.serializeHeader(null, null));
    Assert.assertFalse(serializer.serializeHeader(null));
    Assert.assertFalse(serializer.deserializeHeader(null, null));
    Assert.assertFalse(serializer.deserializeHeader(null));
  }

  @Test
  public void testSerdeNotRpcCommand() throws Exception {
    ProtobufCustomSerializer serializer = new ProtobufCustomSerializer();

    Assert.assertFalse(serializer.serializeContent(new HeartbeatCommand(), null));
    Assert.assertFalse(serializer.serializeContent(new HeartbeatAckCommand()));

    Assert.assertFalse(serializer.deserializeContent(new HeartbeatCommand()));
    Assert.assertFalse(serializer.deserializeContent(new HeartbeatAckCommand(), null));
  }

  @Test
  public void testSerdeReq() throws Exception {
    ProtobufCustomSerializer serializer = new ProtobufCustomSerializer();
    RpcRequestCommand command = new RpcRequestCommand("testObj");
    Assert.assertTrue(serializer.serializeContent(command, null));

    Assert.assertNotNull(command.getContent());
    Assert.assertFalse(command.getContentLength() == 0);

    command.setSerializer(ProtobufCustomSerializer.PROTOCOL_PROTOBUF);
    // not set clazz, exception
    try {
      serializer.deserializeContent(command);
      Assert.fail("expect DeserializationException");
    } catch (DeserializationException e) {
    }

    command.setRequestClass(String.class.getName());
    Assert.assertTrue(serializer.deserializeContent(command));
    Assert.assertEquals(command.getRequestObject(), "testObj");
  }

  @Test
  public void testSerdeRep() throws Exception {
    ProtobufCustomSerializer serializer = new ProtobufCustomSerializer();
    RpcResponseCommand command = new RpcResponseCommand("testObj");
    command.setSerializer(ProtobufCustomSerializer.PROTOCOL_PROTOBUF);
    command.setResponseObject("testObj");
    Assert.assertTrue(serializer.serializeContent(command));

    Assert.assertNotNull(command.getContent());
    Assert.assertFalse(command.getContentLength() == 0);

    // not set clazz, exception
    try {
      serializer.deserializeContent(command, null);
      Assert.fail("expect DeserializationException");
    } catch (DeserializationException e) {
    }

    command.setResponseClass(String.class.getName());
    Assert.assertTrue(serializer.deserializeContent(command, null));
    Assert.assertEquals(command.getResponseObject(), "testObj");
  }
}
