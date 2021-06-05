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
package com.alipay.sofa.registry.common.model;

import java.io.Serializable;

public class TraceTimes implements Serializable {
  private volatile long overrideCount;

  private long datumNotifyCreate;

  private long triggerSession;

  public long getDatumNotifyCreate() {
    return datumNotifyCreate;
  }

  public void setDatumNotifyCreate(long datumNotifyCreate) {
    this.datumNotifyCreate = datumNotifyCreate;
  }

  public long getTriggerSession() {
    return triggerSession;
  }

  public void setTriggerSession(long triggerSession) {
    this.triggerSession = triggerSession;
  }

  public void extend(TraceTimes times) {
    this.datumNotifyCreate = times.datumNotifyCreate;
    this.triggerSession = times.triggerSession;
  }

  public void override(TraceTimes times) {
    extend(times);
    long last = Math.max(overrideCount, times.overrideCount);
    overrideCount = last + 1;
  }

  public long getOverrideCount() {
    return overrideCount;
  }

  @Override
  public String toString() {
    return "TraceTimes{"
        + "overrideCount="
        + overrideCount
        + ", datumNotifyCreate="
        + datumNotifyCreate
        + ", triggerSession="
        + triggerSession
        + '}';
  }
}
