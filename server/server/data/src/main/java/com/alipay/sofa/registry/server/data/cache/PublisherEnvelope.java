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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 19:47 yuzhi.lyz Exp $
 */
public final class PublisherEnvelope {
  final Publisher publisher;
  final ProcessId sessionProcessId;
  final RegisterVersion registerVersion;
  final long tombstoneTimestamp;

  private PublisherEnvelope(
      Publisher publisher,
      ProcessId sessionProcessId,
      RegisterVersion registerVersion,
      long tombstoneTimestamp) {
    this.publisher = publisher;
    this.sessionProcessId = sessionProcessId;
    this.registerVersion = registerVersion;
    this.tombstoneTimestamp = tombstoneTimestamp;
  }

  static PublisherEnvelope of(Publisher publisher) {
    ParaCheckUtil.checkNotNull(publisher.getSessionProcessId(), "publisher.sessionProcessId");
    switch (publisher.getDataType()) {
      case PUBLISHER:
        return pubOf(publisher, publisher.getSessionProcessId());
      case UN_PUBLISHER:
        return unpubOf(publisher.registerVersion(), publisher.getSessionProcessId());
      default:
        throw new IllegalArgumentException("not accept Publisher Type:" + publisher.getDataType());
    }
  }

  static PublisherEnvelope pubOf(Publisher publisher, ProcessId sessionProcessId) {
    ParaCheckUtil.checkEquals(
        publisher.getDataType(), StoreData.DataType.PUBLISHER, "Publisher.dataType");
    return new PublisherEnvelope(
        publisher,
        sessionProcessId,
        publisher.registerVersion(),
        // Long.max means pub never compact
        Long.MAX_VALUE);
  }

  static PublisherEnvelope unpubOf(RegisterVersion version, ProcessId sessionProcessId) {
    return new PublisherEnvelope(null, sessionProcessId, version, System.currentTimeMillis());
  }

  boolean isPub() {
    return publisher != null;
  }

  public RegisterVersion getVersionIfPub() {
    return isPub() ? registerVersion : null;
  }

  /**
   * Getter method for property <tt>sessionProcessId</tt>.
   *
   * @return property value of sessionProcessId
   */
  public ProcessId getSessionProcessId() {
    return sessionProcessId;
  }

  /**
   * Getter method for property <tt>publisher</tt>.
   *
   * @return property value of publisher
   */
  public Publisher getPublisher() {
    return publisher;
  }

  boolean isConnectId(ConnectId connectId) {
    return isPub() && publisher.connectId().equals(connectId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(128);
    sb.append("pub=")
        .append(isPub())
        .append(", connectId=")
        .append(publisher != null ? publisher.connectId() : "null")
        .append(", ver=")
        .append(registerVersion)
        .append(", ts=")
        .append(tombstoneTimestamp);
    return sb.toString();
  }
}
