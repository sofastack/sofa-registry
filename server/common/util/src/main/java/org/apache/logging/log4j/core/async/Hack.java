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
package org.apache.logging.log4j.core.async;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.SLF4JLogger;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.slf4j.Log4jLogger;

public final class Hack {
  private static final org.apache.logging.log4j.Logger LOGGER = StatusLogger.getLogger();
  static final Field FIELD_LOG4J_LOGGER_EXTENDED;
  static final Field FIELD_DISRUPTOR;

  private static final CopyOnWriteArrayList<HackAsyncLoggerDisruptor> HACK_DISRUPTOR_LIST =
      new CopyOnWriteArrayList();

  private Hack() {}

  static {
    Field fieldLogger = null;
    Field fieldDisruptor = null;
    try {
      fieldLogger = FieldUtils.getField(Log4jLogger.class, "logger", true);
      fieldDisruptor = FieldUtils.getField(AsyncLogger.class, "loggerDisruptor", true);
    } catch (Throwable e) {
      LOGGER.error("failed to getDeclaredField to hack", e);
    }
    FIELD_LOG4J_LOGGER_EXTENDED = fieldLogger;
    FIELD_DISRUPTOR = fieldDisruptor;
    LOGGER.info("init hack field: {}", FIELD_LOG4J_LOGGER_EXTENDED);
    LOGGER.info("init hack field: {}", FIELD_DISRUPTOR);
    if (FIELD_LOG4J_LOGGER_EXTENDED != null && FIELD_DISRUPTOR != null) {
      ConcurrentUtils.createDaemonThread("async-log-hack-monitor", new Monitor()).start();
      LOGGER.info("start async-log-hack-monitor");
    }
  }

  public static final Logger hackLoggerDisruptor(Logger logger) {
    try {
      if (!(logger instanceof SLF4JLogger)) {
        LOGGER.info("skip hack disruptor, not SLF4JLogger, {}", logger);
        return logger;
      }
      if (FIELD_LOG4J_LOGGER_EXTENDED == null || FIELD_DISRUPTOR == null) {
        LOGGER.info("skip hack disruptor, not init field, {}", logger);
        return logger;
      }
      Object slf4jLogger = logger.getLogger();
      if (!(slf4jLogger instanceof Log4jLogger)) {
        LOGGER.info("skip hack disruptor, not Log4jLogger, {}", slf4jLogger);
        return logger;
      }
      Log4jLogger slf4j = (Log4jLogger) slf4jLogger;
      Object extendLogger = FIELD_LOG4J_LOGGER_EXTENDED.get(slf4j);
      if (!(extendLogger instanceof AsyncLogger)) {
        LOGGER.info("skip hack disruptor, not AsyncLogger, {}", extendLogger);
        return logger;
      }
      hackLoggerDisruptor(logger, (AsyncLogger) extendLogger);
    } catch (Throwable e) {
      LOGGER.error("failed to hackLoggerDisruptor of Logger:{}", logger, e);
    }
    return logger;
  }

  static void hackLoggerDisruptor(Logger logger, AsyncLogger asyncLogger) throws Exception {
    // new Disruptor to replace the shared Disruptor
    AsyncLoggerDisruptor origin = (AsyncLoggerDisruptor) FIELD_DISRUPTOR.get(asyncLogger);
    HackAsyncLoggerDisruptor hack =
        new HackAsyncLoggerDisruptor(origin.getContextName() + ":" + logger.getName());
    hack.setUseThreadLocals(origin.isUseThreadLocals());
    hack.start();
    FIELD_DISRUPTOR.set(asyncLogger, hack);
    HACK_DISRUPTOR_LIST.add(hack);
    LOGGER.info("hack LoggerDisruptor {} with {}, {}", origin, hack, logger);
  }

  static int printDisruptorStats() {
    Map<String, Long> counts = Maps.newHashMap();
    for (HackAsyncLoggerDisruptor d : HACK_DISRUPTOR_LIST) {
      counts.put(d.getContextName(), d.fullCountAndReset());
    }
    LOGGER.info("full count, hackSize={}, {}", counts.size(), counts);
    return counts.size();
  }

  static final class Monitor extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      printDisruptorStats();
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(30, TimeUnit.SECONDS);
    }
  }
}
