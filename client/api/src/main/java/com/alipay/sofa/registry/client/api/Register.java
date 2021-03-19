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
package com.alipay.sofa.registry.client.api;

/**
 * The interface Register.
 *
 * @author zhuoyu.sjw
 * @version $Id : Register.java, v 0.1 2017-11-22 16:00 zhuoyu.sjw Exp $$
 */
public interface Register {

  /** Reset. */
  void reset();

  /**
   * Is registered boolean.
   *
   * @return the boolean
   */
  boolean isRegistered();

  /** Unregister. */
  void unregister();

  /**
   * Gets data id.
   *
   * @return String data id
   */
  String getDataId();

  /**
   * Gets group.
   *
   * @return the group
   */
  String getGroup();

  /**
   * Gets regist id.
   *
   * @return the regist id
   */
  String getRegistId();

  /**
   * Is enabled boolean.
   *
   * @return boolean boolean
   */
  boolean isEnabled();

  /**
   * Gets timestamp.
   *
   * @return the timestamp
   */
  long getTimestamp();
}
