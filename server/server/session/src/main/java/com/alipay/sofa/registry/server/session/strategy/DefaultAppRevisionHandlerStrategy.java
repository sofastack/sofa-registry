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

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.client.pb.AppList;
import com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse;
import com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatResponse;
import com.alipay.sofa.registry.common.model.client.pb.ServiceAppMappingResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.converter.pb.AppRevisionConvertor;
import com.alipay.sofa.registry.server.session.metadata.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.metadata.AppRevisionHeartbeatRegistry;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultAppRevisionHandlerStrategy implements AppRevisionHandlerStrategy {

    private static final Logger          LOG = LoggerFactory
                                                 .getLogger(DefaultAppRevisionHandlerStrategy.class);

    @Autowired
    private AppRevisionCacheRegistry     appRevisionCacheService;

    @Autowired
    private AppRevisionHeartbeatRegistry appRevisionHeartbeatRegistry;

    @Override
    public void handleAppRevisionRegister(AppRevision appRevision, RegisterResponse response) {
        try {
            validate(appRevision);
            appRevisionCacheService.register(appRevision);
            response.setSuccess(true);
            response.setMessage("app revision register success!");
        } catch (Throwable e) {
            response.setSuccess(false);
            response.setMessage("app revision register failed!");
            LOG.error("app revision register error.", e);
        }
    }

    @Override
    public ServiceAppMappingResponse queryApps(List<String> services) {
        ServiceAppMappingResponse.Builder builder = ServiceAppMappingResponse.newBuilder();

        int statusCode = ValueConstants.METADATA_STATUS_PROCESS_SUCCESS;
        try {
            for (String service : Optional.ofNullable(services).orElse(new ArrayList<>())) {
                InterfaceMapping appNames = appRevisionCacheService.getAppNames(service);
                AppList.Builder build = AppList.newBuilder().addAllApps(appNames.getApps());
                build.setVersion(appNames.getNanosVersion());
                builder.putServiceAppMapping(service, build.build());
            }
        } catch (Throwable e) {
            statusCode = ValueConstants.METADATA_STATUS_PROCESS_ERROR;
            builder.setMessage(String
                .format("query apps by services error. service: %s.", services));
            LOG.error(String.format("query apps by services error. service: %s", services), e);

        }
        builder.setStatusCode(statusCode);
        return builder.build();
    }

    @Override
    public GetRevisionsResponse queryRevision(List<String> revisions) {
        GetRevisionsResponse.Builder builder = GetRevisionsResponse.newBuilder();
        int statusCode = ValueConstants.METADATA_STATUS_PROCESS_SUCCESS;
        try {
            for (String revision : Optional.ofNullable(revisions).orElse(new ArrayList<>())) {
                AppRevision appRevision = appRevisionCacheService.getRevision(revision);
                if (appRevision == null) {
                    statusCode = ValueConstants.METADATA_STATUS_DATA_NOT_FOUND;
                    builder.setMessage(String.format("query revision: %s fail.", revision));
                    LOG.error("query revision {} fail", revision);
                }
                builder.putRevisions(revision, AppRevisionConvertor.convert2Pb(appRevision));
            }
        } catch (Throwable e) {
            statusCode = ValueConstants.METADATA_STATUS_PROCESS_ERROR;
            builder.setMessage(String.format("query revisions: %s error.", revisions));
            LOG.error("query revision {} error", revisions, e);
        }
        builder.setStatusCode(statusCode);
        return builder.build();
    }

    @Override
    public MetaHeartbeatResponse heartbeat(List<String> revisions) {

        MetaHeartbeatResponse.Builder builder = MetaHeartbeatResponse.newBuilder();
        int statusCode = ValueConstants.METADATA_STATUS_PROCESS_SUCCESS;
        try {
            for (String revision : Optional.ofNullable(revisions).orElse(new ArrayList<>())) {
                AppRevision appRevision = appRevisionHeartbeatRegistry.heartbeat(revision);
                if (appRevision == null) {
                    statusCode = ValueConstants.METADATA_STATUS_DATA_NOT_FOUND;
                    builder.setMessage(String.format("revision: %s heartbeat fail.", revision));
                    LOG.error("revision heartbeat {} fail", revision);
                }
            }
        } catch (Throwable e) {
            statusCode = ValueConstants.METADATA_STATUS_PROCESS_ERROR;
            builder.setMessage(String.format("revisions: %s heartbeat error.", revisions));
            LOG.error("revision heartbeat {} error", revisions, e);
        }
        builder.setStatusCode(statusCode);
        return builder.build();
    }

    private void validate(AppRevision appRevision) {
        if (StringUtils.isBlank(appRevision.getAppName())) {
            throw new IllegalArgumentException("register appName is empty");
        }
        if (StringUtils.isBlank(appRevision.getRevision())) {
            throw new IllegalArgumentException("register revision is empty");
        }
    }
}