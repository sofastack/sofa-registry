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
package com.alipay.sofa.registry.log;

import java.io.File;
import java.io.Serializable;
import org.slf4j.LoggerFactory;

/**
 * @author shangyu.wh
 * @version $Id: SLF4JLogger.java, v 0.1 2017-12-22 16:04 shangyu.wh Exp $
 */
public class SLF4JLogger implements Logger, Serializable {

  private static final long serialVersionUID = 1L;

  private static final String LOG_PATH = "logging.path";
  private static final String LOG_PATH_DEFAULT =
      System.getProperty("user.home") + File.separator + "logs";
  private static final String SERVER_LOG_LEVEL = "com.alipay.sofa.registry.server.log.level";
  private static final String SERVER_LOG_LEVEL_DEFAULT = "INFO";

  private final String name;

  private final org.slf4j.Logger logger;

  private final String msgPrefix;

  private final SafeLogger safeLogger = SafeLogger.getInstance();

  /** symbol : */
  public static final char SPACE = ' ';

  static {
    String logPath = System.getProperty(LOG_PATH);
    String logLevel = System.getProperty(SERVER_LOG_LEVEL);
    if (logPath == null || logPath.isEmpty()) {
      System.setProperty(LOG_PATH, LOG_PATH_DEFAULT);
    }
    if (logLevel == null || logLevel.isEmpty()) {
      System.setProperty(SERVER_LOG_LEVEL, SERVER_LOG_LEVEL_DEFAULT);
    }
  }

  /** @param name */
  public SLF4JLogger(String name) {
    this.name = name;
    this.msgPrefix = "";
    this.logger = getLoggerBySpace(name);
  }

  /** @param clazz */
  public SLF4JLogger(Class clazz) {
    String loggerName = clazz.getCanonicalName();
    if (loggerName == null) {
      loggerName = clazz.getName();
    }
    this.name = loggerName;
    this.msgPrefix = "";
    this.logger = getLoggerBySpace(name);
  }

  /**
   * @param name name
   * @param msgPrefix msgPrefix
   */
  public SLF4JLogger(String name, String msgPrefix) {
    this.name = name;
    this.msgPrefix = msgPrefix;
    this.logger = getLoggerBySpace(name);
  }

  /**
   * @param clazz clazz
   * @param msgPrefix msgPrefix
   */
  public SLF4JLogger(Class clazz, String msgPrefix) {
    this.name = clazz.getCanonicalName();
    this.msgPrefix = msgPrefix;
    this.logger = getLoggerBySpace(name);
  }

  /**
   * @param name name
   * @return Logger
   */
  public org.slf4j.Logger getLoggerBySpace(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    return LoggerFactory.getLogger(name);
  }

  @Override
  public void trace(String msg) {
    if (logger.isTraceEnabled()) {
      logger.trace(processMsg(msg));
    }
  }

  @Override
  public void trace(String format, Object arg) {
    if (logger.isTraceEnabled()) {
      logger.trace(processMsg(format), arg);
    }
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    if (logger.isTraceEnabled()) {
      logger.trace(processMsg(format), arg1, arg2);
    }
  }

  @Override
  public void trace(String format, Object... arguments) {
    if (logger.isTraceEnabled()) {
      logger.trace(processMsg(format), arguments);
    }
  }

  @Override
  public void trace(String msg, Throwable e) {
    if (logger.isTraceEnabled()) {
      logger.trace(processMsg(msg), e);
    }
  }

  @Override
  public void debug(String msg) {
    if (logger.isDebugEnabled()) {
      logger.debug(processMsg(msg));
    }
  }

  @Override
  public void debug(String format, Object arg) {
    if (logger.isDebugEnabled()) {
      logger.debug(processMsg(format), arg);
    }
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    if (logger.isDebugEnabled()) {
      logger.debug(processMsg(format), arg1, arg2);
    }
  }

  @Override
  public void debug(String format, Object... arguments) {
    if (logger.isDebugEnabled()) {
      logger.debug(processMsg(format), arguments);
    }
  }

  @Override
  public void debug(String msg, Throwable e) {
    if (logger.isDebugEnabled()) {
      logger.debug(processMsg(msg), e);
    }
  }

  @Override
  public void info(String msg) {
    if (logger.isInfoEnabled()) {
      logger.info(processMsg(msg));
    }
  }

  @Override
  public void info(String format, Object arg) {
    if (logger.isInfoEnabled()) {
      logger.info(processMsg(format), arg);
    }
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    if (logger.isInfoEnabled()) {
      logger.info(processMsg(format), arg1, arg2);
    }
  }

  @Override
  public void info(String format, Object... arguments) {
    if (logger.isInfoEnabled()) {
      logger.info(processMsg(format), arguments);
    }
  }

  @Override
  public void info(String msg, Throwable e) {
    if (logger.isInfoEnabled()) {
      logger.info(processMsg(msg), e);
    }
  }

  @Override
  public void warn(String msg) {
    if (logger.isWarnEnabled()) {
      logger.warn(processMsg(msg));
    }
  }

  @Override
  public void warn(String format, Object arg) {
    if (logger.isWarnEnabled()) {
      logger.warn(processMsg(format), arg);
    }
  }

  @Override
  public void warn(String format, Object... arguments) {
    if (logger.isWarnEnabled()) {
      logger.warn(processMsg(format), arguments);
    }
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    if (logger.isWarnEnabled()) {
      logger.warn(processMsg(format), arg1, arg2);
    }
  }

  @Override
  public void warn(String msg, Throwable e) {
    if (logger.isWarnEnabled()) {
      logger.warn(processMsg(msg), e);
    }
  }

  @Override
  public void error(String msg) {
    if (logger.isErrorEnabled()) {
      logger.error(processMsg(msg));
    }
  }

  @Override
  public void error(String format, Object arg) {
    if (logger.isErrorEnabled()) {
      logger.error(processMsg(format), arg);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    if (logger.isErrorEnabled()) {
      logger.error(processMsg(format), arg1, arg2);
    }
  }

  @Override
  public void error(String format, Object... arguments) {
    if (logger.isErrorEnabled()) {
      logger.error(processMsg(format), arguments);
    }
  }

  @Override
  public void error(String msg, Throwable e) {
    if (logger.isErrorEnabled()) {
      logger.error(processMsg(msg), e);
    }
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  private String processMsg(String msg) {
    if (msgPrefix.isEmpty()) {
      return msg;
    }
    StringBuilder sb = new StringBuilder(msgPrefix.length() + 1 + msg == null ? 8 : msg.length());
    return sb.append(msgPrefix).append(SPACE).append(msg).toString();
  }

  @Override
  public Object getLogger() {
    return logger;
  }

  public String getName() {
    return name;
  }

  @Override
  public void safeError(String format, Object arg1, Object arg2, Object arg3, Throwable arg4) {
    try {
      logger.error(format, arg1, arg2, arg3, arg4);
    } catch (Throwable e) {
      safeLogger.handleExp(e);
    }
  }

  @Override
  public void safeError(String format, Object arg1, Object arg2, Throwable arg3) {
    try {
      logger.error(format, arg1, arg2, arg3);
    } catch (Throwable e) {
      safeLogger.handleExp(e);
    }
  }

  @Override
  public void safeError(String format, Object arg1, Throwable arg2) {
    try {
      logger.error(format, arg1, arg2);
    } catch (Throwable e) {
      safeLogger.handleExp(e);
    }
  }

  @Override
  public void safeError(String format, Throwable arg1) {
    try {
      logger.error(format, arg1);
    } catch (Throwable e) {
      safeLogger.handleExp(e);
    }
  }

  @Override
  public String toString() {
    return "SLF4JLogger{"
        + "name='"
        + name
        + '\''
        + ", msgPrefix='"
        + msgPrefix
        + '\''
        + ", logger='"
        + logger
        + '\''
        + '}';
  }
}
