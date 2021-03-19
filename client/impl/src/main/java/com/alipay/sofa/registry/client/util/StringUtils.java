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
package com.alipay.sofa.registry.client.util;

/**
 * The type String utils.
 *
 * @author zhuoyu.sjw
 * @version $Id : StringUtils.java, v 0.1 2018-03-05 17:36 zhuoyu.sjw Exp $$
 */
public class StringUtils {

  /** The constant EMPTY. */
  public static final String EMPTY = "";

  /**
   * Is empty boolean.
   *
   * @param cs the cs
   * @return the boolean
   */
  public static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  /**
   * Is not empty boolean.
   *
   * @param cs the cs
   * @return the boolean
   */
  public static boolean isNotEmpty(CharSequence cs) {
    return !isEmpty(cs);
  }

  /**
   * Is blank boolean.
   *
   * @param cs the cs
   * @return the boolean
   */
  public static boolean isBlank(CharSequence cs) {
    int strLen;
    if (cs == null || (strLen = cs.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Is not blank boolean.
   *
   * @param cs the cs
   * @return the boolean
   */
  public static boolean isNotBlank(CharSequence cs) {
    return !isBlank(cs);
  }

  /**
   * Returns either the passed in String, or if the String is {@code null}, an empty String ("").
   *
   * <pre>
   * StringUtils.defaultString(null)  = ""
   * StringUtils.defaultString("")    = ""
   * StringUtils.defaultString("bat") = "bat"
   * </pre>
   *
   * @param str the String to check, may be null
   * @return the passed in String, or the empty String if it was {@code null}
   * @see String#valueOf(Object) String#valueOf(Object)
   */
  public static String defaultString(final Object str) {
    return toString(str, EMPTY);
  }

  /**
   * To string string.
   *
   * @param o the o
   * @param defaultVal the default val
   * @return the string
   */
  public static String toString(Object o, String defaultVal) {
    return o == null ? defaultVal : o.toString();
  }

  /**
   * To string string.
   *
   * @param o the o
   * @return the string
   */
  public static String toString(Object o) {
    return toString(o, null);
  }
}
