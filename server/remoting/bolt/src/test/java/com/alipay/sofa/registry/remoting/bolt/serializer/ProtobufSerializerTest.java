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
import com.alipay.remoting.exception.SerializationException;
import com.alipay.sofa.registry.remoting.bolt.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class ProtobufSerializerTest {
  @Test
  public void testException() {
    ProtobufSerializer serializer = ProtobufSerializer.getInstance();
    TestUtils.assertRunException(SerializationException.class, () -> serializer.serialize(null));
    TestUtils.assertRunException(
        SerializationException.class, () -> serializer.serialize(new Integer(10)));
    TestUtils.assertRunException(
        SerializationException.class, () -> serializer.deserialize(null, "classNotExist"));
    TestUtils.assertRunException(
        DeserializationException.class, () -> serializer.decode(null, Integer.class));

    Assert.assertFalse(ProtobufSerializer.isProtoBufMessageLite(null));
    Assert.assertFalse(ProtobufSerializer.isProtoBufMessageLite(new Integer(10)));
    Assert.assertFalse(ProtobufSerializer.isProtoBufMessageLite("xx"));
  }
}
