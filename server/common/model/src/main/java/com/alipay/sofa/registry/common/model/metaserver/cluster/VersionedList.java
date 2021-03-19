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
package com.alipay.sofa.registry.common.model.metaserver.cluster;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author chen.zhu
 *     <p>Mar 09, 2021
 */
public class VersionedList<T> implements Cluster<T>, Serializable {

  public static final VersionedList EMPTY = new VersionedList(-1L, Collections.EMPTY_LIST);

  private final long epoch;

  private final List<T> members;

  public VersionedList(long epoch, List<T> members) {
    this.epoch = epoch;
    this.members = members;
  }

  @Override
  public long getEpoch() {
    return epoch;
  }

  @Override
  public List<T> getClusterMembers() {
    return members;
  }
}
