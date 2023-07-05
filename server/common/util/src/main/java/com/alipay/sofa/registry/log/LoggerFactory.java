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
 * @author shangyu.wh
 * @version $Id: LoggerFactory.java, v 0.1 2017-12-22 15:55 shangyu.wh Exp $
 */
public class LoggerFactory {

  /**
   * get logger
   *
   * @param name name
   * @return Logger
   */
  public static Logger getLogger(String name) {
    return new SLF4JLogger(name);
  }

  /**
   * get logger
   *
   * @param clazz clazz
   * @return
   */
  public static Logger getLogger(Class clazz) {
    return new SLF4JLogger(clazz);
  }

  /**
   * get logger
   *
   * @param name name
   * @param prefix prefix
   * @return
   */
  public static Logger getLogger(String name, String prefix) {
    return new SLF4JLogger(name, prefix);
  }

  /**
   * get logger
   *
   * @param clazz clazz
   * @param prefix prefix
   * @return Logger
   */
  public static Logger getLogger(Class clazz, String prefix) {
    return new SLF4JLogger(clazz, prefix);
  }
}
