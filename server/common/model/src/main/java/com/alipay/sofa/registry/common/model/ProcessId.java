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

import java.io.Serializable;
import java.util.Objects;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-30 10:44 yuzhi.lyz Exp $
 */
public final class ProcessId implements Serializable {
  private final String hostAddress;
  private final long timestamp;
  private final int pid;
  private final int rand;

  public ProcessId(String hostAddress, long timestamp, int pid, int rand) {
    this.hostAddress = hostAddress;
    this.timestamp = timestamp;
    this.pid = pid;
    this.rand = rand;
  }

  /**
   * Getter method for property <tt>address</tt>.
   *
   * @return property value of address
   */
  public String getHostAddress() {
    return hostAddress;
  }

  /**
   * Getter method for property <tt>timestamp</tt>.
   *
   * @return property value of timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Getter method for property <tt>pid</tt>.
   *
   * @return property value of pid
   */
  public int getPid() {
    return pid;
  }

  /**
   * Getter method for property <tt>rand</tt>.
   *
   * @return property value of rand
   */
  public int getRand() {
    return rand;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProcessId)) return false;
    ProcessId processId = (ProcessId) o;
    return timestamp == processId.timestamp
        && pid == processId.pid
        && rand == processId.rand
        && Objects.equals(hostAddress, processId.hostAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hostAddress, timestamp, pid, rand);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(64);
    return sb.append(hostAddress)
        .append('-')
        .append(pid)
        .append('-')
        .append(timestamp)
        .append('-')
        .append(rand)
        .toString();
  }
}
