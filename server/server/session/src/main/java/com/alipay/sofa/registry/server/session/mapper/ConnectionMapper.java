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
package com.alipay.sofa.registry.server.session.mapper;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;

/**
 * @author ruoshan
 * @since 5.4.1
 */
public class ConnectionMapper {

  /** <connectionIp:connectionPort, clientIp > * */
  private final ConcurrentHashMap<String, String> connectionToClientIpMap =
      new ConcurrentHashMap<>();

  /**
   * 添加连接到 clientIp 的映射
   *
   * @param connectId 连接 ip:port
   * @param clientIp 客户端 ip
   */
  public void add(String connectId, String clientIp) {
    if (StringUtils.isNotEmpty(clientIp)) {
      connectionToClientIpMap.putIfAbsent(connectId, clientIp);
    }
  }

  /**
   * 删除连接到 clientIp 的映射
   *
   * @param connectId 连接 ip:port
   */
  public void remove(String connectId) {
    connectionToClientIpMap.remove(connectId);
  }

  /**
   * 是否包含连接到 clientIp 的映射
   *
   * @param connectId connectId
   * @return boolean
   */
  public boolean contains(String connectId) {
    return connectionToClientIpMap.containsKey(connectId);
  }

  /**
   * 获取连接对应的 clientIp
   *
   * @param connectId 连接 ip:port
   * @return clientIp
   */
  public String get(String connectId) {
    return connectionToClientIpMap.get(connectId);
  }

  /**
   * 获取连接映射的 size
   *
   * @return 连接映射的 size
   */
  public int size() {
    return connectionToClientIpMap.size();
  }

  /**
   * 获取所有的连接映射
   *
   * @return 连接映射表
   */
  public Map<String, String> get() {
    return Maps.newHashMap(connectionToClientIpMap);
  }
}
