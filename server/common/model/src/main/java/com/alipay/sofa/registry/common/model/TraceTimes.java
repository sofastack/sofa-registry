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

import com.alipay.sofa.registry.concurrent.ThreadLocalStringBuilder;
import java.io.Serializable;

public class TraceTimes implements Serializable {
  private long createTs;
  private int dataChangeType;
  private long firstDataChange;
  private long datumNotifyCreate;
  private long datumNotifySend;

  private long triggerSession;

  public TraceTimes() {
    createTs = System.currentTimeMillis();
  }

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

  public int getDataChangeType() {
    return dataChangeType;
  }

  public void setDataChangeType(int dataChangeType) {
    this.dataChangeType = dataChangeType;
  }

  public long getFirstDataChange() {
    return firstDataChange;
  }

  public void setFirstDataChange(long firstDataChange) {
    this.firstDataChange = firstDataChange;
  }

  public long getDatumNotifySend() {
    return datumNotifySend;
  }

  public void setDatumNotifySend(long datumNotifySend) {
    this.datumNotifySend = datumNotifySend;
  }

  public TraceTimes copy() {
    TraceTimes times = new TraceTimes();
    times.createTs = createTs;
    times.dataChangeType = dataChangeType;
    times.firstDataChange = firstDataChange;
    times.datumNotifyCreate = datumNotifyCreate;
    times.datumNotifySend = datumNotifySend;
    times.triggerSession = triggerSession;
    return times;
  }

  public boolean beforeThan(TraceTimes times) {
    return this.createTs < times.createTs;
  }

  private long delay(long from, long to) {
    if (from == 0 || to == 0) {
      return 0;
    }
    return to - from;
  }

  public String format(long endTs) {
    StringBuilder sb = ThreadLocalStringBuilder.get();
    sb.append("{");
    sb.append("chgType=").append(dataChangeType).append(",");
    sb.append("dataDelay=").append(delay(firstDataChange, endTs)).append(",");
    sb.append(delay(datumNotifyCreate, endTs)).append(",");
    sb.append(delay(datumNotifySend, endTs)).append(",");
    sb.append(delay(triggerSession, endTs));
    sb.append("}");
    return sb.toString();
  }

  @Override
  public String toString() {
    return "TraceTimes{"
        + ", dataChangeType="
        + dataChangeType
        + ", firstDataChange="
        + firstDataChange
        + ", datumNotifyCreate="
        + datumNotifyCreate
        + ", datumNotifySend="
        + datumNotifySend
        + ", triggerSession="
        + triggerSession
        + '}';
  }
}
