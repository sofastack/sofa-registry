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

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.serialization.Serializer;
import com.google.protobuf.MessageLite;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanggeng on 2017/2/23.
 *
 * @author zhanggeng
 * @version $Id : ProtobufSerializer.java, v 0.1 2018-05-05 23:43 zhanggeng Exp $$
 */
public class ProtobufSerializer implements Serializer {

  public static final byte PROTOCOL_PROTOBUF = 11;

  /** cache parse method */
  private final ConcurrentHashMap<Class, Method> parseMethodMap = new ConcurrentHashMap<>();

  /** cache toByteArray method */
  private final ConcurrentHashMap<Class, Method> toByteArrayMethodMap = new ConcurrentHashMap<>();

  private static final ProtobufSerializer instance = new ProtobufSerializer();

  private ProtobufSerializer() {}

  public static ProtobufSerializer getInstance() {
    return instance;
  }

  @Override
  public byte[] serialize(Object object) throws CodecException {
    if (object == null) {
      throw new SerializationException("Unsupported null message");
    } else if (isProtoBufMessageLite(object)) {
      Class clazz = object.getClass();
      Method method = toByteArrayMethodMap.get(clazz);
      if (method == null) {
        try {
          method = clazz.getMethod("toByteArray");
          method.setAccessible(true);
          toByteArrayMethodMap.put(clazz, method);
        } catch (Exception e) {
          throw new SerializationException(
              "Cannot found method "
                  + clazz.getName()
                  + ".toByteArray(), please check the generated code");
        }
      }
      try {
        return (byte[]) method.invoke(object);
      } catch (Exception e) {
        throw new SerializationException(
            "Cannot found method "
                + clazz.getName()
                + ".toByteArray(), please check the generated code");
      }

    } else if (object instanceof String) {
      return ((String) object).getBytes(StandardCharsets.UTF_8);
    } else {
      throw new SerializationException(
          "Unsupported class:" + object.getClass().getName() + ", only support protobuf message");
    }
  }

  @Override
  public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
    try {
      Class requestClass = Class.forName(classOfT);
      return (T) decode(data, requestClass);
    } catch (ClassNotFoundException e) {
      throw new SerializationException("Cannot found class " + classOfT, e);
    }
  }

  public Object decode(byte[] bytes, Class clazz) throws DeserializationException {
    if (isProtoBufMessageLite(clazz)) {
      try {
        Method method = parseMethodMap.get(clazz);
        if (method == null) {
          method = clazz.getMethod("parseFrom", byte[].class);
          if (!Modifier.isStatic(method.getModifiers())) {
            throw new CodecException(
                "Cannot found method "
                    + clazz.getName()
                    + ".parseFrom(byte[]), please check the generated code");
          }
          method.setAccessible(true);
          parseMethodMap.put(clazz, method);
        }
        return method.invoke(null, bytes);
      } catch (DeserializationException e) {
        throw e;
      } catch (Exception e) {
        throw new DeserializationException(
            "Cannot found method "
                + clazz.getName()
                + ".parseFrom(byte[]), please check the generated code",
            e);
      }
    } else if (clazz == String.class) {
      return new String(bytes, StandardCharsets.UTF_8);
    } else {
      throw new DeserializationException(
          "Unsupported class:" + clazz.getName() + ", only support protobuf message");
    }
  }

  public static boolean isProtoBufMessageLite(Object object) {
    Class clzz = object == null ? null : object.getClass();
    return isProtoBufMessageLite(clzz);
  }

  private static boolean isProtoBufMessageLite(Class clzz) {
    if (clzz != null) {
      return isSpecificationInterface(clzz, MessageLite.class.getCanonicalName());
    }
    return false;
  }

  /**
   * whether clazz has a interface named interfaceName
   *
   * @param clazz
   * @param interfaceName
   * @return
   */
  private static boolean isSpecificationInterface(Class<?> clazz, String interfaceName) {

    boolean find = false;

    // First, get all direct interface
    Class<?>[] interfaces = clazz.getInterfaces();
    if (interfaces.length > 0) {
      for (Class<?> interfaze : interfaces) {
        find = interfaceName.equalsIgnoreCase(interfaze.getCanonicalName());
        if (find) {
          break;
        }
      }
    }
    while (!Object.class.getCanonicalName().equals(clazz.getCanonicalName()) && !find) {
      // Add the super class
      Class<?> superClass = clazz.getSuperclass();

      // Interfaces does not have java,lang.Object as superclass, they have null, so break the cycle
      // and return
      if (superClass == null) {
        break;
      }

      // Now inspect the superclass
      clazz = superClass;

      find = isSpecificationInterface(clazz, interfaceName);
    }

    return find;
  }
}
