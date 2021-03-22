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

import java.io.Serializable;

/**
 * @author xiaojian.xj
 * @version $Id: LeaderInfo.java, v 0.1 2021年03月22日 10:27 xiaojian.xj Exp $
 */
public class LeaderInfo implements Serializable {

  private static final long serialVersionUID = -1851792034078553148L;
  private long epoch;

  private String leader;

  public LeaderInfo() {}

  public LeaderInfo(long epoch, String leader) {
    this.leader = leader;
    this.epoch = epoch;
  }

  /**
   * Getter method for property <tt>epoch</tt>.
   *
   * @return property value of epoch
   */
  public long getEpoch() {
    return epoch;
  }

  /**
   * Getter method for property <tt>leader</tt>.
   *
   * @return property value of leader
   */
  public String getLeader() {
    return leader;
  }

  /**
   * Setter method for property <tt>epoch</tt>.
   *
   * @param epoch value to be assigned to property epoch
   */
  public void setEpoch(long epoch) {
    this.epoch = epoch;
  }

  /**
   * Setter method for property <tt>leader</tt>.
   *
   * @param leader value to be assigned to property leader
   */
  public void setLeader(String leader) {
    this.leader = leader;
  }

  @Override
  public String toString() {
    return "LeaderInfo{" + "epoch=" + epoch + ", leader='" + leader + '\'' + '}';
  }
}
