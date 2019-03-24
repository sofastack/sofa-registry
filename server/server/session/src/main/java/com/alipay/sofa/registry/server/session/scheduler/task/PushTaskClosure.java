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
package com.alipay.sofa.registry.server.session.scheduler.task;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.task.Task;
import com.alipay.sofa.registry.task.TaskClosure;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: PushTaskClosure.java, v 0.1 2018-06-04 17:13 shangyu.wh Exp $
 */
public class PushTaskClosure implements TaskClosure {

    private final static Logger                         LOGGER          = LoggerFactory
                                                                            .getLogger(PushTaskClosure.class);

    private ConcurrentHashMap<String, Task>             taskMap         = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ProcessingResult> taskResultMap   = new ConcurrentHashMap<>();

    private TaskClosure                                 taskClosure;

    private final BlockingQueue<String>                 completionQueue = new LinkedBlockingQueue<>();

    private final ExecutorService                       pushTaskClosureExecutor;

    public PushTaskClosure(ExecutorService pushTaskClosureExecutor) {
        this.pushTaskClosureExecutor = pushTaskClosureExecutor;
    }

    @Override
    public void run(ProcessingResult processingResult, Task task) {
        if (task != null) {
            ProcessingResult existed = taskResultMap
                .putIfAbsent(task.getTaskId(), processingResult);
            if (existed == null) {
                completionQueue.add(task.getTaskId());
            }
        }
    }

    public void addTask(Task task) {
        taskMap.putIfAbsent(task.getTaskId(), task);
    }

    public void start() {
        pushTaskClosureExecutor.execute(() -> {
            try {
                int size = taskMap.size();
                LOGGER.info("Push task queue size {},map size {}", completionQueue.size(), size);
                for (int i = 0; i < size; i++) {
                    String taskId = completionQueue.poll(6000, TimeUnit.MILLISECONDS);
                    if(taskId != null) {
                        ProcessingResult result = taskResultMap.get(taskId);
                        if (result == ProcessingResult.Success) {
                            taskMap.remove(taskId);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.error("Push task check InterruptedException!", e);
            }

            if (taskMap.isEmpty()) {
                LOGGER.info("Push all tasks success");
                if (taskClosure != null) {
                    taskClosure.run(ProcessingResult.Success, null);
                }

            } else {
                LOGGER.warn("Push tasks found error tasks {} !", taskMap);
                if (taskClosure != null) {
                    taskClosure.run(ProcessingResult.PermanentError, null);
                }
            }
        });
    }

    /**
     * Getter method for property <tt>taskMap</tt>.
     *
     * @return property value of taskMap
     */
    public Map<String, Task> getTaskMap() {
        return taskMap;
    }

    /**
     * Setter method for property <tt>taskClosure</tt>.
     *
     * @param taskClosure  value to be assigned to property taskClosure
     */
    public void setTaskClosure(TaskClosure taskClosure) {
        this.taskClosure = taskClosure;
    }
}