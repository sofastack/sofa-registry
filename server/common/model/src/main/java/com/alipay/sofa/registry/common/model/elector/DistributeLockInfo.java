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
package com.alipay.sofa.registry.common.model.elector;

import java.util.Date;

/**
 * @author xiaojian.xj
 * @version : DistributeLockInfo.java, v 0.1 2022年08月06日 17:02 xiaojian.xj Exp $
 */
public class DistributeLockInfo {

  /** primary key */
  private long id;

  /** local data center */
  private String dataCenter;

  /** lock name */
  private String lockName;

  /** lock owner */
  private String owner;

  /** duration(millisecond) */
  private long duration;

  /** create */
  private Date gmtCreate;

  /** last update timestamp */
  private Date gmtModified;

  /** current timestamp in db */
  private Date gmtDbServerTime;

  private long gmtModifiedUnixNanos;

  private long term;

  private long termDuration;

  public DistributeLockInfo() {}

  public DistributeLockInfo(String dataCenter, String lockName, String owner, long duration) {
    this.dataCenter = dataCenter;
    this.lockName = lockName;
    this.owner = owner;
    this.duration = duration;
  }

  /**
   * Getter method for property <tt>id</tt>.
   *
   * @return property value of id
   */
  public long getId() {
    return id;
  }

  /**
   * Setter method for property <tt>id</tt>.
   *
   * @param id value to be assigned to property id
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  /**
   * Setter method for property <tt>dataCenter</tt>.
   *
   * @param dataCenter value to be assigned to property dataCenter
   */
  public void setDataCenter(String dataCenter) {
    this.dataCenter = dataCenter;
  }

  /**
   * Getter method for property <tt>lockName</tt>.
   *
   * @return property value of lockName
   */
  public String getLockName() {
    return lockName;
  }

  /**
   * Setter method for property <tt>lockName</tt>.
   *
   * @param lockName value to be assigned to property lockName
   */
  public void setLockName(String lockName) {
    this.lockName = lockName;
  }

  /**
   * Getter method for property <tt>owner</tt>.
   *
   * @return property value of owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Setter method for property <tt>owner</tt>.
   *
   * @param owner value to be assigned to property owner
   */
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * Getter method for property <tt>duration</tt>.
   *
   * @return property value of duration
   */
  public long getDuration() {
    return duration;
  }

  /**
   * Setter method for property <tt>duration</tt>.
   *
   * @param duration value to be assigned to property duration
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }

  /**
   * Getter method for property <tt>gmtCreate</tt>.
   *
   * @return property value of gmtCreate
   */
  public Date getGmtCreate() {
    return gmtCreate;
  }

  /**
   * Setter method for property <tt>gmtCreate</tt>.
   *
   * @param gmtCreate value to be assigned to property gmtCreate
   */
  public void setGmtCreate(Date gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  /**
   * Getter method for property <tt>gmtModified</tt>.
   *
   * @return property value of gmtModified
   */
  public Date getGmtModified() {
    return gmtModified;
  }

  /**
   * Setter method for property <tt>gmtModified</tt>.
   *
   * @param gmtModified value to be assigned to property gmtModified
   */
  public void setGmtModified(Date gmtModified) {
    this.gmtModified = gmtModified;
  }

  /**
   * Getter method for property <tt>gmtDbServerTime</tt>.
   *
   * @return property value of gmtDbServerTime
   */
  public Date getGmtDbServerTime() {
    return gmtDbServerTime;
  }

  /**
   * Setter method for property <tt>gmtDbServerTime</tt>.
   *
   * @param gmtDbServerTime value to be assigned to property gmtDbServerTime
   */
  public void setGmtDbServerTime(Date gmtDbServerTime) {
    this.gmtDbServerTime = gmtDbServerTime;
  }

  public boolean expire() {

    return gmtDbServerTime.getTime() > gmtModified.getTime() + duration;
  }

  @Override
  public String toString() {
    return "DistributeLockDomain{"
        + "id="
        + id
        + ", dataCenter='"
        + dataCenter
        + '\''
        + ", lockName='"
        + lockName
        + '\''
        + ", owner='"
        + owner
        + '\''
        + ", duration="
        + duration
        + ", gmtCreate="
        + gmtCreate
        + ", gmtModified="
        + gmtModified
        + ", gmtDbServerTime="
        + gmtDbServerTime
        + ", term="
        + term
        + ", index="
        + termDuration
        + '}';
  }

  public long getTerm() {
    return term;
  }

  public void setTerm(long term) {
    this.term = term;
  }

  public long getTermDuration() {
    return termDuration;
  }

  public void setTermDuration(long termDuration) {
    this.termDuration = termDuration;
  }

  public long getGmtModifiedUnixNanos() {
    return gmtModifiedUnixNanos;
  }

  public long getGmtModifiedUnixMillis() {
    return gmtModifiedUnixNanos / 1000000;
  }

  public void setGmtModifiedUnixNanos(long gmtModifiedUnixNanos) {
    this.gmtModifiedUnixNanos = gmtModifiedUnixNanos;
  }
}
