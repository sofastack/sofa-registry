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
package com.alipay.sofa.registry.server.data.remoting.dataserver.task;

import com.alipay.sofa.registry.server.data.cache.DataServerChangeItem;
import com.alipay.sofa.registry.server.data.event.DataServerChangeEvent;
import com.alipay.sofa.registry.server.data.event.DataServerChangeEvent.FromType;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.event.StartTaskTypeEnum;
import com.alipay.sofa.registry.server.data.remoting.metaserver.IMetaServerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author qian.lqlq
 * @version $Id: ConnectionRefreshTask.java, v 0.1 2018-03-12 21:10 qian.lqlq Exp $
 */
public class ConnectionRefreshTask extends AbstractTask {

    @Autowired
    private IMetaServerService metaServerService;

    @Autowired
    private EventCenter        eventCenter;

    @Override
    public void handle() {
        DataServerChangeItem dataServerChangeItem = metaServerService.getDateServers();
        if (dataServerChangeItem != null) {
            eventCenter
                .post(new DataServerChangeEvent(dataServerChangeItem, FromType.CONNECT_TASK));
        }
    }

    @Override
    public int getDelay() {
        return 30;
    }

    @Override
    public int getInitialDelay() {
        return 0;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public StartTaskTypeEnum getStartTaskTypeEnum() {
        return StartTaskTypeEnum.CONNECT_DATA;
    }

}