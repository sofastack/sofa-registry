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

import com.alipay.sofa.registry.client.api.model.ConfigData;

/**
 * The interface Configurator.
 *
 * @author zhuoyu.sjw
 * @version $Id : Configurator.java, v 0.1 2018-04-17 17:26 zhuoyu.sjw Exp $$
 */
public interface Configurator extends Register {

  /**
   * Gets data observer.
   *
   * @return the data observer
   */
  ConfigDataObserver getDataObserver();

  /**
   * Sets config data observer.
   *
   * @param configDataObserver the config data observer
   */
  void setDataObserver(ConfigDataObserver configDataObserver);

  /**
   * Peek data config data.
   *
   * @return the config data
   */
  ConfigData peekData();
}
