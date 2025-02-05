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

public interface AppListOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:AppList)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 version = 1;</code>
   *
   * @return long
   */
  long getVersion();

  /**
   * <code>repeated string apps = 2;</code>
   *
   * @return List
   */
  java.util.List<java.lang.String> getAppsList();
  /**
   * <code>repeated string apps = 2;</code>
   *
   * @return int
   */
  int getAppsCount();
  /**
   * <code>repeated string apps = 2;</code>
   *
   * @param index index
   * @return String
   */
  java.lang.String getApps(int index);
  /**
   * <code>repeated string apps = 2;</code>
   *
   * @param index index
   * @return ByteString
   */
  com.google.protobuf.ByteString getAppsBytes(int index);
}
