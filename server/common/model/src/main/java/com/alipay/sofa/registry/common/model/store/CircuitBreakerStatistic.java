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

public final class CircuitBreakerStatistic {

  private final String group;

  private final String address;

  private final String ip;

  private volatile long failCount = 0;

  private volatile long lastFailTimeStamp;

  private volatile long consecutiveSuccess = 0;

  public CircuitBreakerStatistic(String group, String ip, String address) {
    this.group = group;
    this.ip = ip;
    this.address = address;
  }

  public CircuitBreakerStatistic(
      String group, String ip, String address, long failCount, long lastFailTimeStamp) {
    this.group = group;
    this.ip = ip;
    this.address = address;
    this.failCount = failCount;
    this.lastFailTimeStamp = lastFailTimeStamp;
  }

  public synchronized void success(int threshold) {
    consecutiveSuccess++;
    if (consecutiveSuccess >= threshold) {
      failCount = 0;
      lastFailTimeStamp = 0;
    }
  }

  public synchronized void fail() {
    failCount++;
    lastFailTimeStamp = System.currentTimeMillis();
    consecutiveSuccess = 0;
  }

  public boolean circuitBreak(int failThreshold, long silenceMillis) {

    return failCount > failThreshold
        && System.currentTimeMillis() < lastFailTimeStamp + silenceMillis;
  }

  /**
   * Getter method for property <tt>group</tt>.
   *
   * @return property value of group
   */
  public String getGroup() {
    return group;
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
   * Getter method for property <tt>address</tt>.
   *
   * @return property value of address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Getter method for property <tt>failCount</tt>.
   *
   * @return property value of failCount
   */
  public long getFailCount() {
    return failCount;
  }

  /**
   * Getter method for property <tt>consecutiveSuccess</tt>.
   *
   * @return property value of consecutiveSuccess
   */
  public long getConsecutiveSuccess() {
    return consecutiveSuccess;
  }

  @Override
  public String toString() {
    return "CircuitBreakerStatistic{"
        + "group='"
        + group
        + '\''
        + ", address='"
        + address
        + '\''
        + ", failCount="
        + failCount
        + ", lastFailTimeStamp="
        + lastFailTimeStamp
        + ", consecutiveSuccess="
        + consecutiveSuccess
        + '}';
  }
}
