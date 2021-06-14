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
import com.alipay.sofa.registry.trace.TraceID;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.core.async.Hack;

public final class PushTrace {
  // config the push trace log to use a separate disruptor
  private static final Logger LOGGER =
      Hack.hackLoggerDisruptor(LoggerFactory.getLogger("PUSH-TRACE"));
  private static final Logger SLOW_LOGGER =
      Hack.hackLoggerDisruptor(LoggerFactory.getLogger("PUSH-TRACE-SLOW"));
  private static final int MAX_NTP_TIME_PRECISION_MILLIS = 200;
  private final SubDatum datum;
  final long pushCreateTimestamp = System.currentTimeMillis();

  private final String subApp;

  private final long subRegTimestamp;
  private final InetSocketAddress subAddress;
  final PushCause pushCause;

  private volatile long pushStartTimestamp;
  private final int subNum;

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

  private List<Long> datumModifyTsAfter(long pushedTs) {
    List<Long> ret = Lists.newArrayListWithCapacity(12);
    List<Long> recentVersions = datum.getRecentVersions();
    if (!CollectionUtils.isEmpty(recentVersions)) {
      for (long v : recentVersions) {
        long ts = DatumVersionUtil.getRealTimestamp(v);
        if (ts > pushedTs) {
          ret.add(ts);
        }
      }
    }
    ret.add(DatumVersionUtil.getRealTimestamp(datum.getVersion()));
    return ret;
  }

  private List<Long> datumPushedDelayList(long finishedTs, long lastPushTs) {
    List<Long> timestamps = datumModifyTsAfter(lastPushTs);
    List<Long> ret = Lists.newArrayListWithCapacity(timestamps.size());
    for (long ts : timestamps) {
      ret.add(finishedTs - ts);
    }
    return ret;
  }

  private String formatDatumPushedDelayList(long finishedTs, long lastPushTs) {
    return datumPushedDelayList(finishedTs, lastPushTs).stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));
  }

  public void startPush() {
    this.pushStartTimestamp = System.currentTimeMillis();
  }

  public void finishPush(PushStatus status, TraceID taskID, long subscriberPushedVersion) {
    final long pushFinishTimestamp = System.currentTimeMillis();
    // push.finish- first.newly.publisher.registryTs
    long datumModifyPushSpanMillis;
    // push.finish - datum.versionTs
    long datumVersionPushSpanMillis;
    // session.triggerTs - datum.versionTs
    long datumVersionTriggerSpanMillis;

    // task.create - session.triggerTs
    long pushTaskPrepareSpanMillis;
    // task.start - task.create
    long pushTaskQueueSpanMillis;
    // task.finish - task.start
    long pushTaskClientIOSpanMillis;
    // task.start - session.triggerTs
    long pushTaskSessionSpanMillis;

    // pub after last push
    int newPublisherNum;
    // push.finish - firstPub.registerTimestamp
    long firstPubPushDelayMillis;
    // push.finish - lastPub.registerTimestamp
    long lastPubPushDelayMillis;

    // try find the earliest and the latest publisher after the subPushedVersion
    // that means the modify after last push, but this could not handle the publisher.remove
    if (pushCause.pushType == PushType.Reg) {
      datumVersionPushSpanMillis = pushFinishTimestamp - subRegTimestamp;
    } else {
      datumVersionPushSpanMillis = Math.max(pushFinishTimestamp - pushCause.datumTimestamp, 0);
      if (pushCause.pushType == PushType.Sub) {
        if (subRegTimestamp >= pushCause.datumTimestamp) {
          // case: datum.change trigger the sub.sub, but the sub.reg not finish
          datumVersionPushSpanMillis = pushFinishTimestamp - subRegTimestamp;
        }
      }
    }
    datumVersionTriggerSpanMillis =
        Math.max(
            pushCause.triggerPushCtx.getTimes().getTriggerSession() - pushCause.datumTimestamp, 0);

    // calc the task span millis
    pushTaskPrepareSpanMillis =
        pushCreateTimestamp - pushCause.triggerPushCtx.getTimes().getTriggerSession();
    pushTaskQueueSpanMillis = pushStartTimestamp - pushCreateTimestamp;
    pushTaskClientIOSpanMillis = pushFinishTimestamp - pushStartTimestamp;
    pushTaskSessionSpanMillis =
        pushStartTimestamp - pushCause.triggerPushCtx.getTimes().getTriggerSession();

    final List<SubPublisher> publishers = datum.getPublishers();
    final long lastPushTimestamp =
        subscriberPushedVersion <= ValueConstants.DEFAULT_NO_DATUM_VERSION
            ? subRegTimestamp
            : DatumVersionUtil.getRealTimestamp(subscriberPushedVersion);
    final List<SubPublisher> news =
        findNewPublishers(publishers, lastPushTimestamp + MAX_NTP_TIME_PRECISION_MILLIS);
    final SubPublisher first = news.isEmpty() ? null : news.get(0);
    final SubPublisher last = news.isEmpty() ? null : news.get(news.size() - 1);
    newPublisherNum = news.size();
    firstPubPushDelayMillis =
        first == null ? 0 : Math.max(pushFinishTimestamp - first.getRegisterTimestamp(), 0);
    lastPubPushDelayMillis =
        last == null ? 0 : Math.max(pushFinishTimestamp - last.getRegisterTimestamp(), 0);

    datumModifyPushSpanMillis = datumVersionPushSpanMillis;
    if (pushCause.pushType == PushType.Sub && first != null) {
      // if sub, use first.publisher.registerTs as modifyTs
      datumModifyPushSpanMillis = firstPubPushDelayMillis;
    }
    PushMetrics.Push.observePushDelayHistogram(
        pushCause.pushType, Math.max(datumModifyPushSpanMillis, datumVersionPushSpanMillis));
    PushMetrics.Push.countPushClient(status);
    final Logger log = datumModifyPushSpanMillis > 5000 ? SLOW_LOGGER : LOGGER;
    log.info(
        "{},{},{},{},{},cause={},pubNum={},pubBytes={},pubNew={},delay={},{},{},{},{},"
            + "session={},cliIO={},firstPubDelay={},lastPubDelay={},"
            + "subNum={},addr={},expectVer={},dataNode={},taskID={},pushedVer={},regTs={},"
            + "notifyCreateTs={},commitOverride={},datumRecentDelay={}",
        status,
        datum.getDataInfoId(),
        datum.getVersion(),
        subApp,
        datum.getDataCenter(),
        pushCause.pushType,
        datum.getPublishers().size(),
        datum.getDataBoxBytes(),
        newPublisherNum,
        datumModifyPushSpanMillis,
        datumVersionPushSpanMillis,
        datumVersionTriggerSpanMillis,
        pushTaskPrepareSpanMillis,
        pushTaskQueueSpanMillis,
        pushTaskSessionSpanMillis,
        pushTaskClientIOSpanMillis,
        firstPubPushDelayMillis,
        lastPubPushDelayMillis,
        subNum,
        subAddress,
        pushCause.triggerPushCtx.getExpectDatumVersion(),
        pushCause.triggerPushCtx.dataNode,
        taskID,
        subscriberPushedVersion,
        subRegTimestamp,
        pushCause.triggerPushCtx.getTimes().getDatumNotifyCreate(),
        pushCause.triggerPushCtx.getTimes().getOverrideCount(),
        formatDatumPushedDelayList(pushFinishTimestamp, lastPushTimestamp));
  }

  enum PushStatus {
    OK,
    Fail,
    Timeout,
    Busy,
    ChanClosed,
    ChanOverflow,
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

  long getPushStartTimestamp() {
    return pushStartTimestamp;
  }
}
