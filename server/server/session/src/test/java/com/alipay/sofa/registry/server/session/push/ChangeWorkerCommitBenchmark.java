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
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.common.model.TraceTimes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

/**
 * @author huicha
 * @date 2025/10/28
 */
@State(value = Scope.Benchmark)
@BenchmarkMode(value = {Mode.AverageTime})
@OutputTimeUnit(value = TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 10)
@Fork(1)
public class ChangeWorkerCommitBenchmark {

  private static final int DEFAULT_CHANGE_DEBOUNCING_MILLIS = 100;

  private static final int DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS = 1000;

  private static final int DEFAULT_CHANGE_TASK_WAITING_MILLIS = 100;

  private static final long DEFAULT_BASE_DELAY = 1000;

  private static final long DEFAULT_DELAY_PER_UNIT = 10;

  private static final long DEFAULT_PUBLISHER_THRESHOLD = 1000;

  private static final long DEFAULT_MAX_PUBLISHER_COUNT = 4000;

  private static final String DATA_INFO_ID_PREFIX = "DataInfoId-";

  private static final String DATA_CENTER = "MockDataCenter";

  private static final String DATA_NODE = "DataNode";

  private static final Set<String> DATA_CENTERS = Collections.singleton(DATA_CENTER);

  @Param({"1000", "10000", "100000", "200000", "250000", "500000"})
  private int totalDataInfoIdSize;

  @Param({"0.01", "0.05", "0.1"})
  private double largeDataInfoSize;

  @Param({"false", "true"})
  private boolean useLargeAdapterDelayChangeWorker;

  private Random random;

  private MockChangeHandler mockChangeHandler;

  private int largeDataInfoIdThreshold;

  private Map<String, Long> versions;

  private ChangeWorker<ChangeKey, ChangeTaskImpl> changeWorker;

  public ChangeWorkerCommitBenchmark() {
    this.random = new Random(System.currentTimeMillis());
    this.mockChangeHandler = new MockChangeHandler();
  }

  @Setup
  public void setup() {
    this.largeDataInfoIdThreshold = (int) (this.totalDataInfoIdSize * this.largeDataInfoSize);
    this.versions = new HashMap<>(this.totalDataInfoIdSize);
    if (this.useLargeAdapterDelayChangeWorker) {
      this.changeWorker =
          new LargeChangeAdaptiveDelayWorker(
              DEFAULT_CHANGE_DEBOUNCING_MILLIS,
              DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS,
              DEFAULT_CHANGE_TASK_WAITING_MILLIS,
              DEFAULT_BASE_DELAY,
              DEFAULT_DELAY_PER_UNIT,
              DEFAULT_PUBLISHER_THRESHOLD,
              DEFAULT_MAX_PUBLISHER_COUNT);
    } else {
      this.changeWorker =
          new DefaultChangeWorker(
              DEFAULT_CHANGE_DEBOUNCING_MILLIS,
              DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS,
              DEFAULT_CHANGE_TASK_WAITING_MILLIS);
    }
  }

  @Benchmark
  public void benchmarkCommitChange() {
    int dataInfoNumber = this.random.nextInt(this.totalDataInfoIdSize);
    String dataInfoId = DATA_INFO_ID_PREFIX + dataInfoNumber;
    long version = this.getVersion(dataInfoId);
    this.versions.put(dataInfoId, version);
    ChangeKey changeKey = new ChangeKey(DATA_CENTERS, dataInfoId);
    TraceTimes traceTimes = new TraceTimes();
    TriggerPushContext changeCtx;
    if (dataInfoNumber >= this.largeDataInfoIdThreshold) {
      changeCtx = new TriggerPushContext(DATA_CENTER, version, DATA_NODE, 0, traceTimes, 9000);
    } else {
      changeCtx = new TriggerPushContext(DATA_CENTER, version, DATA_NODE, 0, traceTimes, 100);
    }
    this.changeWorker.commitChange(changeKey, this.mockChangeHandler, changeCtx);
  }

  private long getVersion(String dataInfoId) {
    Long version = this.versions.get(dataInfoId);
    if (null == version) {
      version = 1L;
    } else {
      version = version + 1;
    }
    this.versions.put(dataInfoId, version);
    return version;
  }
}
