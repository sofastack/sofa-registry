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
package com.alipay.sofa.registry.client.remoting;

import com.alipay.remoting.exception.RemotingException;

/**
 * The interface Client.
 *
 * @author zhuoyu.sjw
 * @version $Id : Client.java, v 0.1 2018-03-06 20:43 zhuoyu.sjw Exp $$
 */
public interface Client {

  /** Init. */
  void init();

  /**
   * Is connected boolean.
   *
   * @return the boolean
   */
  boolean isConnected();

  /**
   * Ensure connected.
   *
   * @throws InterruptedException the interrupted exception
   */
  void ensureConnected() throws InterruptedException;

  /**
   * Invoke sync object.
   *
   * @param request the request
   * @return the object
   * @throws RemotingException the remoting exception
   * @throws InterruptedException the interrupted exception
   */
  Object invokeSync(Object request) throws RemotingException, InterruptedException;
}
