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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.*;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 10:12 yuzhi.lyz Exp $
 */
public final class Slot implements Serializable, Cloneable {
  public enum Role {
    Leader,
    Follower,
  }

  private final int id;
  private final String leader;
  private final long leaderEpoch;
  private final Set<String> followers;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Slot(
      @JsonProperty("id") int id,
      @JsonProperty("leader") String leader,
      @JsonProperty("leaderEpoch") long leaderEpoch,
      @JsonProperty("followers") Collection<String> followers) {
    this.id = id;
    this.leader = leader;
    this.leaderEpoch = leaderEpoch;
    if (!CollectionUtils.isEmpty(followers)) {
      this.followers = new HashSet<>(followers);
    } else {
      this.followers = new HashSet<>();
    }
  }

  @Override
  protected Slot clone() {
    return new Slot(this.id, this.leader, this.leaderEpoch, this.followers);
  }

  /**
   * Getter method for property <tt>id</tt>.
   *
   * @return property value of id
   */
  public int getId() {
    return id;
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
   * Getter method for property <tt>followers</tt>.
   *
   * @return property value of followers
   */
  public Set<String> getFollowers() {
    return followers;
  }

  /**
   * Getter method for property <tt>leaderEpoch</tt>.
   *
   * @return property value of leaderEpoch
   */
  public long getLeaderEpoch() {
    return leaderEpoch;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Slot)) return false;
    Slot slot = (Slot) o;
    return id == slot.id
        && leaderEpoch == slot.leaderEpoch
        && Objects.equals(leader, slot.leader)
        && Objects.equals(followers, slot.followers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, leader, leaderEpoch, followers);
  }

  @Override
  public String toString() {
    return "Slot{"
        + "id="
        + id
        + ", leader='"
        + leader
        + '\''
        + ", leaderEpoch="
        + leaderEpoch
        + ", followers="
        + followers
        + '}';
  }
}
