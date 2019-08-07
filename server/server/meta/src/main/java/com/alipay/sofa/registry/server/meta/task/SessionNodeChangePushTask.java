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
package com.alipay.sofa.registry.server.meta.task;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.SessionNodeService;
import com.alipay.sofa.registry.task.listener.TaskEvent;

import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionNodeChangePushTask.java, v 0.1 2018-01-15 16:12 shangyu.wh Exp $
 */
public class SessionNodeChangePushTask extends AbstractMetaServerTask {

    private final SessionNodeService sessionNodeService;
    final private MetaServerConfig   metaServerConfig;
    private NodeChangeResult         nodeChangeResult;
    private Map<String, SessionNode> targetNodes;
    private String                   confirmNodeIp;

    public SessionNodeChangePushTask(MetaServerConfig metaServerConfig) {
        this.metaServerConfig = metaServerConfig;
        this.sessionNodeService = (SessionNodeService) ServiceFactory
            .getNodeService(NodeType.SESSION);
    }

    @Override
    public void execute() {
        sessionNodeService.pushSessions(nodeChangeResult, targetNodes, confirmNodeIp);
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        targetNodes = (Map<String, SessionNode>) taskEvent
            .getAttribute(Constant.PUSH_TARGET_SESSION_NODE);

        confirmNodeIp = (String) taskEvent.getAttribute(Constant.PUSH_TARGET_CONFIRM_NODE);
        Object obj = taskEvent.getEventObj();
        if (obj instanceof NodeChangeResult) {
            this.nodeChangeResult = (NodeChangeResult) obj;
        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
    }

    @Override
    public String toString() {
        return "SESSION_NODE_CHANGE_PUSH_TASK{" + "taskId='" + taskId + '\''
               + ", nodeChangeResult=" + nodeChangeResult + '}';
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(metaServerConfig.getSessionNodeChangePushTaskRetryTimes());
    }
}