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
package com.alipay.sofa.registry.server.session.strategy;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.server.session.cache.AppRevisionCacheRegistry;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;

public class DefaultAppRevisionHandlerStrategy implements AppRevisionHandlerStrategy {
    @Autowired
    private AppRevisionCacheRegistry appRevisionCacheService;

    @Override
    public void handleAppRevisionRegister(AppRevisionRegister appRevisionRegister,
                                          RegisterResponse response) {
        try {
            setDefaultField(appRevisionRegister);
            validate(appRevisionRegister);
            AppRevision revision = AppRevision.convert(appRevisionRegister);
            appRevisionCacheService.register(revision);
            response.setSuccess(true);
            response.setMessage("app revision register success!");
        } catch (Throwable e) {
            response.setSuccess(false);
            response.setMessage("app revision register failed!");
        }
    }

    private void setDefaultField(AppRevisionRegister register) {
        for (AppRevisionInterface inf : register.getInterfaceList()) {
            if (StringUtils.isBlank(inf.getInstanceId())) {
                inf.setInstanceId(DEFAULT_INSTANCE_ID);
            }
            if (StringUtils.isBlank(inf.getGroup())) {
                inf.setGroup(DEFAULT_GROUP);
            }
        }
    }

    private void validate(AppRevisionRegister register) {
        if (StringUtils.isBlank(register.getAppName())) {
            throw new IllegalArgumentException("register appName is empty");
        }
        if (StringUtils.isBlank(register.getRevision())) {
            throw new IllegalArgumentException("register revision is empty");
        }
        for (AppRevisionInterface inf : register.getInterfaceList()) {
            DataInfo.toDataInfoId(inf.getDataId(), inf.getInstanceId(), inf.getGroup());
        }
    }
}