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
package com.alipay.sofa.registry.jdbc.repository.batch;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionQueryModel;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.BatchCallableRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AppRevisionBatchQueryCallable.java, v 0.1 2021年01月24日 14:01 xiaojian.xj Exp $
 */
public class AppRevisionBatchQueryCallable extends
                                          BatchCallableRunnable<AppRevisionQueryModel, AppRevision> {

    private static final Logger LOG = LoggerFactory.getLogger("METADATA-EXCHANGE",
                                        "[AppRevisionBatchQuery]");
    @Autowired
    private AppRevisionMapper   appRevisionMapper;

    /**
     * batch query app_revision
     * @param taskEvents
     * @return
     */
    @Override
    public boolean batchProcess(List<TaskEvent> taskEvents) {

        if (CollectionUtils.isEmpty(taskEvents)) {
            return true;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("commit app_revision query, task size: " + taskEvents.size());
        }
        List<AppRevisionQueryModel> querys = taskEvents.stream().map(task -> task.getData()).collect(Collectors.toList());
        List<AppRevisionDomain> domains = appRevisionMapper.batchQuery(querys);

        Map<String, AppRevision> queryResult = new HashMap<>();
        domains.forEach(domain -> {
            AppRevision appRevision = queryResult.get(domain.getRevision());
            if (appRevision != null) {
                return;
            }
            queryResult.putIfAbsent(domain.getRevision(), AppRevisionDomainConvertor.convert2Revision(domain));
        });

        taskEvents.forEach(taskEvent -> {
            String revision = taskEvent.getData().getRevision();
            InvokeFuture<AppRevision> future = taskEvent.getFuture();
            future.putResponse(queryResult.get(revision));
        });
        return true;
    }

    @Override
    protected void setBatchSize() {
        this.batchSize = 200;
    }

    @Override
    protected void setTimeUnit() {
        this.timeUnit = TimeUnit.MILLISECONDS;
    }

    @Override
    protected void setSleep() {
        this.sleep = 20;
    }
}