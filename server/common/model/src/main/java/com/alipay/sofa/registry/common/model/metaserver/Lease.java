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
package com.alipay.sofa.registry.common.model.metaserver;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * heartbeat info for node
 *
 * @author shangyu.wh
 * @version $Id: Renewer.java, v 0.1 2018-01-16 17:10 shangyu.wh Exp $
 */
public class Lease<T> implements Serializable {

  public static final String LEASE_DURATION = "registry.lease.duration.secs";
  public static final int DEFAULT_DURATION_SECS = Integer.getInteger(LEASE_DURATION, 20);

  private T renewal;

  private long beginTimestamp;

  private volatile long lastUpdateTimestamp;

  private long duration;

  /**
   * constructor
   *
   * @param renewal renewal
   * @param durationSECS durationSECS
   */
  public Lease(T renewal, long durationSECS) {
    this.renewal = renewal;
    this.beginTimestamp = System.currentTimeMillis();
    this.lastUpdateTimestamp = beginTimestamp;
    this.duration = durationSECS * 1000;
  }

  /**
   * Constructor.
   *
   * @param renewal the renewal
   * @param duration the duration
   * @param unit the unit
   */
  public Lease(T renewal, long duration, TimeUnit unit) {
    this(renewal, unit.toSeconds(duration));
  }

  /**
   * verify expired or not
   *
   * @return boolean
   */
  public boolean isExpired() {
    return System.currentTimeMillis() > lastUpdateTimestamp + duration;
  }

  /** refresh lastUpdateTimestamp */
  public void renew() {
    lastUpdateTimestamp = System.currentTimeMillis();
  }

  /**
   * refresh lastUpdateTimestamp by durationSECS
   *
   * @param durationSECS durationSECS
   */
  public void renew(long durationSECS) {
    lastUpdateTimestamp = System.currentTimeMillis();
    duration = durationSECS * 1000;
  }

  /**
   * Getter method for property <tt>renewal</tt>.
   *
   * @return property value of renewal
   */
  public T getRenewal() {
    return renewal;
  }

  /**
   * Setter method for property <tt>renewal</tt>.
   *
   * @param renewal value to be assigned to property renewal
   */
  public void setRenewal(T renewal) {
    this.renewal = renewal;
  }

  /**
   * Getter method for property <tt>beginTimestamp</tt>.
   *
   * @return property value of beginTimestamp
   */
  public long getBeginTimestamp() {
    return beginTimestamp;
  }

  /**
   * Getter method for property <tt>lastUpdateTimestamp</tt>.
   *
   * @return property value of lastUpdateTimestamp
   */
  public long getLastUpdateTimestamp() {
    return lastUpdateTimestamp;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "Lease{"
        + "renewal="
        + renewal
        + ", beginTimestamp="
        + beginTimestamp
        + ", lastUpdateTimestamp="
        + lastUpdateTimestamp
        + ", duration="
        + duration
        + '}';
  }

  /**
   * Equals boolean.
   *
   * @param o the o
   * @return the boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Lease<?> lease = (Lease<?>) o;
    return beginTimestamp == lease.beginTimestamp && renewal.equals(lease.renewal);
  }

  /**
   * Hash code int.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(renewal, beginTimestamp);
  }
}
