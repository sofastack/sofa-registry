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
package com.alipay.sofa.registry.util;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;

/**
 * generates ID: 49 bit millisecond timestamp + 15 bit incremental ID
 *
 * <p>refer to: https://github.com/twitter/snowflake
 *
 * @author kezhu.wukz
 * @version $Id: DatumVersionUtil.java, v 0.1 2019-07-04 22:05 kezhu.wukz Exp $
 */
public final class DatumVersionUtil {
  private static final Logger LOG = LoggerFactory.getLogger(DatumVersionUtil.class);

  static final String DATUM_VERSION_TYPE_CONFREG = "confreg";
  static final String DATUM_VERSION_TYPE_REGISTRY = "registry";
  static String datumVersionType =
      SystemUtils.getSystem("registry.data.datumVersionType", DATUM_VERSION_TYPE_REGISTRY);

  private DatumVersionUtil() {}

  private static long sequence = 0L;

  /** Tue Jan 01 00:00:00 CST 2019 */
  private static final long twepoch = 1546272000000L;

  /** Tue Jan 01 00:00:00 CST 2021 */
  private static final long timeStart = 1609459200000L;

  private static final long sequenceBits = 15L;
  private static final long timestampLeftShift = sequenceBits;
  private static final long sequenceMask = -1L ^ (-1L << sequenceBits);

  private static long lastTimestamp = -1L;

  private static final long registryMinVersion = (timeStart - twepoch) << timestampLeftShift;

  public static synchronized long nextId() {
    long timestamp = timeGen();
    if (timestamp < lastTimestamp) {
      throw new RuntimeException(
          String.format(
              "Clock moved backwards. Refusing to generate id for %d milliseconds",
              lastTimestamp - timestamp));
    }

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & sequenceMask;
      if (sequence == 0) {
        timestamp = untilNextMillis(lastTimestamp);
      }
    } else {
      sequence = 0L;
    }
    lastTimestamp = timestamp;
    return ((timestamp - twepoch) << timestampLeftShift) | sequence;
  }

  public static long getRealTimestamp(long id) {
    if (versionType(id).equals(DATUM_VERSION_TYPE_CONFREG)) {
      return id;
    }
    return (id >> timestampLeftShift) + twepoch;
  }

  static long untilNextMillis(long lastTimestamp) {
    long timestamp = timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen();
    }
    return timestamp;
  }

  public static boolean useConfregVersionGen() {
    return StringUtils.equals(datumVersionType, DATUM_VERSION_TYPE_CONFREG);
  }

  public static long confregNextId(long lastVersion) {
    long timestamp = timeGen();
    if (lastVersion < timestamp) {
      return timestamp;
    }
    if (lastVersion > timestamp + 10) {
      LOG.warn(
          "[DatumVersion] last version {} is higher than currentTimestamp {}",
          lastVersion,
          timestamp);
    }
    return lastVersion + 1;
  }

  public static long transferDatumVersion(long version) {
    if (version <= 1) {
      // empty datum
      return version;
    }
    if (useConfregVersionGen()) {
      return getRealTimestamp(version);
    }
    return version;
  }

  @VisibleForTesting
  static String versionType(long version) {
    if (version > registryMinVersion) {
      return DATUM_VERSION_TYPE_REGISTRY;
    }
    return DATUM_VERSION_TYPE_CONFREG;
  }

  private static long timeGen() {
    return System.currentTimeMillis();
  }
}
