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
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.util.CollectionUtils;

public final class TriggerPushContext {
  public final String dataNode;
  private Map<String, Long> expectDatumVersion;
  private TraceTimes firstTraceTimes;
  private TraceTimes lastTraceTimes;

  public TriggerPushContext(
      String dataCenter, long expectDatumVersion, String dataNode, long triggerSessionTimestamp) {
    this(
        Collections.singletonMap(dataCenter, expectDatumVersion),
        dataNode,
        triggerSessionTimestamp,
        new TraceTimes());
  }

  public TriggerPushContext(
      String dataCenter,
      long expectDatumVersion,
      String dataNode,
      long triggerSessionTimestamp,
      TraceTimes traceTimes) {
    this(
        Collections.singletonMap(dataCenter, expectDatumVersion),
        dataNode,
        triggerSessionTimestamp,
        traceTimes);
  }

  public TriggerPushContext(
      Map<String, Long> expectDatumVersion, String dataNode, long triggerSessionTimestamp) {
    this(expectDatumVersion, dataNode, triggerSessionTimestamp, new TraceTimes());
  }

  public TriggerPushContext(
      Map<String, Long> expectDatumVersion,
      String dataNode,
      long triggerSessionTimestamp,
      TraceTimes traceTimes) {
    this.dataNode = dataNode;
    this.expectDatumVersion = expectDatumVersion;
    traceTimes.setTriggerSession(triggerSessionTimestamp);
    this.firstTraceTimes = traceTimes;
    this.lastTraceTimes = traceTimes;
  }

  public synchronized Set<String> dataCenters() {
    if (CollectionUtils.isEmpty(expectDatumVersion)) {
      return Collections.emptySet();
    }
    return expectDatumVersion.keySet();
  }

  public TraceTimes getFirstTimes() {
    return this.firstTraceTimes;
  }

  public TraceTimes getLastTimes() {
    return this.lastTraceTimes;
  }

  public void addTraceTime(TraceTimes times) {
    if (times.beforeThan(this.firstTraceTimes)) {
      this.firstTraceTimes = times;
    }
    if (this.lastTraceTimes.beforeThan(times)) {
      this.lastTraceTimes = times;
    }
  }

  public synchronized Map<String, Long> getExpectDatumVersion() {
    return expectDatumVersion;
  }

  public synchronized void setExpectDatumVersion(Map<String, Long> expectDatumVersion) {
    this.expectDatumVersion = expectDatumVersion;
  }

  public String formatTraceTimes(long pushFinishTimestamp) {
    if (firstTraceTimes == lastTraceTimes) {
      return StringFormatter.format("lastDataTrace={}", lastTraceTimes.format(pushFinishTimestamp));
    } else {
      return StringFormatter.format(
          "firstDataTrace={},lastDataTrace={}",
          firstTraceTimes.format(pushFinishTimestamp),
          lastTraceTimes.format(pushFinishTimestamp));
    }
  }

  @Override
  public String toString() {
    return StringFormatter.format("TriggerPushCtx{{},ver={}}", dataNode, expectDatumVersion);
  }

  /**
   * return true when this all datacenter version are smaller than changeCtx
   *
   * @param existCtx existCtx
   * @return boolean
   */
  public synchronized boolean smallerThan(TriggerPushContext existCtx) {
    ParaCheckUtil.checkEquals(
        expectDatumVersion.keySet(), existCtx.dataCenters(), "pushChangeCtx.dataCenters");

    for (Entry<String, Long> entry : this.expectDatumVersion.entrySet()) {
      Long ctxVersion = existCtx.expectDatumVersion.get(entry.getKey());
      if (ctxVersion == null || entry.getValue() > ctxVersion) {
        return false;
      }
    }
    return true;
  }

  public synchronized int mergeVersion(TriggerPushContext existCtx) {
    ParaCheckUtil.checkEquals(
        expectDatumVersion.keySet(), existCtx.dataCenters(), "pushChangeCtx.dataCenters");
    int merge = 0;

    for (Entry<String, Long> existEntry : existCtx.expectDatumVersion.entrySet()) {
      Long updateVersion = expectDatumVersion.get(existEntry.getKey());
      if (updateVersion == null || updateVersion < existEntry.getValue()) {
        expectDatumVersion.put(existEntry.getKey(), existEntry.getValue());
        merge++;
      }
    }
    return merge;
  }
}
