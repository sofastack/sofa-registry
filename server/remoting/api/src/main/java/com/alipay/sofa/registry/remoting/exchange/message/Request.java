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
package com.alipay.sofa.registry.remoting.exchange.message;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The interface Request.
 *
 * @param <T> the type parameter
 * @author shangyu.wh
 * @version $Id : Request.java, v 0.1 2017-11-30 17:33 shangyu.wh Exp $
 */
public interface Request<T> {

  /**
   * Gets request body.
   *
   * @return the request body
   */
  T getRequestBody();

  /**
   * Gets request url.
   *
   * @return the request url
   */
  URL getRequestUrl();

  /**
   * Gets call back handler.
   *
   * @return the call back handler
   */
  default CallbackHandler getCallBackHandler() {
    return null;
  }

  /**
   * request send by retry some times
   *
   * @return
   */
  default AtomicInteger getRetryTimes() {
    return new AtomicInteger();
  }

  default Integer getTimeout() {
    return null;
  }
}
