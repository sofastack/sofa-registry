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

public class SimpleRequest<T> implements Request<T> {
  private final T req;
  private final URL url;
  private final CallbackHandler callback;

  public SimpleRequest(T req, URL url) {
    this(req, url, null);
  }

  public SimpleRequest(T req, URL url, CallbackHandler callback) {
    this.req = req;
    this.url = url;
    this.callback = callback;
  }

  @Override
  public T getRequestBody() {
    return req;
  }

  @Override
  public URL getRequestUrl() {
    return url;
  }

  @Override
  public CallbackHandler getCallBackHandler() {
    return callback;
  }
}
