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
package com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.processor;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessor;

/**
 *
 * @author kezhu.wukz
 * @version 1.0: DatumExpireProvideDataProcessor.java, v 0.1 2019-12-26 20:30 kezhu.wukz Exp $
 */
public class DatumExpireProvideDataProcessor implements ProvideDataProcessor {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(DatumExpireProvideDataProcessor.class);

    @Override
    public void changeDataProcess(ProvideData provideData) {
        if (checkInvalid(provideData)) {
            return;
        }
        //TODO enable datum expire
        int sessionLeaseSec = ProvideData.toInteger(provideData);
        LOGGER.info("Fetch sessionLeaseSec {} success!", sessionLeaseSec);
    }

    private boolean checkInvalid(ProvideData provideData) {
        boolean invalid = provideData == null || provideData.getProvideData() == null
                          || provideData.getProvideData().getObject() == null;
        if (invalid) {
            LOGGER.warn("Fetch enableDataDatumExpire return invalid data, provideData: {}",
                provideData);
        }
        return invalid;
    }

    @Override
    public boolean support(ProvideData provideData) {
        return ValueConstants.DATA_SESSION_LEASE_SEC.equals(provideData.getDataInfoId());
    }
}