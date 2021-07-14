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
package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.URL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 21:32 yuzhi.lyz Exp $
 */
public final class ConnectId implements Serializable {
  private final String clientHostAddress;
  private final int clientPort;
  private final String sessionHostAddress;
  private final int sessionPort;
  private int hash;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public ConnectId(
      @JsonProperty("clientHostAddress") String clientHostAddress,
      @JsonProperty("clientPort") int clientPort,
      @JsonProperty("sessionHostAddress") String sessionHostAddress,
      @JsonProperty("sessionPort") int sessionPort) {
    this.clientHostAddress = clientHostAddress;
    this.clientPort = clientPort;
    this.sessionHostAddress = sessionHostAddress;
    this.sessionPort = sessionPort;
  }

  public static ConnectId of(String clientAddress, String sessionAddress) {
    URL clientURL = URL.valueOf(clientAddress);
    URL sessionURL = URL.valueOf(sessionAddress);
    return new ConnectId(
        clientURL.getIpAddress(),
        clientURL.getPort(),
        sessionURL.getIpAddress(),
        sessionURL.getPort());
  }

  public static ConnectId of(InetSocketAddress clientAddress, InetSocketAddress sessionAddress) {
    return new ConnectId(
        clientAddress.getAddress().getHostAddress(),
        clientAddress.getPort(),
        sessionAddress.getAddress().getHostAddress(),
        sessionAddress.getPort());
  }

  public static ConnectId parse(String str) {
    String[] strs = str.split(ValueConstants.CONNECT_ID_SPLIT);
    if (strs.length != 2) {
      throw new IllegalArgumentException("unknow format of ConnectId:" + str);
    }
    return of(strs[0], strs[1]);
  }

  /**
   * Getter method for property <tt>clientAddress</tt>.
   *
   * @return property value of clientAddress
   */
  public String getClientHostAddress() {
    return clientHostAddress;
  }

  /**
   * Getter method for property <tt>clientPort</tt>.
   *
   * @return property value of clientPort
   */
  public int getClientPort() {
    return clientPort;
  }

  /**
   * Getter method for property <tt>sessionAddress</tt>.
   *
   * @return property value of sessionAddress
   */
  public String getSessionHostAddress() {
    return sessionHostAddress;
  }

  /**
   * Getter method for property <tt>sessionPort</tt>.
   *
   * @return property value of sessionPort
   */
  public int getSessionPort() {
    return sessionPort;
  }

  public String clientAddress() {
    return clientHostAddress + URL.COLON + clientPort;
  }

  public String sessionAddress() {
    return sessionHostAddress + URL.COLON + sessionPort;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConnectId)) return false;
    ConnectId connectId = (ConnectId) o;
    return clientPort == connectId.clientPort
        && sessionPort == connectId.sessionPort
        && Objects.equals(clientHostAddress, connectId.clientHostAddress)
        && Objects.equals(sessionHostAddress, connectId.sessionHostAddress);
  }

  @Override
  public int hashCode() {
    if (hash == 0) {
      hash = Objects.hash(clientHostAddress, clientPort, sessionHostAddress, sessionPort);
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(64);
    sb.append(clientHostAddress)
        .append(URL.COLON)
        .append(clientPort)
        .append(ValueConstants.CONNECT_ID_SPLIT)
        .append(sessionHostAddress)
        .append(URL.COLON)
        .append(sessionPort);
    return sb.toString();
  }
}
