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
package com.alipay.sofa.registry.server.data.change;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.MergeResult;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventQueue;
import com.alipay.sofa.registry.server.data.change.notify.IDataChangeNotifier;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * notify sessionserver when data changed
 *
 * @author qian.lqlq
 * @version $Id: DataChangeHandler.java, v 0.1 2017-12-07 18:44 qian.lqlq Exp $
 */
public class DataChangeHandler implements InitializingBean {

    private static final Logger       LOGGER = LoggerFactory.getLogger(DataChangeHandler.class);

    @Autowired
    private DataServerConfig          dataServerBootstrapConfig;

    @Autowired
    private DataChangeEventCenter     dataChangeEventCenter;

    @Resource
    private List<IDataChangeNotifier> dataChangeNotifiers;

    @Override
    public void afterPropertiesSet() {
        //init DataChangeEventCenter
        dataChangeEventCenter.init(dataServerBootstrapConfig);
        start();
    }

    /**
     *
     */
    public void start() {
        DataChangeEventQueue[] queues = dataChangeEventCenter.getQueues();
        int queueCount = queues.length;
        Executor executor = ExecutorFactory.newFixedThreadPool(queueCount,
                DataChangeHandler.class.getSimpleName());
        Executor notifyExecutor = ExecutorFactory.newFixedThreadPool(
                dataServerBootstrapConfig.getQueueCount() * 5, this.getClass().getSimpleName());
        for (int idx = 0; idx < queueCount; idx++) {
            final DataChangeEventQueue dataChangeEventQueue = queues[idx];
            final String name = dataChangeEventQueue.getName();
            LOGGER.info("[DataChangeHandler] begin to notify datum in queue:{}", name);
            executor.execute(() -> {
                while (true) {
                    try {
                        final ChangeData changeData = dataChangeEventQueue.take();
                        notifyExecutor.execute(new ChangeNotifier(changeData, name));
                    } catch (Throwable e) {
                        LOGGER.error("[DataChangeHandler][{}] notify scheduler error", name, e);
                    }
                }
            });
            LOGGER.info("[DataChangeHandler] notify datum in queue:{} success", name);
        }
    }

    /**
     *
     */
    private class ChangeNotifier implements Runnable {

        private ChangeData changeData;

        private String     name;

        /**
         * constructor
         * @param changeData
         * @param name
         */
        public ChangeNotifier(ChangeData changeData, String name) {
            this.changeData = changeData;
            this.name = name;
        }

        @Override
        public void run() {
            Datum datum = changeData.getDatum();
            String dataCenter = datum.getDataCenter();
            String dataInfoId = datum.getDataInfoId();
            long version = datum.getVersion();
            DataSourceTypeEnum sourceType = changeData.getSourceType();
            DataChangeTypeEnum changeType = changeData.getChangeType();
            try {
                if (sourceType == DataSourceTypeEnum.CLEAN) {
                    if (DatumCache.cleanDatum(dataCenter, dataInfoId)) {
                        LOGGER
                            .info(
                                "[DataChangeHandler][{}] clean datum, dataCenter={}, dataInfoId={}, version={},sourceType={}, changeType={}",
                                name, dataCenter, dataInfoId, version, sourceType, changeType);
                    }

                } else {
                    Long lastVersion = null;

                    if (sourceType == DataSourceTypeEnum.PUB_TEMP) {
                        notifyTempPub(datum, sourceType, changeType);
                        return;
                    }

                    MergeResult mergeResult = DatumCache.putDatum(changeType, datum);
                    lastVersion = mergeResult.getLastVersion();

                    if (lastVersion != null
                        && lastVersion.longValue() == DatumCache.ERROR_DATUM_VERSION) {
                        LOGGER
                            .error(
                                "[DataChangeHandler][{}] first put unPub datum into cache error, dataCenter={}, dataInfoId={}, version={}, sourceType={},isContainsUnPub={}",
                                name, dataCenter, dataInfoId, version, sourceType,
                                datum.isContainsUnPub());
                        return;
                    }

                    boolean changeFlag = mergeResult.isChangeFlag();

                    LOGGER
                        .info(
                            "[DataChangeHandler][{}] datum handle,datum={},dataCenter={}, dataInfoId={}, version={}, lastVersion={}, sourceType={}, changeType={},changeFlag={},isContainsUnPub={}",
                            name, datum.hashCode(), dataCenter, dataInfoId, version, lastVersion,
                            sourceType, changeType, changeFlag, datum.isContainsUnPub());
                    //lastVersion null means first add datum
                    if (lastVersion == null || version != lastVersion) {
                        if (changeFlag) {
                            for (IDataChangeNotifier notifier : dataChangeNotifiers) {
                                if (notifier.getSuitableSource().contains(sourceType)) {
                                    notifier.notify(datum, lastVersion);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER
                    .error(
                        "[DataChangeHandler][{}] put datum into cache error, dataCenter={}, dataInfoId={}, version={}, sourceType={},isContainsUnPub={}",
                        name, dataCenter, dataInfoId, version, sourceType, datum.isContainsUnPub(),
                        e);
            }

        }

        private void notifyTempPub(Datum datum, DataSourceTypeEnum sourceType,
                                   DataChangeTypeEnum changeType) {

            String dataCenter = datum.getDataCenter();
            String dataInfoId = datum.getDataInfoId();
            long version = datum.getVersion();

            Datum existDatum = DatumCache.get(dataCenter, dataInfoId);
            if (existDatum != null) {
                Map<String, Publisher> cachePubMap = existDatum.getPubMap();
                if (cachePubMap != null && !cachePubMap.isEmpty()) {
                    datum.getPubMap().putAll(cachePubMap);
                }
            }

            LOGGER
                .info(
                    "[DataChangeHandler][{}] datum handle temp pub,datum={},dataCenter={}, dataInfoId={}, version={}, sourceType={}, changeType={}",
                    name, datum.hashCode(), dataCenter, dataInfoId, version, sourceType, changeType);

            for (IDataChangeNotifier notifier : dataChangeNotifiers) {
                if (notifier.getSuitableSource().contains(sourceType)) {
                    notifier.notify(datum, null);
                }
            }
        }
    }
}
