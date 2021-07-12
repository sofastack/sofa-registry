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
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.domain.AppRevisionDomain;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionRaftRepository.java, v 0.1 2021年01月17日 15:57 xiaojian.xj Exp $
 */
public class AppRevisionRaftRepository implements AppRevisionRepository {
  private static final Logger LOG = LoggerFactory.getLogger(AppRevisionRaftRepository.class);

  @Autowired
  private RheaKVStore rheaKVStore;
  
  /** map: <revision, AppRevision> */
  private final Map<String, AppRevision> registry = new ConcurrentHashMap<>();

  private Map<String, AppRevisionDomain> appRevisionMap=new ConcurrentHashMap<>();

  /** map: <revision, AppRevision> */
  private final AtomicReference<ConcurrentHashMap.KeySetView> heartbeatSet =
          new AtomicReference<>();

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Resource
  private InterfaceAppsRaftRepository interfaceAppsRaftRepository;

  @Override
  public void register(AppRevision appRevision) {
    if (this.registry.containsKey(appRevision.getRevision())) {
      return;
    }
  }

  @Override
  public void refresh() {

  }

  @Override
  public AppRevision queryRevision(String revision) {
    return registry.get(revision);
  }

  @Override
  public boolean heartbeat(String revision) {
    if(heartbeatSet.get().contains(revision)){
      return true;
    }

    byte[] bytes = rheaKVStore.bGet("AppRevision");
    Map<String, AppRevisionDomain> map = CommandCodec.decodeCommand(bytes, appRevisionMap.getClass());
    AppRevisionDomain appRevisionDomain = map.get(defaultCommonConfig.getClusterId());

    
    return false;
  }


}
