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

/**
 *  generates ID: 49 bit millisecond timestamp + 15 bit incremental ID
 *
 *  refer to: https://github.com/twitter/snowflake
 *
 * @author kezhu.wukz
 * @version $Id: DatumVersionUtil.java, v 0.1 2019-07-04 22:05 kezhu.wukz Exp $
 */
public final class DatumVersionUtil {
    private DatumVersionUtil() {
    }

    private static long sequence           = 0L;

    /** Tue Jan 01 00:00:00 CST 2019 */
    private static long twepoch            = 1546272000000L;

    private static long sequenceBits       = 15L;
    private static long timestampLeftShift = sequenceBits;
    private static long sequenceMask       = -1L ^ (-1L << sequenceBits);

    private static long lastTimestamp      = -1L;

    public synchronized static long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                "Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp
                                                                                      - timestamp));
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
        return (id >> timestampLeftShift) + twepoch;
    }

    private static long untilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private static long timeGen() {
        return System.currentTimeMillis();
    }

}