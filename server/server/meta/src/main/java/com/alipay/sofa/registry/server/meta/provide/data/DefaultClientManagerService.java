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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.shared.client.manager.BaseClientManagerService;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultClientManagerService.java, v 0.1 2021年05月12日 15:16 xiaojian.xj Exp $
 */
public class DefaultClientManagerService extends BaseClientManagerService
    implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOG = LoggerFactory.getLogger("CLIENT-MANAGER", "[clientManager]");

  @Autowired DateNowRepository dateNowRepository;

  @Autowired ClientManagerAddressRepository clientManagerAddressRepository;

  @Autowired MetaServerConfig metaServerConfig;

  @Autowired MetaLeaderService metaLeaderService;

  final Cleaner cleaner = new Cleaner();

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    start();
  }

  public void start() {
    ConcurrentUtils.createDaemonThread("clientManagerCleaner", cleaner).start();
    LOG.info("ClientManagerCleaner started");
  }

  final class Cleaner extends LoopRunnable {

    private final int maxRemoved = 200;

    @Override
    public void runUnthrowable() {
      if (!metaLeaderService.amIStableAsLeader()) {
        return;
      }

      doCleanExpired();
    }

    private void doCleanExpired() {
      Date date = dateBeforeNow(metaServerConfig.getClientManagerExpireDays());
      List<String> expireAddress =
          clientManagerAddressRepository.getExpireAddress(date, maxRemoved);
      if (CollectionUtils.isNotEmpty(expireAddress)) {
        int count = clientManagerAddressRepository.cleanExpired(expireAddress);
        LOG.info(
            "clean expired address, expect:{}, actual:{}, address:{}",
            expireAddress.size(),
            count,
            expireAddress);
      }

      int expireClientOffSize = clientManagerAddressRepository.getClientOffSizeBefore(date);
      LOG.info("expire client off address size:{}", expireClientOffSize);
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          metaServerConfig.getClientManagerCleanSecs(), TimeUnit.SECONDS);
    }
  }

  Date dateBeforeNow(int day) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateNowRepository.getNow());
    calendar.add(Calendar.DATE, -day);
    return calendar.getTime();
  }

  /**
   * Setter method for property <tt>clientManagerAddressRepository</tt>.
   *
   * @param clientManagerAddressRepository value to be assigned to property
   *     ClientManagerAddressRepository
   */
  @VisibleForTesting
  public void setClientManagerAddressRepository(
      ClientManagerAddressRepository clientManagerAddressRepository) {
    this.clientManagerAddressRepository = clientManagerAddressRepository;
  }

  /**
   * Setter method for property <tt>metaLeaderService</tt>.
   *
   * @param metaLeaderService value to be assigned to property metaLeaderService
   */
  @VisibleForTesting
  public void setMetaLeaderService(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
  }
}
