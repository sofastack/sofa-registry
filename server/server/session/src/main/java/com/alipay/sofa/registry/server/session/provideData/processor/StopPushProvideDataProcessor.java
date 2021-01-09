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
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version 1.0: StopPushProvideDataProcessor.java, v 0.1 2019-10-09 18:53 shangyu.wh Exp $
 */
public class StopPushProvideDataProcessor implements ProvideDataProcessor {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(StopPushProvideDataProcessor.class);

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private Registry            sessionRegistry;

    @Override
    public void changeDataProcess(ProvideData provideData) {
        if (provideData == null) {
            LOGGER.info("Fetch session stopPushSwitch null");
            return;
        }

        //push stop switch
        final Boolean stop = ProvideData.toBool(provideData);
        if (stop == null) {
            LOGGER.info("Fetch session stopPushSwitch content null");
            return;
        }
        boolean prev = sessionServerConfig.isStopPushSwitch();
        sessionServerConfig.setStopPushSwitch(stop);
        if (prev && !stop) {
            // prev is stop, now close stop, trigger push
            sessionRegistry.fetchChangDataProcess();
        }
        LOGGER.info("Fetch session stopPushSwitch={}, prev={}, current={}", stop, prev,
            sessionServerConfig.isStopPushSwitch());

    }

    @Override
    public void fetchDataProcess(ProvideData provideData) {
        changeDataProcess(provideData);
    }

    @Override
    public boolean support(ProvideData provideData) {
        return ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID.equals(provideData.getDataInfoId());
    }
}
