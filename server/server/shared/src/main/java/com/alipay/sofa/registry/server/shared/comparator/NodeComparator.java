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
package com.alipay.sofa.registry.server.shared.comparator;

import com.alipay.sofa.registry.common.model.Triple;
import java.util.Collection;
import java.util.Set;

/**
 * @author chen.zhu
 *     <p>Jan 12, 2021
 *     <p>String stands for data node ip
 *     <p>as the situation is mainly about rebalance the slot-table which, data node is stored just
 *     as IP address (String)
 */
public class NodeComparator extends AbstractComparator<String> {

  private Collection<String> prev;

  private Collection<String> current;

  public NodeComparator(Collection<String> prev, Collection<String> current) {
    this.prev = prev;
    this.current = current;
    compare();
  }

  public void compare() {
    Triple<Set<String>, Set<String>, Set<String>> triple = getDiff(prev, current);
    this.added = triple.getFirst();
    this.remainings = triple.getMiddle();
    this.removed = triple.getLast();
    this.count = this.added.size() + this.removed.size();
  }
}
