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

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.SubPublisher;
import com.alipay.sofa.registry.compress.CompressUtils;
import com.alipay.sofa.registry.concurrent.ThreadLocalStringBuilder;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.trace.TraceID;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

  private Tuple<List<Long>, String> datumPushedDelayList(long finishedTs, long lastPushTs) {
    List<Long> recentVersions = datum.getRecentVersions();
    List<Long> timestamps =
        Lists.newArrayListWithCapacity(recentVersions == null ? 1 : recentVersions.size() + 1);
    StringBuilder builder = ThreadLocalStringBuilder.get();
    if (!CollectionUtils.isEmpty(recentVersions)) {
      for (long v : recentVersions) {
        long ts = DatumVersionUtil.getRealTimestamp(v);
        if (ts > lastPushTs) {
          long delay = finishedTs - ts;
          timestamps.add(delay);
          builder.append(delay).append(',');
        }
      }
    }

    long datumChangeTs = DatumVersionUtil.getRealTimestamp(datum.getVersion());
    long delay = finishedTs - Math.max(lastPushTs, datumChangeTs);
    timestamps.add(delay);
    builder.append(delay);
    return new Tuple<>(timestamps, builder.toString());
  }

  public void startPush() {
    this.pushStartTimestamp = System.currentTimeMillis();
  }

  public void finishPush(
      PushStatus status,
      TraceID taskID,
      long subscriberPushedVersion,
      int pushNum,
      int retry,
      String pushEncode,
      int encodeSize) {
    try {
      finish(status, taskID, subscriberPushedVersion, pushNum, retry, pushEncode, encodeSize);
    } catch (Throwable t) {
      LOGGER.error(
          "finish push error, {},{},{},{}",
          datum.getDataInfoId(),
          datum.getVersion(),
          subAddress,
          taskID,
          t);
    }
  }

  private void finish(
      PushStatus status,
      TraceID taskID,
      long subscriberPushedVersion,
      int pushNum,
      int retry,
      String pushEncode,
      int encodeSize) {
    final long pushFinishTimestamp = System.currentTimeMillis();
    // push.finish- first.newly.datumTimestamp(after subscriberPushedVersion)
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

    // try find the earliest and the latest publisher after the subPushedVersion
    // that means the modify after last push, but this could not handle the publisher.remove
    final long lastTriggerSession = pushCause.triggerPushCtx.getLastTimes().getTriggerSession();
    if (pushCause.pushType == PushType.Reg) {
      datumVersionPushSpanMillis = pushFinishTimestamp - subRegTimestamp;
      datumVersionTriggerSpanMillis = Math.max(lastTriggerSession - subRegTimestamp, 0);
    } else {
      datumVersionPushSpanMillis = Math.max(pushFinishTimestamp - pushCause.datumTimestamp, 0);
      datumVersionTriggerSpanMillis = Math.max(lastTriggerSession - pushCause.datumTimestamp, 0);
      if (pushCause.pushType == PushType.Sub) {
        if (subRegTimestamp >= pushCause.datumTimestamp) {
          // case: datum.change trigger the sub.sub, but the sub.reg not finish
          datumVersionPushSpanMillis = pushFinishTimestamp - subRegTimestamp;
          datumVersionTriggerSpanMillis = Math.max(lastTriggerSession - subRegTimestamp, 0);
        }
      }
    }

    // calc the task span millis
    pushTaskPrepareSpanMillis =
        pushCreateTimestamp - pushCause.triggerPushCtx.getFirstTimes().getTriggerSession();
    pushTaskQueueSpanMillis = pushStartTimestamp - pushCreateTimestamp;
    pushTaskClientIOSpanMillis = pushFinishTimestamp - pushStartTimestamp;
    pushTaskSessionSpanMillis =
        pushStartTimestamp - pushCause.triggerPushCtx.getFirstTimes().getTriggerSession();

    final long lastPushTimestamp =
        subscriberPushedVersion <= ValueConstants.DEFAULT_NO_DATUM_VERSION
            ? subRegTimestamp
            : DatumVersionUtil.getRealTimestamp(subscriberPushedVersion);

    Tuple<List<Long>, String> datumPushedDelay =
        datumPushedDelayList(pushFinishTimestamp, lastPushTimestamp);
    List<Long> datumPushedDelayList = datumPushedDelay.o1;
    String pushDatumDelayStr = datumPushedDelay.o2;

    datumModifyPushSpanMillis = datumVersionPushSpanMillis;

    if (pushCause.pushType == PushType.Sub) {
      datumModifyPushSpanMillis = Math.max(datumPushedDelayList.get(0), datumVersionPushSpanMillis);
    }

    PushMetrics.Push.observePushDelayHistogram(
        pushCause.pushType, datumModifyPushSpanMillis, status);
    if (LOGGER.isInfoEnabled() || SLOW_LOGGER.isInfoEnabled()) {
      final String msg =
          StringFormatter.format(
              "{},{},{},ver={},app={},cause={},pubNum={},pubBytes={},delay={},{},{},{},{},"
                  + "session={},cliIO={},"
                  + "subNum={},addr={},expectVer={},dataNode={},taskID={},pushedVer={},regTs={},"
                  + "{},recentDelay={},pushNum={},retry={},encode={},encSize={}",
              status,
              datum.getDataInfoId(),
              datum.getDataCenter(),
              datum.getVersion(),
              subApp,
              pushCause.pushType,
              datum.getPubNum(),
              datum.getDataBoxBytes(),
              datumModifyPushSpanMillis,
              datumVersionPushSpanMillis,
              datumVersionTriggerSpanMillis,
              pushTaskPrepareSpanMillis,
              pushTaskQueueSpanMillis,
              pushTaskSessionSpanMillis,
              pushTaskClientIOSpanMillis,
              subNum,
              subAddress,
              pushCause.triggerPushCtx.getExpectDatumVersion(),
              pushCause.triggerPushCtx.dataNode,
              taskID,
              subscriberPushedVersion,
              subRegTimestamp,
              pushCause.triggerPushCtx.formatTraceTimes(pushFinishTimestamp),
              pushDatumDelayStr,
              pushNum,
              retry,
              CompressUtils.normalizeEncode(pushEncode),
              encodeSize);
      LOGGER.info(msg);
      if (datumModifyPushSpanMillis > 6000) {
        SLOW_LOGGER.info(msg);
      }
    }
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
