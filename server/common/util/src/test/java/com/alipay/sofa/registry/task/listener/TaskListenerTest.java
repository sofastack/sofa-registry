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
package com.alipay.sofa.registry.task.listener;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.alipay.sofa.registry.task.listener.TaskEvent.TaskType.CANCEL_DATA_TASK;
import static com.alipay.sofa.registry.task.listener.TaskEvent.TaskType.DATA_PUSH_TASK;
import static com.alipay.sofa.registry.task.listener.TaskEvent.TaskType.WATCHER_REGISTER_FETCH_TASK;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class TaskListenerTest {
    private static final TaskListenerManager taskListenerManager                    = new DefaultTaskListenerManager();
    private static volatile boolean          watcherRegisterFetchTaskListenerCalled = false;
    private static volatile boolean          cancelDataTaskListenerCalled           = false;
    private static volatile boolean          dataPushTaskListenerCalled             = false;

    @BeforeClass
    public static void beforeClass() {
        taskListenerManager.addTaskListener(new CancelDataTaskListener());
        taskListenerManager.addTaskListener(new DataPushTaskListener());
        taskListenerManager.addTaskListener(new WatcherRegisterFetchTaskListener());
    }

    @Test
    public void doTest() {
        Assert.assertEquals(3, taskListenerManager.getTaskListeners().size());
        taskListenerManager.sendTaskEvent(new TaskEvent(DATA_PUSH_TASK));
        Assert.assertTrue(dataPushTaskListenerCalled);
        Assert.assertFalse(cancelDataTaskListenerCalled);
        Assert.assertFalse(watcherRegisterFetchTaskListenerCalled);
        dataPushTaskListenerCalled = false;

        taskListenerManager.sendTaskEvent(new TaskEvent(CANCEL_DATA_TASK));
        Assert.assertFalse(dataPushTaskListenerCalled);
        Assert.assertTrue(cancelDataTaskListenerCalled);
        Assert.assertFalse(watcherRegisterFetchTaskListenerCalled);
        cancelDataTaskListenerCalled = false;

        taskListenerManager.sendTaskEvent(new TaskEvent(WATCHER_REGISTER_FETCH_TASK));
        Assert.assertFalse(dataPushTaskListenerCalled);
        Assert.assertFalse(cancelDataTaskListenerCalled);
        Assert.assertTrue(watcherRegisterFetchTaskListenerCalled);
        watcherRegisterFetchTaskListenerCalled = false;
    }

    private static class DataPushTaskListener implements TaskListener {
        @Override
        public boolean support(TaskEvent event) {
            return DATA_PUSH_TASK.equals(event.getTaskType());
        }

        @Override
        public void handleEvent(TaskEvent event) {
            dataPushTaskListenerCalled = true;
        }
    }

    private static class CancelDataTaskListener implements TaskListener {
        @Override
        public boolean support(TaskEvent event) {
            return TaskEvent.TaskType.CANCEL_DATA_TASK.equals(event.getTaskType());
        }

        @Override
        public void handleEvent(TaskEvent event) {
            cancelDataTaskListenerCalled = true;
        }
    }

    private static class WatcherRegisterFetchTaskListener implements TaskListener {
        @Override
        public boolean support(TaskEvent event) {
            return TaskEvent.TaskType.WATCHER_REGISTER_FETCH_TASK.equals(event.getTaskType());
        }

        @Override
        public void handleEvent(TaskEvent event) {
            watcherRegisterFetchTaskListenerCalled = true;
        }
    }
}
