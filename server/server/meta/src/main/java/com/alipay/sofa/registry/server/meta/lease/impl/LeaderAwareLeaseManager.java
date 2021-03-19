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
import com.alipay.sofa.registry.exception.SofaRegistryMetaLeaderException;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 09, 2021
 */
public class LeaderAwareLeaseManager<T extends Node> extends SimpleLeaseManager<T> {

  @Autowired protected MetaLeaderService metaLeaderService;

  /**
   * Register.
   *
   * @param lease the lease
   */
  @Override
  public void register(Lease<T> lease) {
    if (!amILeader()) {
      throw new SofaRegistryMetaLeaderException(
          metaLeaderService.getLeader(), metaLeaderService.getLeaderEpoch(), "leader mismatch");
    }
    super.register(lease);
  }

  /**
   * Cancel boolean.
   *
   * @param lease the lease
   * @return the boolean
   */
  @Override
  public boolean cancel(Lease<T> lease) {
    if (!amILeader()) {
      throw new SofaRegistryMetaLeaderException(
          metaLeaderService.getLeader(), metaLeaderService.getLeaderEpoch(), "leader mismatch");
    }
    return super.cancel(lease);
  }

  /**
   * Renew boolean.
   *
   * @param renewal the renewal
   * @param leaseDuration the lease duration
   * @return the boolean
   */
  @Override
  public boolean renew(T renewal, int leaseDuration) {
    if (!amILeader()) {
      throw new SofaRegistryMetaLeaderException(
          metaLeaderService.getLeader(), metaLeaderService.getLeaderEpoch(), "leader mismatch");
    }
    return super.renew(renewal, leaseDuration);
  }

  protected boolean amILeader() {
    return metaLeaderService.amILeader();
  }
}
