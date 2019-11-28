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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
import com.alipay.sofa.registry.server.meta.store.SessionStoreService;
import com.alipay.sofa.registry.server.meta.task.Constant;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionNodeChangePushTask.java, v 0.1 2018-01-15 16:12 shangyu.wh Exp $
 */
public class SessionNodeChangePushTaskMock implements TaskListener {

    private SessionStoreService sessionStoreService;

    private Collection<String>  removeNodes;

    public SessionNodeChangePushTaskMock(SessionStoreService sessionStoreService,
                                         Collection<String> removeNodes) {
        this.sessionStoreService = sessionStoreService;
        this.removeNodes = removeNodes;
    }

    @Override
    public TaskType support() {
        return TaskType.SESSION_NODE_CHANGE_PUSH_TASK;
    }

    @Override
    public void handleEvent(TaskEvent event) {
        Map<String, SessionNode> targetNodes = (Map<String, SessionNode>) event
                .getAttribute(Constant.PUSH_TARGET_SESSION_NODE);

        String ip = (String) event.getAttribute(Constant.PUSH_TARGET_CONFIRM_NODE);
        final ExecutorService threadPool = Executors.newSingleThreadExecutor();
        try {

            threadPool.submit(() -> targetNodes.forEach((address, dataNode) -> {

                if (removeNodes == null || !removeNodes.contains(address)) {
                    sessionStoreService.confirmNodeStatus(address, ip);
                }
            }));
        } finally {
            threadPool.shutdown();
        }
    }
}