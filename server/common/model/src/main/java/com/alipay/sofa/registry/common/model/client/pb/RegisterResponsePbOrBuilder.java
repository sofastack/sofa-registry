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

public interface RegisterResponsePbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:RegisterResponsePb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>bool success = 1;</code> */
  boolean getSuccess();

  /** <code>string registId = 2;</code> */
  java.lang.String getRegistId();
  /** <code>string registId = 2;</code> */
  com.google.protobuf.ByteString getRegistIdBytes();

  /** <code>int64 version = 3;</code> */
  long getVersion();

  /** <code>bool refused = 4;</code> */
  boolean getRefused();

  /** <code>string message = 5;</code> */
  java.lang.String getMessage();
  /** <code>string message = 5;</code> */
  com.google.protobuf.ByteString getMessageBytes();
}
