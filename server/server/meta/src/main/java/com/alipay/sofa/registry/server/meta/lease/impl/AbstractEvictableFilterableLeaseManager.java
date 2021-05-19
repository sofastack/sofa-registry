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
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.server.meta.lease.LeaseFilter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
public abstract class AbstractEvictableFilterableLeaseManager<T extends Node>
    extends AbstractEvictableLeaseManager<T> {

  @Autowired private List<LeaseFilter> leaseFilters;

  @Override
  public VersionedList<Lease<T>> getLeaseMeta() {
    if (leaseFilters == null || leaseFilters.isEmpty()) {
      return super.getLeaseMeta();
    }
    VersionedList<Lease<T>> rawVersionedList = super.getLeaseMeta();
    List<Lease<T>> leaseList = rawVersionedList.getClusterMembers();
    for (LeaseFilter filter : leaseFilters) {
      leaseList = filterOut(leaseList, filter);
    }
    return new VersionedList<>(rawVersionedList.getEpoch(), leaseList);
  }

  protected List<Lease<T>> filterOut(List<Lease<T>> inputs, LeaseFilter filter) {
    List<Lease<T>> leases = Lists.newArrayListWithCapacity(inputs.size());
    for (Lease<T> lease : inputs) {
      if (filter.allowSelect(lease)) {
        leases.add(lease);
      }
    }
    return leases;
  }

  @VisibleForTesting
  protected AbstractEvictableFilterableLeaseManager<T> setLeaseFilters(
      List<LeaseFilter> leaseFilters) {
    this.leaseFilters = leaseFilters;
    return this;
  }
}
