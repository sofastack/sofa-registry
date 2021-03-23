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

public interface DataBoxesPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:DataBoxesPb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>repeated .DataBoxPb data = 1;</code> */
  java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> getDataList();
  /** <code>repeated .DataBoxPb data = 1;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getData(int index);
  /** <code>repeated .DataBoxPb data = 1;</code> */
  int getDataCount();
  /** <code>repeated .DataBoxPb data = 1;</code> */
  java.util.List<? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
      getDataOrBuilderList();
  /** <code>repeated .DataBoxPb data = 1;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getDataOrBuilder(int index);
}
