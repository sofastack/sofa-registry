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

public interface SubscriberRegisterPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:SubscriberRegisterPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string scope = 1;</code>
   *
   * @return The scope.
   */
  java.lang.String getScope();
  /**
   * <code>string scope = 1;</code>
   *
   * @return The bytes for scope.
   */
  com.google.protobuf.ByteString getScopeBytes();

  /**
   * <code>.BaseRegisterPb baseRegister = 2;</code>
   *
   * @return Whether the baseRegister field is set.
   */
  boolean hasBaseRegister();
  /**
   * <code>.BaseRegisterPb baseRegister = 2;</code>
   *
   * @return The baseRegister.
   */
  com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb getBaseRegister();
  /** <code>.BaseRegisterPb baseRegister = 2;</code> */
  com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder
      getBaseRegisterOrBuilder();

  /**
   * <code>string acceptEncoding = 3;</code>
   *
   * @return The acceptEncoding.
   */
  java.lang.String getAcceptEncoding();
  /**
   * <code>string acceptEncoding = 3;</code>
   *
   * @return The bytes for acceptEncoding.
   */
  com.google.protobuf.ByteString getAcceptEncodingBytes();

  /**
   * <code>bool acceptMulti = 4;</code>
   *
   * @return The acceptMulti.
   */
  boolean getAcceptMulti();
}
