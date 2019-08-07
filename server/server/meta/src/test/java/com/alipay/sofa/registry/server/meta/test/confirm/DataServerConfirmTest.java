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

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig.DecisionMode;
import com.alipay.sofa.registry.server.meta.bootstrap.NodeConfig;
import com.alipay.sofa.registry.server.meta.repository.service.DataConfirmStatusService;
import com.alipay.sofa.registry.server.meta.repository.service.DataRepositoryService;
import com.alipay.sofa.registry.server.meta.store.DataStoreService;
import com.alipay.sofa.registry.task.listener.DefaultTaskListenerManager;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.task.scheduler.TimedSupervisorTask;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataServerConfirmTest.java, v 0.1 2018-03-27 17:13 shangyu.wh Exp $
 */
public class DataServerConfirmTest {

    @Test
    public void testDataServerRegisterConfirm() throws InterruptedException {

        DataStoreService dataStoreService = new DataStoreService();

        DataConfirmStatusService dataConfirmStatusService = new DataConfirmStatusService();

        NodeConfig nodeConfig = mock(NodeConfig.class);
        MetaServerConfig metaServerConfig = mock(MetaServerConfig.class);
        TaskListenerManager taskListenerManager = new DefaultTaskListenerManager();
        taskListenerManager
                .addTaskListener(new DataNodeChangePushTaskListenerMock(dataStoreService, null));
        taskListenerManager.addTaskListener(new ReceiveStatusConfirmNotifyTaskMock());

        dataStoreService.setNodeConfig(nodeConfig);
        dataStoreService.setTaskListenerManager(taskListenerManager);
        dataStoreService.setDataConfirmStatusService(dataConfirmStatusService);

        DataRepositoryService dataRepositoryService = new DataRepositoryService();
        dataRepositoryService.setNodeConfig(nodeConfig);
        dataStoreService.setDataRepositoryService(dataRepositoryService);

        when(nodeConfig.getLocalDataCenter()).thenReturn("DefaultDataCenter");
        when(metaServerConfig.getDecisionMode()).thenReturn(DecisionMode.RUNTIME);

        //dataStoreService.pushDataNodeListChange();
        start(dataStoreService);

        int numThreads = 10;
        final CountDownLatch allDone = new CountDownLatch(numThreads);
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int n = i;
            threadPool.execute(() -> dataStoreService.addNode(new NodeTest(new URL("192.168.0." + n, 0), allDone)));
        }

        assertTrue(" timeout! More than" + 60 + "seconds,allDone:" + allDone.getCount(),
                allDone.await(60, TimeUnit.SECONDS));
    }

    @Test
    public void testDataServerRegisterConfirmWithRemove() throws InterruptedException {

        DataStoreService dataStoreService = new DataStoreService();
        DataConfirmStatusService dataConfirmStatusService = new DataConfirmStatusService();
        NodeConfig nodeConfig = mock(NodeConfig.class);
        MetaServerConfig metaServerConfig = mock(MetaServerConfig.class);
        TaskListenerManager taskListenerManager = new DefaultTaskListenerManager();

        Collection<String> removeNodes = new CopyOnWriteArrayList<>();

        removeNodes.add("192.168.0.4");
        removeNodes.add("192.168.0.5");
        removeNodes.add("192.168.0.17");
        removeNodes.add("192.168.0.21");
        removeNodes.add("192.168.0.10");
        removeNodes.add("192.168.0.24");

        taskListenerManager
                .addTaskListener(new DataNodeChangePushTaskListenerMock(dataStoreService, removeNodes));
        taskListenerManager.addTaskListener(new ReceiveStatusConfirmNotifyTaskMock());

        dataStoreService.setNodeConfig(nodeConfig);
        dataStoreService.setTaskListenerManager(taskListenerManager);
        dataStoreService.setDataConfirmStatusService(dataConfirmStatusService);

        DataRepositoryService dataRepositoryService = new DataRepositoryService();
        dataRepositoryService.setNodeConfig(nodeConfig);
        dataStoreService.setDataRepositoryService(dataRepositoryService);

        when(nodeConfig.getLocalDataCenter()).thenReturn("DefaultDataCenter");
        when(metaServerConfig.getDecisionMode()).thenReturn(DecisionMode.RUNTIME);

        //dataStoreService.pushDataNodeListChange();
        start(dataStoreService);
        int numThreads = 10;
        //done number = node
        final CountDownLatch allDone = new CountDownLatch(numThreads);
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {

            for (int i = 0; i < numThreads; i++) {
                final int n = i;
                threadPool.execute(() -> dataStoreService.addNode(new NodeTest(new URL("192.168.0." + n, 0), allDone)));
            }
            TimeUnit.MILLISECONDS.sleep(2000);
            removeNodes.forEach(dataStoreService::removeNode);

            assertTrue(" timeout! More than" + 60 + "seconds, allDone:" + allDone.getCount(),
                    allDone.await(60, TimeUnit.SECONDS));
        } finally {
            threadPool.shutdown();
        }
    }

    private void start(DataStoreService dataStoreService) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
        ThreadPoolExecutor checkDataNodeListChangeExecutor = new ThreadPoolExecutor(1, 2,
                0, TimeUnit.SECONDS, new SynchronousQueue<>());

        scheduler.schedule(new TimedSupervisorTask("CheckDataNodeListChange", scheduler,
                checkDataNodeListChangeExecutor, 500, TimeUnit.MILLISECONDS, 3,
                dataStoreService::pushNodeListChange), 1, TimeUnit.SECONDS);
    }

    class NodeTest extends DataNode {
        private final CountDownLatch allDone;

        public NodeTest(URL nodeUrl, CountDownLatch allDone) {
            super(nodeUrl, "DefaultDataCenter");
            this.allDone = allDone;
        }

        /**
         * Getter method for property <tt>allDone</tt>.
         *
         * @return property value of allDone
         */
        public CountDownLatch getAllDone() {
            return allDone;
        }
    }
}