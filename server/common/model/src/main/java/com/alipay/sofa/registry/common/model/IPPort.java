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

import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-21 11:14 yuzhi.lyz Exp $
 */
public final class IPPort implements Serializable {
  private final String ip;
  private final int port;

  private IPPort(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  /**
   * Getter method for property <tt>ip</tt>.
   *
   * @return property value of ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * Getter method for property <tt>port</tt>.
   *
   * @return property value of port
   */
  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IPPort)) return false;
    IPPort ipPort = (IPPort) o;
    return port == ipPort.port && Objects.equals(ip, ipPort.ip);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, port);
  }

  @Override
  public String toString() {
    return ip + ":" + port;
  }

  public static IPPort of(String ip, int port) {
    ParaCheckUtil.checkNotBlank(ip, "ip");
    ParaCheckUtil.checkIsPositive(port, "port");
    return new IPPort(ip, port);
  }
}
