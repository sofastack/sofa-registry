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
import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.ServiceFactory;
import com.alipay.sofa.registry.server.meta.node.DataNodeService;
import com.alipay.sofa.registry.server.meta.node.SessionNodeService;
import com.alipay.sofa.registry.task.listener.TaskEvent;

import java.util.Map;

/**
 * node info change according local or other dataCenter,push change info to local dataCenter node
 *
 * @author shangyu.wh
 * @version $Id: DataNodeChangePushTask.java, v 0.1 2018-01-23 19:05 shangyu.wh Exp $
 */
public class DataNodeChangePushTask extends AbstractMetaServerTask {

    private static final Logger      LOGGER = LoggerFactory.getLogger(DataNodeChangePushTask.class,
                                                "[Task]");
    private final SessionNodeService sessionNodeService;
    private final DataNodeService    dataNodeService;
    final private MetaServerConfig   metaServerConfig;
    final private NodeType           nodeType;
    private NodeChangeResult         nodeChangeResult;
    private Boolean                  confirm;
    private String                   confirmNodeIp;

    private Map<String, DataNode>    targetNodes;

    public DataNodeChangePushTask(NodeType nodeType, MetaServerConfig metaServerConfig) {
        this.metaServerConfig = metaServerConfig;
        this.nodeType = nodeType;
        this.sessionNodeService = (SessionNodeService) ServiceFactory
            .getNodeService(NodeType.SESSION);
        this.dataNodeService = (DataNodeService) ServiceFactory.getNodeService(NodeType.DATA);
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {

        confirm = (Boolean) taskEvent.getAttribute(Constant.PUSH_NEED_CONFIRM_KEY);
        targetNodes = (Map<String, DataNode>) taskEvent
            .getAttribute(Constant.PUSH_TARGET_DATA_NODE);

        confirmNodeIp = (String) taskEvent.getAttribute(Constant.PUSH_TARGET_CONFIRM_NODE);

        Object obj = taskEvent.getEventObj();
        if (obj instanceof NodeChangeResult) {
            nodeChangeResult = (NodeChangeResult) obj;
        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
    }

    @Override
    public void execute() {
        switch (nodeType) {
            case SESSION:
                sessionNodeService.pushDataNodes(nodeChangeResult);
                LOGGER.info("push change to Session Nodes!");
                break;
            case DATA:
                dataNodeService
                    .pushDataNodes(nodeChangeResult, targetNodes, confirm, confirmNodeIp);
                LOGGER.info("push change to Data Nodes!");
                break;
            default:
                break;
        }

    }

    @Override
    public String toString() {
        return "DATA_NODE_CHANGE_PUSH_TASK {" + "taskId='" + taskId + '\'' + ", nodeType='"
               + nodeType + '\'' + ", nodeChangeRequest=" + nodeChangeResult + '}';
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(metaServerConfig.getDataNodeChangePushTaskRetryTimes());
    }
}