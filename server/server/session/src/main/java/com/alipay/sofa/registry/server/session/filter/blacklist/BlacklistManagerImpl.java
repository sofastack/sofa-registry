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

import com.alipay.sofa.jraft.util.ThreadPoolUtil;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version 1.0: BlacklistManagerImpl.java, v 0.1 2019-06-19 18:30 shangyu.wh Exp $
 */
public class BlacklistManagerImpl extends AbstractLifecycle implements BlacklistManager {

    private static final Logger      LOGGER              = LoggerFactory
                                                             .getLogger(BlacklistManagerImpl.class);

    private static final Logger      EXCHANGE_LOGGER     = LoggerFactory
                                                             .getLogger("SESSION-EXCHANGE");

    @Autowired
    protected MetaServerService      metaNodeService;

    private List<BlacklistConfig>    blacklistConfigList = new ArrayList();

    private ScheduledExecutorService scheduled;

    private ScheduledFuture<?>       future;

    @PostConstruct
    public void postConstruct() throws Exception {
        LifecycleHelper.initializeIfPossible(this);
        LifecycleHelper.startIfPossible(this);
    }

    @PreDestroy
    public void preDestroy() throws Exception {
        LifecycleHelper.stopIfPossible(this);
        LifecycleHelper.disposeIfPossible(this);
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        scheduled = ThreadPoolUtil.newScheduledBuilder()
            .coreThreads(Math.min(2, OsUtils.getCpuCount())).enableMetric(true)
            .poolName(BlacklistManager.class.getSimpleName())
            .threadFactory(new NamedThreadFactory(BlacklistManager.class.getSimpleName())).build();
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                fetchStopPushSwitch();
            }
        }, getIntervalMilli(), getIntervalMilli(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() throws StopException {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        super.doStop();
    }

    @Override
    protected void doDispose() throws DisposeException {
        if (scheduled != null) {
            scheduled.shutdownNow();
            scheduled = null;
        }
        super.doDispose();
    }

    private int getIntervalMilli() {
        return 30 * 1000;
    }

    @Override
    public void load() {
        fetchStopPushSwitch();
    }

    @Override
    public List<BlacklistConfig> getBlacklistConfigList() {
        return blacklistConfigList;
    }

    @Override
    public void setBlacklistConfigList(List<BlacklistConfig> blacklistConfigList) {
        this.blacklistConfigList = blacklistConfigList;
    }

    private void fetchStopPushSwitch() {
        ProvideData provideData = metaNodeService.fetchData(ValueConstants.BLACK_LIST_DATA_ID);
        if (provideData != null) {
            if (provideData.getProvideData() == null
                || provideData.getProvideData().getObject() == null) {
                LOGGER.info("Fetch session blacklist no data existed,current config not change!");
                return;
            }
            String data = (String) provideData.getProvideData().getObject();
            if (data != null) {
                convertBlacklistConfig(data);
                EXCHANGE_LOGGER.info("Fetch session blacklist data switch {} success!", data);
            } else {
                LOGGER.info("Fetch session blacklist data null,current config not change!");
            }
        } else {
            LOGGER.info("Fetch session blacklist data null,config not change!");
        }
    }

    /**
     * @see BlacklistManager#convertBlacklistConfig(String)
     */
    public Map<String, Map<String, Set<String>>> convertBlacklistConfig(String config) {

        TypeReference<HashMap<String, HashMap<String, HashSet<String>>>> typeReference = new TypeReference<HashMap<String, HashMap<String, HashSet<String>>>>() {
        };

        ObjectMapper mapper = JsonUtils.getJacksonObjectMapper();

        Map<String, Map<String, Set<String>>> blacklistConfigMap;
        try {
            blacklistConfigMap = mapper.readValue(config, typeReference);
        } catch (IOException e) {
            LOGGER.error("Parser config json error!", e);
            return null;
        }
        if (null == blacklistConfigMap) {
            LOGGER.info("[cmd] setBlacklistConfig fail, params is null");
            return null;
        }
        try {
            List<BlacklistConfig> blacklistConfigs = new ArrayList();
            for (Entry<String, Map<String, Set<String>>> configEntry : blacklistConfigMap
                .entrySet()) {
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

            setBlacklistConfigList(blacklistConfigs);
            return blacklistConfigMap;
        } catch (Exception e) {
            LOGGER.error("[cmd] setBlacklistConfig error", e);
            return null;
        }
    }
}