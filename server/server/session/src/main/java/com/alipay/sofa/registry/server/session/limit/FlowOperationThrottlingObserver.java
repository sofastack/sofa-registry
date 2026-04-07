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
package com.alipay.sofa.registry.server.session.limit;

import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/**
 * Follower node's throttling status observer.
 *
 * <p>Maintains two types of throttling status:
 *
 * <ul>
 *   <li><b>Local throttling</b>: computed locally based on this node's own load (e.g., high CPU).
 *       When enabled, it always enforces 100% throttling.
 *   <li><b>Cluster throttling</b>: synchronized from Meta Leader via heartbeat, representing the
 *       global adaptive throttling decision based on overall cluster health.
 * </ul>
 *
 * <p><b>Throttling policy:</b>
 *
 * <ol>
 *   <li>If local throttling is enabled → always throttle (return {@code true}).
 *   <li>Otherwise, apply probabilistic throttling based on cluster throttling percentage.
 * </ol>
 */
@Component
public class FlowOperationThrottlingObserver {

  // Local throttling status (updated by local monitoring thread)
  private volatile FlowOperationThrottlingStatus localThrottlingStatus =
      FlowOperationThrottlingStatus.disabled();

  // Cluster-wide throttling status (updated by heartbeat handler from Meta Leader)
  private volatile FlowOperationThrottlingStatus clusterThrottlingStatus =
      FlowOperationThrottlingStatus.disabled();

  /**
   * Updates the local throttling status.
   *
   * @param status new local status; if null, ignored
   */
  public void updateLocalThrottlingStatus(FlowOperationThrottlingStatus status) {
    if (status != null) {
      this.localThrottlingStatus = status;
    }
  }

  /**
   * Updates the cluster-wide throttling status received from Meta Leader.
   *
   * @param status new cluster status; if null, ignored
   */
  public void updateClusterThrottlingStatus(FlowOperationThrottlingStatus status) {
    if (status != null) {
      this.clusterThrottlingStatus = status;
    }
  }

  /**
   * Determines whether the current request should be throttled.
   *
   * <p>Policy:
   *
   * <ul>
   *   <li>If local throttling is enabled → immediately return {@code true} (100% throttling).
   *   <li>Otherwise, use the cluster throttling percentage to perform probabilistic throttling.
   * </ul>
   *
   * <p>Probabilistic throttling example:
   *
   * <ul>
   *   <li>Cluster throttle percent = 30.0 → ~30% of requests will be throttled
   *   <li>Cluster throttle percent = 0.0 → no throttling
   *   <li>Cluster throttle percent = 100.0 → all requests throttled
   * </ul>
   *
   * @return {@code true} if the request should be throttled, {@code false} otherwise
   */
  public boolean shouldThrottle() {
    // Local overload has the highest priority: always throttle
    if (localThrottlingStatus.isEnabled()) {
      return true;
    }

    // Fall back to cluster throttling decision
    FlowOperationThrottlingStatus clusterStatus = this.clusterThrottlingStatus;
    if (!clusterStatus.isEnabled()) {
      return false;
    }

    double throttlePercent = clusterStatus.getThrottlePercent();
    if (throttlePercent >= 100.0) {
      return true;
    }
    if (throttlePercent <= 0.0) {
      return false;
    }

    // Probabilistic throttling: [0.0, 100.0) random vs threshold
    return ThreadLocalRandom.current().nextDouble(100.0) < throttlePercent;
  }

  // --- Getters for debugging or metrics ---

  public FlowOperationThrottlingStatus getLocalThrottlingStatus() {
    return localThrottlingStatus;
  }

  public FlowOperationThrottlingStatus getClusterThrottlingStatus() {
    return clusterThrottlingStatus;
  }
}
