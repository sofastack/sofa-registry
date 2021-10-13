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
package com.alipay.sofa.registry.common.model.store;

import org.apache.commons.lang.StringUtils;

public class PushData<T> {
  private final T payload;
  private final int dataCount;
  private final String encode;
  private final int encodeSize;

  public PushData(T payload, int dataCount) {
    this(payload, dataCount, StringUtils.EMPTY, 0);
  }

  public PushData(T payload, int dataCount, String encode, int encodeSize) {
    this.payload = payload;
    this.dataCount = dataCount;
    this.encode = encode;
    this.encodeSize = encodeSize;
  }

  public T getPayload() {
    return payload;
  }

  public int getDataCount() {
    return dataCount;
  }

  public String getEncode() {
    return encode;
  }

  public int getEncodeSize() {
    return encodeSize;
  }
}
