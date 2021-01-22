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

import static com.alipay.sofa.registry.task.listener.TaskEvent.TaskType.WATCHER_REGISTER_FETCH_TASK;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class TaskListenerTest {
    private static final TaskListenerManager taskListenerManager                    = new DefaultTaskListenerManager();
    private static volatile boolean          watcherRegisterFetchTaskListenerCalled = false;

    @BeforeClass
    public static void beforeClass() {

        taskListenerManager.addTaskListener(new WatcherRegisterFetchTaskListener());
    }

    @Test
    public void doTest() {
        Assert.assertEquals(1, taskListenerManager.getTaskListeners().size());

        Assert.assertFalse(watcherRegisterFetchTaskListenerCalled);

        taskListenerManager.sendTaskEvent(new TaskEvent(WATCHER_REGISTER_FETCH_TASK));

        Assert.assertTrue(watcherRegisterFetchTaskListenerCalled);
        watcherRegisterFetchTaskListenerCalled = false;
    }

    private static class WatcherRegisterFetchTaskListener implements TaskListener {
        @Override
        public TaskType support() {
            return TaskType.WATCHER_REGISTER_FETCH_TASK;
        }

        @Override
        public void handleEvent(TaskEvent event) {
            watcherRegisterFetchTaskListenerCalled = true;
        }
    }
}
