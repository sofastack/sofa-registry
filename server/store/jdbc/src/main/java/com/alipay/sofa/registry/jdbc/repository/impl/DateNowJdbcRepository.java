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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.jdbc.mapper.DateNowMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : DateNowJdbcRepository.java, v 0.1 2021年09月29日 15:09 xiaojian.xj Exp $
 */
public class DateNowJdbcRepository implements DateNowRepository {

  private static final Logger LOG = LoggerFactory.getLogger(DateNowJdbcRepository.class);

  private volatile Date dateNow;

  @Autowired DateNowMapper dateNowMapper;

  private final DateTimeWatcher watcher = new DateTimeWatcher();

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread(this.getClass().getSimpleName() + "WatchDog", watcher)
        .start();
  }

  @Override
  public Date getNow() {
    if (dateNow != null) {
      return dateNow;
    }
    dateNow = dateNowMapper.getNow().getNow();
    LOG.info("[Load]getNow dateNow: {}", dateNow);
    return dateNow;
  }

  class DateTimeWatcher extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      dateNow = dateNowMapper.getNow().getNow();
      LOG.info("[Load]watcher dateNow: {}", dateNow);
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Setter method for property <tt>dateNowMapper</tt>.
   *
   * @param dateNowMapper value to be assigned to property dateNowMapper
   */
  @VisibleForTesting
  public void setDateNowMapper(DateNowMapper dateNowMapper) {
    this.dateNowMapper = dateNowMapper;
  }
}
