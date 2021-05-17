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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.SubPublisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class PushTrace {
  private static final Logger LOGGER = LoggerFactory.getLogger("PUSH-TRACE");

  private final SubDatum datum;
  final long pushCommitTimestamp = System.currentTimeMillis();

  private long subscriberPushedVersion;
  private final String subApp;
  private final int subNum;
  private final long subRegTimestamp;
  private final InetSocketAddress subAddress;
  final PushCause pushCause;

  private long pushStartTimestamp;

  private PushStatus status;
  private long pushFinishTimestamp;

  // push.finish- first.newly.publisher.registryTs
  long datumModifyDelayMillis;
  // push.finish - datum.versionTs
  long datumTotalDelayMillis;
  // exec.start - datum.modify
  long datumPushTriggerSpanMillis;
  // commit - exec.start
  long datumPushCommitSpanMillis;
  // push.start - fetch.finish
  long datumPushStartSpanMillis;
  // push.finish - push.start
  long datumPushFinishSpanMillis;

  // push.start - exec.start
  long datumPushSessionSpanMillis;

  // pub after last push
  int newPublisherNum;
  // push.finish - firstPub.registerTimestamp
  long firstPubPushDelayMillis;
  // push.finish - lastPub.registerTimestamp
  long lastPubPushDelayMillis;

  private PushTrace(
      SubDatum datum,
      InetSocketAddress address,
      String subApp,
      PushCause pushCause,
      int subNum,
      long subRegTimestamp) {
    this.datum = datum;
    this.pushCause = pushCause;
    this.subAddress = address;
    this.subApp = subApp;
    this.subNum = subNum;
    this.subRegTimestamp = subRegTimestamp;
  }

  public static PushTrace trace(
      SubDatum datum,
      InetSocketAddress address,
      String subApp,
      PushCause pushCause,
      int subNum,
      long subRegTimestamp) {
    return new PushTrace(datum, address, subApp, pushCause, subNum, subRegTimestamp);
  }

  public PushTrace startPush(long subscriberPushedVersion, long startPushTimestamp) {
    this.subscriberPushedVersion = subscriberPushedVersion;
    this.pushStartTimestamp = startPushTimestamp;
    return this;
  }

  public PushTrace finishPush(PushStatus status, long finishPushTimestamp) {
    this.status = status;
    this.pushFinishTimestamp = finishPushTimestamp;
    return this;
  }

  public void print() {
    calc();
    LOGGER.info(
        "{},{},{},{},{},cause={},pubNum={},pubBytes={},pubNew={},delay={},{},{},{},{},session={},cliIO={},firstPubDelay={},lastPubDelay={},subNum={},addr={},expectVer={},dataNode={}",
        status,
        datum.getDataInfoId(),
        datum.getVersion(),
        subApp,
        datum.getDataCenter(),
        pushCause.pushType,
        datum.getPublishers().size(),
        datum.getDataBoxBytes(),
        newPublisherNum,
        datumModifyDelayMillis,
        datumTotalDelayMillis,
        datumPushTriggerSpanMillis,
        datumPushCommitSpanMillis,
        datumPushStartSpanMillis,
        datumPushSessionSpanMillis,
        datumPushFinishSpanMillis,
        firstPubPushDelayMillis,
        lastPubPushDelayMillis,
        subNum,
        subAddress,
        pushCause.triggerPushCtx.getExpectDatumVersion(),
        pushCause.triggerPushCtx.dataNode);
  }

  private void calc() {
    // try find the earliest and the latest publisher after the subPushedVersion
    // that means the modify after last push, but this could not handle the publisher.remove
    if (subscriberPushedVersion == 0) {
      this.datumTotalDelayMillis = pushFinishTimestamp - subRegTimestamp;
    } else {
      this.datumTotalDelayMillis = pushFinishTimestamp - pushCause.datumTimestamp;
    }
    this.datumPushTriggerSpanMillis =
        Math.max(
            pushCause.triggerPushCtx.getTriggerSessionTimestamp() - pushCause.datumTimestamp, 0);
    this.datumPushCommitSpanMillis =
        pushCommitTimestamp - pushCause.triggerPushCtx.getTriggerSessionTimestamp();
    this.datumPushStartSpanMillis = pushStartTimestamp - pushCommitTimestamp;
    this.datumPushFinishSpanMillis = pushFinishTimestamp - pushStartTimestamp;
    this.datumPushSessionSpanMillis =
        pushStartTimestamp - pushCause.triggerPushCtx.getTriggerSessionTimestamp();
    final List<SubPublisher> publishers = datum.getPublishers();
    final long lastPushTimestamp =
        subscriberPushedVersion <= ValueConstants.DEFAULT_NO_DATUM_VERSION
            ? subRegTimestamp
            : DatumVersionUtil.getRealTimestamp(subscriberPushedVersion);
    final List<SubPublisher> news = findNewPublishers(publishers, lastPushTimestamp);
    final SubPublisher first = news.isEmpty() ? null : news.get(0);
    final SubPublisher last = news.isEmpty() ? null : news.get(news.size() - 1);
    this.newPublisherNum = news.size();
    this.firstPubPushDelayMillis =
        first == null ? 0 : pushFinishTimestamp - first.getRegisterTimestamp();
    this.lastPubPushDelayMillis =
        last == null ? 0 : pushFinishTimestamp - last.getRegisterTimestamp();

    this.datumModifyDelayMillis = datumTotalDelayMillis;
    if (pushCause.pushType == PushType.Sub && first != null) {
      // if sub, use first.publisher.registerTs as modifyTs
      datumModifyDelayMillis = firstPubPushDelayMillis;
    }
  }

  enum PushStatus {
    OK,
    Fail,
    Timeout,
    Busy,
  }

  static List<SubPublisher> findNewPublishers(
      List<SubPublisher> publishers, long minRegisterTimestamp) {
    if (publishers.isEmpty()) {
      return Collections.emptyList();
    }
    List<SubPublisher> news = Lists.newArrayListWithCapacity(128);
    for (SubPublisher p : publishers) {
      if (p.getRegisterTimestamp() > minRegisterTimestamp) {
        news.add(p);
      }
    }
    news.sort(
        new Comparator<SubPublisher>() {
          @Override
          public int compare(SubPublisher o1, SubPublisher o2) {
            return Longs.compare(o1.getRegisterTimestamp(), o2.getRegisterTimestamp());
          }
        });
    return news;
  }

  static long getTriggerPushTimestamp(SubDatum datum) {
    long ts =
        datum.getVersion() <= ValueConstants.DEFAULT_NO_DATUM_VERSION
            ? System.currentTimeMillis()
            : DatumVersionUtil.getRealTimestamp(datum.getVersion());
    return ts;
  }
}
