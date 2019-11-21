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
package com.alipay.sofa.registry.server.session.provideData.processor;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version 1.0: RenewSnapshotProvideDataProcessor.java, v 0.1 2019-10-09 20:30 shangyu.wh Exp $
 */
public class RenewSnapshotProvideDataProcessor implements ProvideDataProcessor {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(RenewSnapshotProvideDataProcessor.class);

    @Autowired
    private Registry            sessionRegistry;

    @Override
    public void changeDataProcess(ProvideData provideData) {
        //stop renew switch
        if (provideData == null || provideData.getProvideData() == null
            || provideData.getProvideData().getObject() == null) {
            LOGGER
                .info("Fetch enableDataRenewSnapshot but no data existed, current config not change!");
            return;
        }
        boolean enableDataRenewSnapshot = Boolean.parseBoolean((String) provideData
            .getProvideData().getObject());
        LOGGER.info("Fetch enableDataRenewSnapshot {} success!", enableDataRenewSnapshot);
        this.sessionRegistry.setEnableDataRenewSnapshot(enableDataRenewSnapshot);
        return;
    }

    @Override
    public void fetchDataProcess(ProvideData provideData) {
        if (provideData == null || provideData.getProvideData() == null
            || provideData.getProvideData().getObject() == null) {
            LOGGER
                .info("Fetch enableDataRenewSnapshot but no data existed, current config not change!");
            return;
        }
        boolean enableDataRenewSnapshot = Boolean.parseBoolean((String) provideData
            .getProvideData().getObject());
        LOGGER.info("Fetch enableDataRenewSnapshot {} success!", enableDataRenewSnapshot);
        this.sessionRegistry.setEnableDataRenewSnapshot(enableDataRenewSnapshot);
    }

    @Override
    public boolean support(ProvideData provideData) {
        return ValueConstants.ENABLE_DATA_RENEW_SNAPSHOT.equals(provideData.getDataInfoId());
    }
}