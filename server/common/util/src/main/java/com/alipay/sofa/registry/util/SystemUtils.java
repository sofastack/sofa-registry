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

import org.apache.commons.lang.StringUtils;

public final class SystemUtils {
  private SystemUtils() {}

  public static int getSystemInteger(String name, int def) {
    String v = System.getProperty(name);
    if (v == null) {
      v = System.getenv(convertEnvKey(name));
    }
    return v == null ? def : Integer.parseInt(v);
  }

  public static long getSystemLong(String name, long def) {
    String v = System.getProperty(name);
    if (v == null) {
      v = System.getenv(convertEnvKey(name));
    }
    return v == null ? def : Long.parseLong(v);
  }

  public static String getSystem(String name, String def) {
    String v = System.getProperty(name);
    if (v == null) {
      v = System.getenv(convertEnvKey(name));
    }
    return v == null ? def : v;
  }

  public static String getSystem(String name) {
    return getSystem(name, null);
  }

  private static String convertEnvKey(String key) {
    return StringUtils.replace(key, ".", "_").toUpperCase();
  }
}
