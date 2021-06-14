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
package com.alipay.sofa.registry.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-22 17:30 yuzhi.lyz Exp $
 */
public final class JsonUtils {
  public static final ThreadLocal<ObjectMapper> JACKSON_MAPPER =
      ThreadLocal.withInitial(() -> new ObjectMapper());

  private JsonUtils() {}

  public static ObjectMapper getJacksonObjectMapper() {
    return JACKSON_MAPPER.get();
  }

  public static <T> T read(String str, Class<T> clazz) {
    try {
      return JACKSON_MAPPER.get().readValue(str, clazz);
    } catch (Throwable e) {
      throw new RuntimeException("failed to read json to " + clazz.getName() + ", " + str, e);
    }
  }

  public static <T> T read(String str, TypeReference<T> typeReference) {
    try {
      return JACKSON_MAPPER.get().readValue(str, typeReference);
    } catch (Throwable e) {
      throw new RuntimeException(
          "failed to read json to " + typeReference.toString() + ", " + str, e);
    }
  }

  public static String writeValueAsString(Object o) {
    try {
      return JACKSON_MAPPER.get().writeValueAsString(o);
    } catch (Throwable e) {
      throw new RuntimeException("failed to write json: " + o, e);
    }
  }
}
