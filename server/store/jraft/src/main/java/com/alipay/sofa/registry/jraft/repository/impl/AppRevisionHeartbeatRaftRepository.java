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
package com.alipay.sofa.registry.jraft.repository.impl;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.config.MetadataConfig;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.AppRevisionHeartbeatRepository;
import com.alipay.sofa.registry.util.SingleFlight;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionHeartbeatRaftRepository.java, v 0.1 2021年02月09日 17:15 xiaojian.xj Exp $
 */
public class AppRevisionHeartbeatRaftRepository implements AppRevisionHeartbeatRepository {
  private static final Logger LOG = LoggerFactory.getLogger(AppRevisionHeartbeatRaftRepository.class);

  @Autowired
  private MetadataConfig metadataConfig;

  @Autowired
  private AppRevisionRaftRepository appRevisionRaftRepository;

  @Autowired
  private DefaultCommonConfig defaultCommonConfig;

  @Autowired
  private RheaKVStore rheaKVStore;

  private SingleFlight singleFlight = new SingleFlight();

  private Integer REVISION_GC_LIMIT;

  private static final Integer heartbeatCheckerSize = 1000;

  @PostConstruct
  public void postConstruct() {
    REVISION_GC_LIMIT = metadataConfig.getRevisionGcLimit();
  }
  
  public AppRevisionHeartbeatRaftRepository() {
  }

  @Override
  public void doAppRevisionHeartbeat() {}

  @Override
  public void doHeartbeatCacheChecker() {}

  @Override
  public void doAppRevisionGc(int silenceHour) {}
}
