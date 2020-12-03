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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.event.*;
import com.alipay.sofa.registry.server.data.change.notify.IDataChangeNotifier;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * notify sessionserver when data changed
 *
 * @author qian.lqlq
 * @version $Id: DataChangeHandler.java, v 0.1 2017-12-07 18:44 qian.lqlq Exp $
 */
public class DataChangeHandler {

    private static final Logger       LOGGER       = LoggerFactory
                                                       .getLogger(DataChangeHandler.class);

    private static final Logger       LOGGER_START = LoggerFactory.getLogger("DATA-START-LOGS");

    @Autowired
    private DataServerConfig          dataServerConfig;

    @Autowired
    private DataChangeEventCenter     dataChangeEventCenter;

    @Autowired
    private DatumCache                datumCache;

    @Resource
    private List<IDataChangeNotifier> dataChangeNotifiers;

    @PostConstruct
    public void start() {
        DataChangeEventQueue[] queues = dataChangeEventCenter.getQueues();
        int queueCount = queues.length;
        Executor executor = ExecutorFactory.newFixedThreadPool(queueCount, DataChangeHandler.class.getSimpleName());
        Executor notifyExecutor = ExecutorFactory
                .newFixedThreadPool(dataServerConfig.getQueueCount() * 5, this.getClass().getSimpleName());
        for (int idx = 0; idx < queueCount; idx++) {
            final DataChangeEventQueue dataChangeEventQueue = queues[idx];
            final String name = dataChangeEventQueue.getName();
            executor.execute(() -> {
                while (true) {
                    try {
                        final IDataChangeEvent event = dataChangeEventQueue.take();
                        notifyExecutor.execute(new ChangeNotifier(event, name));
                    } catch (Throwable e) {
                        LOGGER.error("[DataChangeHandler][{}] notify scheduler error", name, e);
                    }
                }
            });
            LOGGER_START.info("[DataChangeHandler] notify datum in queue:{} success", name);
        }
    }

    /**
     *
     */
    private class ChangeNotifier implements Runnable {

        private IDataChangeEvent event;

        private String           name;

        public ChangeNotifier(IDataChangeEvent event, String name) {
            this.event = event;
            this.name = name;
        }

        @Override
        public void run() {
            final DataSourceTypeEnum sourceType = event.getSourceType();
            try {
                switch (sourceType) {
                    case PUB_TEMP:
                        DataTempChangeEvent temp = (DataTempChangeEvent) event;
                        notifyTempPub(temp.getDatum());
                        break;

                    case PUB:
                        DataChangeEvent pub = (DataChangeEvent) event;
                        notify(pub.getDataCenter(), pub.getDataInfoId());
                        break;

                    default:
                        throw new IllegalArgumentException("unknow SourceType of event:"
                                                           + event.getSourceType());

                }
            } catch (Throwable e) {
                LOGGER.error("[DataChangeHandler][{}] failed to notify, event={}", name, event, e);
            }
        }

        private void notifyTempPub(Datum datum) {
            Datum existDatum = datumCache.get(datum.getDataCenter(), datum.getDataInfoId());
            if (existDatum != null) {
                datum.addPublishers(existDatum.getPubMap());
            }

            LOGGER.info("[DataChangeHandler][{}] temp pub, {}, dataCenter={}, size={}, ver={}",
                name, datum.getDataInfoId(), datum.getDataCenter(), datum.publisherSize(),
                datum.getVersion());
            notify(datum, null, DataSourceTypeEnum.PUB_TEMP);
        }

        private void notify(String dataCenter, String dataInfoId) {
            final Datum datum = datumCache.get(dataCenter, dataInfoId);
            if (datum != null) {
                notify(datum, datum.getVersion(), DataSourceTypeEnum.PUB);
            }
        }

        private void notify(Datum datum, Long version, DataSourceTypeEnum sourceType) {
            for (IDataChangeNotifier notifier : dataChangeNotifiers) {
                if (notifier.getSuitableSource().contains(sourceType)) {
                    notifier.notify(datum, version);
                }
            }
        }

    }

}
