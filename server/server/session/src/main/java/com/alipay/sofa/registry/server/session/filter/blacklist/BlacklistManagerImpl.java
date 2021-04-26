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
package com.alipay.sofa.registry.server.session.filter.blacklist;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version 1.0: BlacklistManagerImpl.java, v 0.1 2019-06-19 18:30 shangyu.wh Exp $
 */
public class BlacklistManagerImpl implements BlacklistManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistManagerImpl.class);

  @Autowired protected MetaServerService metaNodeService;

  volatile List<BlacklistConfig> blacklistConfigList = new ArrayList();

  private final WatchDog watchDog = new WatchDog();

  private final class WatchDog extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      fetchBlackList();
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(30, TimeUnit.SECONDS);
    }
  }

  @Override
  public void load() {
    fetchBlackList();
    ConcurrentUtils.createDaemonThread("BlacklistManager-load", watchDog).start();
  }

  @Override
  public List<BlacklistConfig> getBlacklistConfigList() {
    return blacklistConfigList;
  }

  private void fetchBlackList() {
    ProvideData provideData = metaNodeService.fetchData(ValueConstants.BLACK_LIST_DATA_ID);
    String data = ProvideData.toString(provideData);
    if (data != null) {
      convertBlacklistConfig(data);
      LOGGER.info("Fetch session blacklist data switch {} success!", data);
    } else {
      LOGGER.info("Fetch session blacklist data null,current config not change!");
    }
  }

  /** @see BlacklistManager#convertBlacklistConfig(String) */
  public Map<String, Map<String, Set<String>>> convertBlacklistConfig(String config) {

    TypeReference<HashMap<String, HashMap<String, HashSet<String>>>> typeReference =
        new TypeReference<HashMap<String, HashMap<String, HashSet<String>>>>() {};

    ObjectMapper mapper = JsonUtils.getJacksonObjectMapper();

    Map<String, Map<String, Set<String>>> blacklistConfigMap;
    try {
      blacklistConfigMap = mapper.readValue(config, typeReference);
    } catch (Throwable e) {
      LOGGER.error("Parser config json error!", e);
      return null;
    }
    if (null == blacklistConfigMap) {
      LOGGER.info("[cmd] setBlacklistConfig fail, params is null");
      return null;
    }
    try {
      List<BlacklistConfig> blacklistConfigs = new ArrayList();
      for (Entry<String, Map<String, Set<String>>> configEntry : blacklistConfigMap.entrySet()) {
        BlacklistConfig blacklistConfig = new BlacklistConfig();
        blacklistConfig.setType(configEntry.getKey());

        List<MatchType> matchTypeList = new ArrayList();

        Map<String, Set<String>> matchTypeMap = configEntry.getValue();
        for (Entry<String, Set<String>> typeEntry : matchTypeMap.entrySet()) {
          String type = typeEntry.getKey();

          MatchType<String> ipFullMatchType = new MatchType();
          ipFullMatchType.setType(type);
          ipFullMatchType.setPatternSet(typeEntry.getValue());
          matchTypeList.add(ipFullMatchType);
        }
        blacklistConfig.setMatchTypes(matchTypeList);
        blacklistConfigs.add(blacklistConfig);
      }
      this.blacklistConfigList = blacklistConfigs;
      return blacklistConfigMap;
    } catch (Throwable e) {
      LOGGER.error("[cmd] setBlacklistConfig error", e);
      return null;
    }
  }
}
