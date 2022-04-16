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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.informer.DbEntryContainer;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class AppRevisionContainer implements DbEntryContainer<AppRevisionDomain> {
  private final Map<String, String> data = Maps.newConcurrentMap();

  @Override
  public synchronized void onEntry(AppRevisionDomain entry) {
    if (entry.isDeleted()) {
      data.remove(entry.getRevision());
    } else {
      data.put(entry.getRevision(), entry.getAppName());
    }
  }

  public boolean containsRevisionId(String revisionId) {
    return data.containsKey(revisionId);
  }

  public int size() {
    return data.size();
  }

  public Set<String> allRevisions() {
    return data.keySet();
  }

  public void foreach(BiConsumer<String, String> f) {
    data.forEach(f);
  }
}
