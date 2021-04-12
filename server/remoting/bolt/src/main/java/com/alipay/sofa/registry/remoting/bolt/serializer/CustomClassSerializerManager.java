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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Custom class serializer manager.
 *
 * @author zhuoyu.sjw
 * @version $Id : CustomClassSerializerManager.java, v 0.1 2018-05-05 23:43 zhuoyu.sjw Exp $$
 */
public final class CustomClassSerializerManager {

  private static final ConcurrentMap<Class, Byte> serializerMap = new ConcurrentHashMap<>();

  private CustomClassSerializerManager() {}
  /**
   * Register serializer.
   *
   * @param clazz the clazz
   * @param serializer the serializer
   */
  public static void registerSerializer(Class clazz, Byte serializer) {
    if (clazz == null || serializer == null) {
      throw new IllegalArgumentException("class and serializer can not be null");
    }
    serializerMap.put(clazz, serializer);
  }

  /**
   * Gets protocol.
   *
   * @param clazz the clazz
   * @return the protocol
   */
  public static Byte getClassSerializer(Class clazz) {
    return serializerMap.get(clazz);
  }
}
