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
package com.alipay.sofa.registry.jdbc.domain;

import java.util.Date;

/**
 * @author xiaojian.xj
 * @version $Id: FollowCompeteLockDomain.java, v 0.1 2021年03月16日 11:39 xiaojian.xj Exp $
 */
public class FollowCompeteLockDomain {

  /** local data center */
  private String dataCenter;

  /** lock name */
  private String lockName;

  /** lock owner */
  private String owner;

  /** last update timestamp */
  private Date gmtModified;

  /** try to compete new owner */
  private String newOwner;

  private long duration;

  private long term;

  private long termDuration;

  public FollowCompeteLockDomain() {}

  public FollowCompeteLockDomain(
      String dataCenter,
      String lockName,
      String owner,
      Date gmtModified,
      String newOwner,
      long leaseDuration,
      long term,
      long termDuration) {
    this.dataCenter = dataCenter;
    this.lockName = lockName;
    this.owner = owner;
    this.gmtModified = gmtModified;
    this.newOwner = newOwner;
    this.duration = leaseDuration;
    this.term = term;
    this.termDuration = termDuration;
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
   * Getter method for property <tt>newOwner</tt>.
   *
   * @return property value of newOwner
   */
  public String getNewOwner() {
    return newOwner;
  }

  /**
   * Setter method for property <tt>newOwner</tt>.
   *
   * @param newOwner value to be assigned to property newOwner
   */
  public void setNewOwner(String newOwner) {
    this.newOwner = newOwner;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
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
}
