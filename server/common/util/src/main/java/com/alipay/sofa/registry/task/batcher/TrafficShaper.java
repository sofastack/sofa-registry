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
package com.alipay.sofa.registry.task.batcher;

import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;

/**
 * {@link TrafficShaper} provides admission control policy prior to dispatching tasks to workers. It
 * reacts to events coming via reprocess requests (transient failures, congestion), and delays the
 * processing depending on this feedback.
 *
 * @author Tomasz Bak
 * @author shangyu.wh modify
 * @version $Id: TrafficShaper.java, v 0.1 2017-11-14 15:37 shangyu.wh Exp $
 */
public class TrafficShaper {

  private static final long MAX_DELAY = 30 * 1000;

  private final long congestionRetryDelayMs;
  private final long networkFailureRetryMs;

  private volatile long lastCongestionError;
  private volatile long lastNetworkFailure;

  /**
   * @param congestionRetryDelayMs
   * @param networkFailureRetryMs
   */
  TrafficShaper(long congestionRetryDelayMs, long networkFailureRetryMs) {
    this.congestionRetryDelayMs = Math.min(MAX_DELAY, congestionRetryDelayMs);
    this.networkFailureRetryMs = Math.min(MAX_DELAY, networkFailureRetryMs);
  }

  /** @param processingResult */
  void registerFailure(ProcessingResult processingResult) {
    if (processingResult == ProcessingResult.Congestion) {
      lastCongestionError = System.currentTimeMillis();
    } else if (processingResult == ProcessingResult.TransientError) {
      lastNetworkFailure = System.currentTimeMillis();
    }
  }

  /** @return */
  long transmissionDelay() {
    if (lastCongestionError == -1 && lastNetworkFailure == -1) {
      return 0;
    }

    long now = System.currentTimeMillis();
    if (lastCongestionError != -1) {
      long congestionDelay = now - lastCongestionError;
      if (congestionDelay >= 0 && congestionDelay < congestionRetryDelayMs) {
        return congestionRetryDelayMs - congestionDelay;
      }
      lastCongestionError = -1;
    }

    if (lastNetworkFailure != -1) {
      long failureDelay = now - lastNetworkFailure;
      if (failureDelay >= 0 && failureDelay < networkFailureRetryMs) {
        return networkFailureRetryMs - failureDelay;
      }
      lastNetworkFailure = -1;
    }
    return 0;
  }
}
