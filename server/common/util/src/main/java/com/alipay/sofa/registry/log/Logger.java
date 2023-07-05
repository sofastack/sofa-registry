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

/**
 * import slf4j
 *
 * @author shangyu.wh
 * @version $Id: Logger.java, v 0.1 2017-12-22 15:56 shangyu.wh Exp $
 */
public interface Logger {

  /**
   * TRACE level
   *
   * @return boolean boolean
   */
  boolean isTraceEnabled();

  /**
   * Log TRACE level
   *
   * @param msg msg
   */
  void trace(String msg);

  /**
   * Log TRACE level
   *
   * @param format format
   * @param arg arg
   */
  void trace(String format, Object arg);

  /**
   * Log TRACE level
   *
   * @param format format
   * @param arg1 format
   * @param arg2 arg2
   */
  void trace(String format, Object arg1, Object arg2);

  /**
   * Log TRACE level
   *
   * @param format format
   * @param arguments arguments
   */
  void trace(String format, Object... arguments);

  /**
   * Log TRACE level
   *
   * @param msg msg
   * @param t t
   */
  void trace(String msg, Throwable t);

  /**
   * DEBUG level
   *
   * @return boolean boolean
   */
  boolean isDebugEnabled();

  /**
   * Log DEBUG level.
   *
   * @param msg msg
   */
  void debug(String msg);

  /**
   * Log DEBUG level.
   *
   * @param format format
   * @param arg arg
   */
  void debug(String format, Object arg);

  /**
   * Log DEBUG level.
   *
   * @param format format
   * @param arg1 arg1
   * @param arg2 arg2
   */
  void debug(String format, Object arg1, Object arg2);

  /**
   * Log DEBUG level.
   *
   * @param format format
   * @param arguments arguments
   */
  void debug(String format, Object... arguments);

  /**
   * Log DEBUG level.
   *
   * @param msg msg
   * @param t t
   */
  void debug(String msg, Throwable t);

  /**
   * INFO level
   *
   * @return True if this Logger is enabled for the INFO level, false otherwise.
   */
  boolean isInfoEnabled();

  /**
   * Log INFO level.
   *
   * @param msg msg
   */
  void info(String msg);

  /**
   * Log INFO level.
   *
   * @param format format
   * @param arg arg
   */
  void info(String format, Object arg);

  /**
   * Log INFO level.
   *
   * @param format format
   * @param arg1 arg1
   * @param arg2 arg2
   */
  void info(String format, Object arg1, Object arg2);

  /**
   * Log INFO level.
   *
   * @param format format
   * @param arguments arguments
   */
  void info(String format, Object... arguments);

  /**
   * Log INFO level.
   *
   * @param msg msg
   * @param t t
   */
  void info(String msg, Throwable t);

  /**
   * WARN level
   *
   * @return boolean boolean
   */
  boolean isWarnEnabled();

  /**
   * Log WARN level.
   *
   * @param msg msg
   */
  void warn(String msg);

  /**
   * Log WARN level.
   *
   * @param format format
   * @param arg arg
   */
  void warn(String format, Object arg);

  /**
   * Log WARN level.
   *
   * @param format format
   * @param arguments arguments
   */
  void warn(String format, Object... arguments);

  /**
   * Log WARN level.
   *
   * @param format format
   * @param arg1 arg1
   * @param arg2 arg2
   */
  void warn(String format, Object arg1, Object arg2);

  /**
   * Log WARN level.
   *
   * @param msg msg
   * @param t t
   */
  void warn(String msg, Throwable t);

  /**
   * ERROR level
   *
   * @return boolean boolean
   */
  boolean isErrorEnabled();

  /**
   * Log ERROR level.
   *
   * @param msg the message string to be logged
   */
  void error(String msg);

  /**
   * Log ERROR level.
   *
   * @param format format
   * @param arg arg
   */
  void error(String format, Object arg);

  /**
   * Log ERROR level.
   *
   * @param format format
   * @param arg1 arg1
   * @param arg2 arg2
   */
  void error(String format, Object arg1, Object arg2);

  /**
   * Log ERROR level.
   *
   * @param format format
   * @param arguments arguments
   */
  void error(String format, Object... arguments);

  /**
   * Log ERROR level.
   *
   * @param msg msg
   * @param t t
   */
  void error(String msg, Throwable t);

  /**
   * get actually logger
   *
   * @return Object Object
   */
  Object getLogger();

  /** @return String string */
  String getName();

  // zero allocate arguments
  void safeError(String format, Object arg1, Object arg2, Object arg3, Throwable arg4);

  void safeError(String format, Object arg1, Object arg2, Throwable arg3);

  void safeError(String format, Object arg1, Throwable arg2);

  void safeError(String format, Throwable arg1);
}
