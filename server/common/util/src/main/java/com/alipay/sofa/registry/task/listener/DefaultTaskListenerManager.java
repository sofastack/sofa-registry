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

import java.util.Collection;

import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class DefaultTaskListenerManager implements TaskListenerManager {

    private Multimap<TaskType, TaskListener> taskListeners = ArrayListMultimap.create();

    @Override
    public Multimap<TaskType, TaskListener> getTaskListeners() {
        return taskListeners;
    }

    @Override
    public void addTaskListener(TaskListener taskListener) {
        taskListeners.put(taskListener.support(), taskListener);
    }

    @Override
    public void sendTaskEvent(TaskEvent taskEvent) {
        Collection<TaskListener> taskListeners = this.taskListeners.get(taskEvent.getTaskType());
        for (TaskListener taskListener : taskListeners) {
            taskListener.handleEvent(taskEvent);
        }
    }
}
