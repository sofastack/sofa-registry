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
package com.alipay.sofa.registry.server.session.wrapper;

import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.remoting.Channel;

/**
 * @author xiaojian.xj
 * @version : RegisterInvokeData.java, v 0.1 2021年08月02日 15:09 xiaojian.xj Exp $
 */
public class RegisterInvokeData {

  private final StoreData storeData;

  private final Channel channel;

  public RegisterInvokeData(StoreData storeData, Channel channel) {
    this.storeData = storeData;
    this.channel = channel;
  }

  /**
   * Getter method for property <tt>storeData</tt>.
   *
   * @return property value of storeData
   */
  public StoreData getStoreData() {
    return storeData;
  }

  /**
   * Getter method for property <tt>channel</tt>.
   *
   * @return property value of channel
   */
  public Channel getChannel() {
    return channel;
  }
}
