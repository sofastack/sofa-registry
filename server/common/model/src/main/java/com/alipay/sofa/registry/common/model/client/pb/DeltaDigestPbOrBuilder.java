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
package com.alipay.sofa.registry.common.model.client.pb;

public interface DeltaDigestPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:DeltaDigestPb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>int64 publisherCount = 1;</code> */
  long getPublisherCount();

  /** <code>int64 dataCount = 2;</code> */
  long getDataCount();

  /** <code>int64 registerIdHashSum = 3;</code> */
  long getRegisterIdHashSum();

  /** <code>int64 registerIdHashXOR = 4;</code> */
  long getRegisterIdHashXOR();

  /** <code>int64 registerTimestampSum = 5;</code> */
  long getRegisterTimestampSum();

  /** <code>int64 registerTimestampXOR = 6;</code> */
  long getRegisterTimestampXOR();
}
