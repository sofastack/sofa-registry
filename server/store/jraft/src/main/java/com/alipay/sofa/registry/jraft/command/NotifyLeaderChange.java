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
package com.alipay.sofa.registry.jraft.command;

import com.alipay.sofa.jraft.entity.PeerId;
import java.io.Serializable;

/**
 * @author shangyu.wh
 * @version $Id: NotifyLeaderChange.java, v 0.1 2018-06-23 14:32 shangyu.wh Exp $
 */
public class NotifyLeaderChange implements Serializable {

  private final PeerId leader;

  private String sender;

  /**
   * constructor
   *
   * @param leader
   */
  public NotifyLeaderChange(PeerId leader) {
    this.leader = leader;
  }

  /**
   * Getter method for property <tt>leader</tt>.
   *
   * @return property value of leader
   */
  public PeerId getLeader() {
    return leader;
  }

  /**
   * Getter method for property <tt>sender</tt>.
   *
   * @return property value of sender
   */
  public String getSender() {
    return sender;
  }

  /**
   * Setter method for property <tt>sender</tt>.
   *
   * @param sender value to be assigned to property sender
   */
  public void setSender(String sender) {
    this.sender = sender;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NotifyLeaderChange{");
    sb.append("leader=").append(leader);
    sb.append(", sender='").append(sender).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
