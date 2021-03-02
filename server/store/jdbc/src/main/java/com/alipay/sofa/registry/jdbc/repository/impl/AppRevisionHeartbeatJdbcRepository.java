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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionQueryModel;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.jdbc.repository.JdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.batch.AppRevisionHeartbeatBatchCallable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.AppRevisionHeartbeatRepository;
import com.alipay.sofa.registry.util.BatchCallableRunnable.InvokeFuture;
import com.alipay.sofa.registry.util.BatchCallableRunnable.TaskEvent;
import com.alipay.sofa.registry.util.SingleFlight;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AppRevisionHeartbeatJdbcRepository.java, v 0.1 2021年02月09日 17:14 xiaojian.xj Exp $
 */
public class AppRevisionHeartbeatJdbcRepository implements AppRevisionHeartbeatRepository,
                                               JdbcRepository {

    private static final Logger               LOG          = LoggerFactory.getLogger(
                                                               "METADATA-EXCHANGE",
                                                               "[AppRevisionHeartbeat]");

    @Resource
    private AppRevisionJdbcRepository         appRevisionJdbcRepository;

    @Autowired
    private AppRevisionMapper                 appRevisionMapper;

    @Autowired
    private AppRevisionHeartbeatBatchCallable appRevisionHeartbeatBatchCallable;

    @Autowired
    private MetadataConfig                    metadataConfig;

    private SingleFlight                      singleFlight = new SingleFlight();

    private Integer                           REVISION_GC_LIMIT;

    @PostConstruct
    public void postConstruct() {
        REVISION_GC_LIMIT = metadataConfig.getRevisionGcLimit();
    }

    @Override
    public void doAppRevisionHeartbeat() {

        try {
            singleFlight.execute("app_revision_heartbeat", () -> {
                Map<AppRevisionQueryModel, AppRevision> heartbeatMap = appRevisionJdbcRepository.getHeartbeatMap();

                Map<AppRevisionQueryModel, InvokeFuture> futureMap = new HashMap<>();
                for (Entry<AppRevisionQueryModel, AppRevision> entry : heartbeatMap.entrySet()) {
                    TaskEvent taskEvent = appRevisionHeartbeatBatchCallable.new TaskEvent(entry.getValue());
                    InvokeFuture future = appRevisionHeartbeatBatchCallable.commit(taskEvent);
                    futureMap.put(entry.getKey(), future);
                }

                for (Entry<AppRevisionQueryModel, InvokeFuture> entry : futureMap.entrySet()) {

                    InvokeFuture future = entry.getValue();
                    try {
                        future.getResponse();
                    } catch (InterruptedException e) {
                        LOG.error(String.format("app_revision: %s heartbeat error.", entry.getKey().getRevision()), e);
                    }
                }
                return null;
            });
        } catch (Exception e) {
            LOG.error("app_revision heartbeat error.", e);
        }

    }

    @Override
    public void doAppRevisionGc(String dataCenter, int silenceHour) {

        try {
            singleFlight.execute("app_revision_gc", () -> {

                List<AppRevision> appRevisions = AppRevisionDomainConvertor.convert2Revisions(appRevisionMapper.queryGcRevision(dataCenter, silenceHour, REVISION_GC_LIMIT));

                if (LOG.isInfoEnabled()) {
                    LOG.info("app_revision tobe gc size: " + appRevisions.size());
                }
                for (AppRevision appRevision : appRevisions) {
                    // delete app_revision
                    appRevisionMapper.deleteAppRevision(dataCenter, appRevision.getRevision());
                }

                return null;
            });
        } catch (Exception e) {
            LOG.error("app_revision gc error.", e);
        }
    }
}