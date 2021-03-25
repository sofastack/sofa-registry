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

import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.*;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-03 11:27 yuzhi.lyz Exp $
 */
public final class DataNodeSlot implements Serializable {
  private static final long serialVersionUID = -4418378966762753298L;
  private final String dataNode;
  private final Set<Integer> leaders = Sets.newTreeSet();
  private final Set<Integer> followers = Sets.newTreeSet();

  /**
   * Constructor.
   *
   * @param dataNode the data node
   */
  public DataNodeSlot(String dataNode) {
    this.dataNode = dataNode;
  }

  /**
   * Fork data node slot.
   *
   * @param ignoreFollowers the ignore followers
   * @return the data node slot
   */
  public DataNodeSlot fork(boolean ignoreFollowers) {
    DataNodeSlot clone = new DataNodeSlot(dataNode);
    clone.leaders.addAll(leaders);
    if (!ignoreFollowers) {
      clone.followers.addAll(followers);
    }
    return clone;
  }

  public DataNodeSlot addLeader(int slotId) {
    leaders.add(slotId);
    return this;
  }

  public DataNodeSlot addLeader(Collection<Integer> slotIds) {
    leaders.addAll(slotIds);
    return this;
  }

  public DataNodeSlot removeLeader(int slotId) {
    leaders.remove(slotId);
    return this;
  }

  public DataNodeSlot addFollower(int slotId) {
    followers.add(slotId);
    return this;
  }

  public DataNodeSlot addFollower(Collection<Integer> slotIds) {
    followers.addAll(slotIds);
    return this;
  }

  public DataNodeSlot removeFollower(int slotId) {
    followers.remove(slotId);
    return this;
  }

  public boolean containsLeader(int slotId) {
    return leaders.contains(slotId);
  }

  public boolean containsFollower(int slotId) {
    return followers.contains(slotId);
  }

  /**
   * Getter method for property <tt>dataNode</tt>.
   *
   * @return property value of dataNode
   */
  public String getDataNode() {
    return dataNode;
  }

  /**
   * Getter method for property <tt>leaders</tt>.
   *
   * @return property value of leaders
   */
  public Set<Integer> getLeaders() {
    return Collections.unmodifiableSet(leaders);
  }

  /**
   * Getter method for property <tt>followers</tt>.
   *
   * @return property value of followers
   */
  public Set<Integer> getFollowers() {
    return Collections.unmodifiableSet(followers);
  }

  /**
   * Total slot num int.
   *
   * @return the int
   */
  public int totalSlotNum() {
    return leaders.size() + followers.size();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataNodeSlot that = (DataNodeSlot) o;
    return Objects.equals(dataNode, that.dataNode)
        && Objects.equals(leaders, that.leaders)
        && Objects.equals(followers, that.followers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataNode, leaders, followers);
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "DataNodeSlot{{},leaders={}/{},followers={}/{}",
        dataNode,
        leaders.size(),
        leaders,
        followers.size(),
        followers);
  }

  public static List<String> collectDataNodes(Collection<DataNodeSlot> dataNodeSlots) {
    Set<String> ret = Sets.newLinkedHashSet();
    dataNodeSlots.forEach(d -> ret.add(d.dataNode));
    return Lists.newArrayList(ret);
  }
}
