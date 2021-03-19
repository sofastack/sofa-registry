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
package com.alipay.sofa.registry.client.log;

import com.alipay.sofa.common.log.LoggerSpaceManager;
import java.io.File;
import org.slf4j.Logger;

/**
 * The type Logger factory.
 *
 * @author zhuoyu.sjw
 * @version $Id : LoggerFactory.java, v 0.1 2018-03-24 12:06 zhuoyu.sjw Exp $$
 */
public class LoggerFactory {

  private static final String LOG_SPACE_PROPERTY = "registry.client.log.space";
  private static final String LOG_PATH = "logging.path";
  private static final String LOG_PATH_DEFAULT =
      System.getProperty("user.home") + File.separator + "logs";
  private static final String CLIENT_LOG_LEVEL = "com.alipay.sofa.registry.client.log.level";
  private static final String CLIENT_LOG_LEVEL_DEFAULT = "INFO";
  private static final String CLIENT_LOG_ENCODE = "com.alipay.sofa.registry.client.log.encode";
  private static final String CLIENT_LOG_ENCODE_DEFAULT = "UTF-8";
  private static String logSpace = "com.alipay.sofa.registry.client";

  static {
    LoggerFactory.logSpace = getLogSpace();

    String logPath = System.getProperty(LOG_PATH);
    String logLevel = System.getProperty(CLIENT_LOG_LEVEL);
    String logEncode = System.getProperty(CLIENT_LOG_ENCODE);
    if (isBlank(logPath)) {
      System.setProperty(LOG_PATH, LOG_PATH_DEFAULT);
    }
    if (isBlank(logLevel)) {
      System.setProperty(CLIENT_LOG_LEVEL, CLIENT_LOG_LEVEL_DEFAULT);
    }
    if (isBlank(logEncode)) {
      System.setProperty(CLIENT_LOG_ENCODE, CLIENT_LOG_ENCODE_DEFAULT);
    }
  }

  static String getLogSpace() {
    String sysLogSpace = System.getProperty(LOG_SPACE_PROPERTY);
    return (null != sysLogSpace && !sysLogSpace.isEmpty()) ? sysLogSpace : logSpace;
  }

  /**
   * Gets logger.
   *
   * @param clazz the clazz
   * @return the logger
   */
  public static Logger getLogger(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    return getLogger(clazz.getCanonicalName());
  }

  /**
   * Gets logger.
   *
   * @param name the name
   * @return the logger
   */
  public static Logger getLogger(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    return LoggerSpaceManager.getLoggerBySpace(name, logSpace);
  }

  static boolean isBlank(CharSequence cs) {
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
}
