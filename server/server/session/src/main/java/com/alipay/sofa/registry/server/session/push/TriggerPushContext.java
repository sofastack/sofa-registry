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
import com.alipay.sofa.registry.util.StringFormatter;

public final class TriggerPushContext {
  public final String dataCenter;
  public final String dataNode;
  private long expectDatumVersion;
  private final TraceTimes times;

  public TriggerPushContext(
      String dataCenter, long expectDatumVersion, String dataNode, long triggerSessionTimestamp) {
    this.dataCenter = dataCenter;
    this.dataNode = dataNode;
    this.expectDatumVersion = expectDatumVersion;
    this.times = new TraceTimes();
    this.times.setTriggerSession(triggerSessionTimestamp);
  }

  public TraceTimes getTimes() {
    return this.times;
  }

  public long getExpectDatumVersion() {
    return expectDatumVersion;
  }

  public void setExpectDatumVersion(long expectDatumVersion) {
    this.expectDatumVersion = expectDatumVersion;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "TriggerPushCtx{{},ver={},dc={},times={}}",
        dataNode,
        expectDatumVersion,
        dataCenter,
        times);
  }
}
