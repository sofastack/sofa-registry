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
package com.alipay.sofa.registry.common.model.slot;

import java.io.Serializable;

/**
 * @author chen.zhu
 *     <p>Feb 24, 2021
 */
public class BaseSlotStatus implements Serializable {

  protected final int slotId;

  protected final long slotLeaderEpoch;

  protected final Slot.Role role;

  protected final String server;

  /**
   * Constructor.
   *
   * @param slotId the slot id
   * @param slotLeaderEpoch the slot leader epoch
   * @param role the role
   * @param server server
   */
  public BaseSlotStatus(int slotId, long slotLeaderEpoch, Slot.Role role, String server) {
    this.slotId = slotId;
    this.slotLeaderEpoch = slotLeaderEpoch;
    this.role = role;
    this.server = server;
  }

  /**
   * Gets get slot id.
   *
   * @return the get slot id
   */
  public int getSlotId() {
    return slotId;
  }

  /**
   * Gets get slot leader epoch.
   *
   * @return the get slot leader epoch
   */
  public long getSlotLeaderEpoch() {
    return slotLeaderEpoch;
  }

  /**
   * Gets get role.
   *
   * @return the get role
   */
  public Slot.Role getRole() {
    return role;
  }

  /**
   * Gets get data server.
   *
   * @return the get data server
   */
  public String getServer() {
    return server;
  }

  public enum LeaderStatus {
    INIT,
    HEALTHY,
    UNHEALTHY;

    /**
     * Is healthy boolean.
     *
     * @return the boolean
     */
    public boolean isHealthy() {
      return this == HEALTHY;
    }
  }

  @Override
  public String toString() {
    return "BaseSlotStatus{"
        + "slotId="
        + slotId
        + ", slotLeaderEpoch="
        + slotLeaderEpoch
        + ", role="
        + role
        + ", server='"
        + server
        + '\''
        + '}';
  }
}
