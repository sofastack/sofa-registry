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
package com.alipay.sofa.registry.server.data.providedata;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.DataChangeMergeConfig;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jiangcun.hlc@antfin.com
 * @since 2023/2/21
 */
public class FetchDataChangeMergerConfigService
        extends AbstractFetchSystemPropertyService<FetchDataChangeMergerConfigService.SwitchStorage> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FetchDataChangeMergerConfigService.class);

    @Autowired
    private DataServerConfig dataServerConfig;

    @Autowired
    protected DataChangeEventCenter dataChangeEventCenter;


    public FetchDataChangeMergerConfigService() {
        super(
                ValueConstants.DATA_MERGER_TASK_DELAY_CONFIG_DATA_ID,
                new SwitchStorage(INIT_VERSION, new DataChangeMergeConfig()));
    }

    @Override
    protected int getSystemPropertyIntervalMillis() {
        return dataServerConfig.getSystemPropertyIntervalMillis();
    }

    @Override
    protected boolean doProcess(SwitchStorage expect, ProvideData data) {
        final String configString = ProvideData.toString(data);

        DataChangeMergeConfig dataChangeMergeConfig = null;
        if (StringUtils.isBlank(configString)) {
            return true;
        }
        try {
            dataChangeMergeConfig = JsonUtils.read(configString, DataChangeMergeConfig.class);
        } catch (Throwable e) {
            LOGGER.error("Decode DataChangeMergeConfig failed", e);
            return false;
        }
        if (dataChangeMergeConfig == null) {
            LOGGER.error(
                    "Fetch DataChangeMergeConfig invalid, value={}", dataChangeMergeConfig);
            return false;
        }

        SwitchStorage update = new SwitchStorage(data.getVersion(), dataChangeMergeConfig);
        if (!compareAndSet(expect, update)) {
            return false;
        }

        dataChangeEventCenter.setDataChangeConfig(dataChangeMergeConfig);

        LOGGER.info(
                "Fetch DataChangeMergeConfig success, prev={}, current={}",
                expect.dataChangeMergeConfig,
                dataChangeMergeConfig);
        return true;
    }

    protected static class SwitchStorage extends SystemDataStorage {
        protected final DataChangeMergeConfig dataChangeMergeConfig;

        public SwitchStorage(long version, DataChangeMergeConfig dataChangeMergeConfig) {
            super(version);
            this.dataChangeMergeConfig = dataChangeMergeConfig;
        }
    }
    @VisibleForTesting
    public FetchDataChangeMergerConfigService setDataServerConfig(DataServerConfig dataServerConfig) {
        this.dataServerConfig = dataServerConfig;
        return this;
    }
    @VisibleForTesting
    public FetchDataChangeMergerConfigService setDataChangeEventCenter(DataChangeEventCenter dataChangeEventCenter) {
        this.dataChangeEventCenter = dataChangeEventCenter;
        return this;
    }


}
