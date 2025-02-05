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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.remoting.CustomSerializerManager;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.protocol.RpcCommandDecoder;
import com.alipay.remoting.rpc.protocol.RpcCommandEncoder;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb;
import com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.remoting.bolt.serializer.ProtobufCustomSerializer;
import com.alipay.sofa.registry.remoting.bolt.serializer.ProtobufSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author bystander
 * @version $Id: BoltEncodeTest.java, v 0.1 2018年03月21日 5:26 PM bystander Exp $
 */
public class BoltEncodeTest {

  @Test
  public void test() {

    CustomSerializerManager.registerCustomSerializer(
        PublisherRegisterPb.class.getName(), new ProtobufCustomSerializer());

    PublisherRegisterPb.Builder builder = PublisherRegisterPb.newBuilder();

    BaseRegisterPb.Builder baseRegisterBuilder = BaseRegisterPb.newBuilder();

    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put("a", "b");
    baseRegisterBuilder
        .setAppName("test")
        .setClientId("clientId")
        .setDataId("dataId")
        .setDataInfoId("DataInfoId")
        .setEventType("eventType")
        .setGroup("group")
        .setInstanceId("InstanceId")
        .setIp("ip")
        .setPort(1200)
        .setProcessId("ProcessId")
        .setRegistId("RegistId")
        .setVersion(1)
        .setTimestamp(1)
        .putAllAttributes(attributes);

    builder.setBaseRegister(baseRegisterBuilder.build());
    List<DataBox> dataBoxJavas = new ArrayList<DataBox>();
    final DataBox data = new DataBox();
    data.setData("data");
    dataBoxJavas.add(data);
    builder.addAllDataList(DataBoxConvertor.convert2Pbs(dataBoxJavas));

    final PublisherRegisterPb publisherRegisterPb = builder.build();
    RpcRequestCommand command = new RpcRequestCommand(publisherRegisterPb);
    command.setSerializer(ProtobufSerializer.PROTOCOL_PROTOBUF);
    command.setTimeout(-1);
    command.setRequestClass(publisherRegisterPb.getClass().getName());
    try {
      command.serialize();
    } catch (SerializationException e) {
      e.printStackTrace();
    }

    for (byte b : command.getContent()) {
      System.out.print(String.format("%02x", b));
    }
    System.out.println();

    RpcCommandEncoder protocolEncoder = new RpcCommandEncoder();
    ByteBuf byteBuf = Unpooled.buffer();
    try {
      protocolEncoder.encode(null, command, byteBuf);
    } catch (Exception e) {
      e.printStackTrace();
    }

    StringBuilder sb = new StringBuilder(1024);
    ByteBufUtil.appendPrettyHexDump(sb, byteBuf);

    System.out.println(sb);

    RpcCommandDecoder rpcCommandDecoder = new RpcCommandDecoder();

    final ArrayList<Object> out = new ArrayList<>();
    try {
      rpcCommandDecoder.decode(null, byteBuf, out);
    } catch (Exception e) {
      e.printStackTrace();
    }
    RpcRequestCommand rpcRequestCommand = null;
    for (Object o : out) {
      rpcRequestCommand = (RpcRequestCommand) o;
      try {
        rpcRequestCommand.deserialize();
      } catch (DeserializationException e) {
        e.printStackTrace();
      }
    }
    final PublisherRegisterPb requestObject =
        (PublisherRegisterPb) rpcRequestCommand.getRequestObject();
    System.out.println(requestObject);

    Assert.assertEquals(requestObject.getBaseRegister().getAppName(), "test");

    PublisherRegister publisherRegister = PublisherRegisterConvertor.convert2Java(requestObject);

    System.out.println(publisherRegister);
  }
}
