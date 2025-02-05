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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 * @author qian.lqlq
 * @version $Id: ParaCheckUtil.java, v 0.1 2017-12-06 21:20 qian.lqlq Exp $
 */
public final class ParaCheckUtil {
  private ParaCheckUtil() {}

  /**
   * check object not null
   *
   * @param param param
   * @param paraName paraName
   * @throws RuntimeException RuntimeException
   */
  public static void checkNotNull(Object param, String paraName) {
    if (param == null) {
      throw new IllegalArgumentException(
          StringFormatter.format("{} is not allowed to be null", paraName));
    }
  }

  public static void checkNull(Object param, String paraName) {
    if (param != null) {
      throw new IllegalArgumentException(StringFormatter.format("{} must be null", paraName));
    }
  }

  public static void assertTrue(boolean expression, String message) {
    if (!expression) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void assertFalse(boolean expression, String message) {
    if (expression) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void checkEquals(Object actual, Object expect, String paraName) {
    if (!Objects.equals(actual, expect)) {
      throw new IllegalArgumentException(
          StringFormatter.format("{}={} is not equals {}", paraName, actual, expect));
    }
  }

  /**
   * check string not blank
   *
   * @param param param
   * @param paraName paraName
   * @throws RuntimeException RuntimeException
   */
  public static void checkNotBlank(String param, String paraName) {
    if (StringUtils.isBlank(param)) {
      throw new IllegalArgumentException(
          StringFormatter.format("{} is not allowed to be blank", paraName));
    }
  }

  /**
   * check param not empty
   *
   * @param param param
   * @param paraName paraName
   * @throws RuntimeException RuntimeException
   */
  public static void checkNotEmpty(Collection<?> param, String paraName) {
    if (param == null || param.size() == 0) {
      throw new IllegalArgumentException(
          StringFormatter.format("{} is not allowed to be empty", paraName));
    }
  }

  /**
   * check param not empty
   *
   * @param param param
   * @param paraName paraName
   * @throws RuntimeException RuntimeException
   */
  public static void checkNotEmpty(Map param, String paraName) {
    if (param == null || param.size() == 0) {
      throw new IllegalArgumentException(
          StringFormatter.format("{} is not allowed to be empty", paraName));
    }
  }

  public static void checkNonNegative(long v, String paraName) {
    if (v < 0) {
      throw new IllegalArgumentException(
          StringFormatter.format("{} is not allowed to be negative, {}", paraName, v));
    }
  }

  public static void checkIsPositive(long v, String paraName) {
    if (v <= 0) {
      throw new IllegalArgumentException(
          StringFormatter.format("{} is require positive, {}", paraName, v));
    }
  }

  public static void checkContains(Set sets, Object param, String paraName) {
    if (!sets.contains(param)) {
      throw new IllegalArgumentException(
          StringFormatter.format("{} is not contain in {}, {}", paraName, sets, param));
    }
  }
}
