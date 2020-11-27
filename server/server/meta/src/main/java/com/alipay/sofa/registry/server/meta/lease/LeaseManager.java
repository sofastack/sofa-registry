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
package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 19, 2020
 */
public interface LeaseManager<T extends Node> {

    /**
     * Cancel Lease for unpub/unregister perspective.
     *
     * @param renewal the renewal
     * @return the boolean
     */
    boolean cancel(T renewal);

    /**
     * Renew Lease.
     * Return true if the renewal has been existed and renew works
     * Return false if the renewal is a new entry, we have to register it insteadof renew it
     *
     * @param renewal       the renewal
     * @param leaseDuration the lease duration
     * @return the boolean
     */
    boolean renew(T renewal, int leaseDuration);

    /**
     * Evict expired leases.
     * Return true if version should change (some nodes are expired, and removed)
     * Otherwise, return false.
     */
    boolean evict();

}
