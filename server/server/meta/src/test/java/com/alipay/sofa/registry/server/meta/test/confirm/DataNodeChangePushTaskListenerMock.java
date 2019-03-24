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
package com.alipay.sofa.registry.server.meta.test.confirm;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.server.meta.store.DataStoreService;
import com.alipay.sofa.registry.server.meta.task.Constant;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataNodeChangePushTaskListenerMoke.java, v 0.1 2018-03-28 16:20 shangyu.wh Exp $
 */
public class DataNodeChangePushTaskListenerMock implements TaskListener {

    private DataStoreService   dataStoreService;

    private Collection<String> removeNodes;

    public DataNodeChangePushTaskListenerMock(DataStoreService dataStoreService,
                                              Collection<String> removeNodes) {
        this.dataStoreService = dataStoreService;
        this.removeNodes = removeNodes;
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.DATA_NODE_CHANGE_PUSH_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {

        NodeType nodeType = (NodeType) event.getAttribute(Constant.PUSH_TARGET_TYPE);
        String ip = (String) event.getAttribute(Constant.PUSH_TARGET_CONFIRM_NODE);

        Map<String, DataNode> targetNodes = (Map<String, DataNode>) event
                .getAttribute(Constant.PUSH_TARGET_DATA_NODE);
        switch (nodeType) {
            case SESSION:
                break;
            case DATA:
                final ExecutorService threadPool = Executors.newSingleThreadExecutor();
                try {
                    threadPool.submit(() -> targetNodes.forEach((address, dataNode) -> {
                        if (removeNodes == null || !removeNodes.contains(address)) {
                            dataStoreService.confirmNodeStatus(address, ip);
                        }
                    }));
                    break;
                } finally {
                    threadPool.shutdown();
                }
        }
    }
}