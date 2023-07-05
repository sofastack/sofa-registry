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
package com.alipay.sofa.registry.server.session.cache;

import com.alipay.sofa.registry.cache.Sizer;

/**
 * cache return result
 *
 * @author shangyu.wh
 * @version $Id: Value.java, v 0.1 2017-12-06 15:52 shangyu.wh Exp $
 */
public class Value implements Sizer {

  private final Sizer payload;

  /**
   * constructor
   *
   * @param payload payload
   */
  public Value(Sizer payload) {
    this.payload = payload;
  }

  /**
   * Getter method for property <tt>payload</tt>.
   *
   * @return property value of payload
   */
  public Sizer getPayload() {
    return payload;
  }

  @Override
  public int size() {
    if (payload == null) {
      // default size for java header
      return 20;
    }
    return payload.size();
  }
}
