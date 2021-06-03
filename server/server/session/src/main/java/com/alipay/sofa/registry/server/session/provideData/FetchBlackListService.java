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
package com.alipay.sofa.registry.server.session.provideData;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistConfig;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistConstants;
import com.alipay.sofa.registry.server.session.filter.blacklist.MatchType;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: FetchBlackListService.java, v 0.1 2021年05月16日 17:59 xiaojian.xj Exp $
 */
public class FetchBlackListService extends AbstractFetchSystemPropertyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchBlackListService.class);

  private volatile List<BlacklistConfig> blacklistConfigList = new ArrayList();

  @Autowired private Registry sessionRegistry;

  @Autowired private ConnectionsService connectionsService;

  public FetchBlackListService() {
    super(ValueConstants.BLACK_LIST_DATA_ID);
  }

  @Override
  protected boolean doProcess(ProvideData provideData) {
    // black list data
    final String data = ProvideData.toString(provideData);
    if (data == null) {
      LOGGER.warn("Fetch session blacklist content null");
      return false;
    }

    LOGGER.info("Fetch session blacklist {}", data);

    List<BlacklistConfig> blacklistConfigs = new ArrayList();
    // {"FORBIDDEN_PUB":{"IP_FULL":["1.1.1.1","10.15.233.150"]},"FORBIDDEN_SUB_BY_PREFIX":{"IP_FULL":["1.1.1.1"]}}
    Map<String, Map<String, Set<String>>> blacklistConfigMap =
        convertBlacklistConfig(data, blacklistConfigs);
    clientOffBlackIp(blacklistConfigMap);

    // after cancel success
    writeLock.lock();
    try {
      blacklistConfigList = blacklistConfigs;
      version.set(provideData.getVersion());
    } catch (Throwable t) {
      LOGGER.error("update blacklist:{} error.", provideData, t);
    } finally {
      writeLock.unlock();
    }

    return true;
  }

  private Map<String, Map<String, Set<String>>> convertBlacklistConfig(
      String config, List<BlacklistConfig> blacklistConfigs) {

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
      return blacklistConfigMap;
    } catch (Throwable e) {
      LOGGER.error("[cmd] setBlacklistConfig error", e);
      return null;
    }
  }

  private void clientOffBlackIp(Map<String, Map<String, Set<String>>> blacklistConfigMap) {
    if (blacklistConfigMap != null) {
      Set<String> ipSet = new HashSet();

      for (Map.Entry<String, Map<String, Set<String>>> configEntry :
          blacklistConfigMap.entrySet()) {
        if (BlacklistConstants.FORBIDDEN_PUB.equals(configEntry.getKey())
            || BlacklistConstants.FORBIDDEN_SUB_BY_PREFIX.equals(configEntry.getKey())) {
          Map<String, Set<String>> typeMap = configEntry.getValue();
          if (typeMap != null) {
            for (Map.Entry<String, Set<String>> typeEntry : typeMap.entrySet()) {
              if (BlacklistConstants.IP_FULL.equals(typeEntry.getKey())) {
                if (typeEntry.getValue() != null) {
                  ipSet.addAll(typeEntry.getValue());
                }
              }
            }
          }
        }
      }

      List<ConnectId> conIds = connectionsService.getIpConnects(ipSet);
      sessionRegistry.clientOff(conIds);
    }
  }

  /**
   * Getter method for property <tt>blacklistConfigList</tt>.
   *
   * @return property value of blacklistConfigList
   */
  public List<BlacklistConfig> getBlacklistConfigList() {
    return blacklistConfigList;
  }
}
