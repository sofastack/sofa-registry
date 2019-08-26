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
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.task.Task;
import com.alipay.sofa.registry.task.TaskClosure;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;
import com.alipay.sofa.registry.task.listener.TaskEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: PushTaskClosure.java, v 0.1 2018-06-04 17:13 shangyu.wh Exp $
 */
public class PushTaskClosure implements TaskClosure {

    private final static Logger                         LOGGER        = LoggerFactory
                                                                          .getLogger(PushTaskClosure.class);

    private Set<String>                                 tasks         = ConcurrentHashMap
                                                                          .newKeySet();

    private ConcurrentHashMap<String, ProcessingResult> taskResultMap = new ConcurrentHashMap<>();

    private TaskClosure                                 taskClosure;

    private final ExecutorService                       pushTaskClosureExecutor;

    private CountDownLatch                              latch;

    private SessionServerConfig sessionServerConfig;

    public PushTaskClosure(ExecutorService pushTaskClosureExecutor, SessionServerConfig sessionServerConfig) {
        this.pushTaskClosureExecutor = pushTaskClosureExecutor;
        this.sessionServerConfig = sessionServerConfig;
    }

    @Override
    public void run(ProcessingResult processingResult, Task task) {
        if (task != null) {
            ProcessingResult result = taskResultMap.putIfAbsent(task.getTaskId(), processingResult);
            if (result == null) {
                latch.countDown();
            }
        }
    }

    public void addTask(TaskEvent taskEvent) {
        tasks.add(taskEvent.getTaskId());
    }

    public void start() {
        pushTaskClosureExecutor.execute(() -> {
            int size = tasks.size();
            latch = new CountDownLatch(size);
            LOGGER.info("Push task result wait,all task size {}",  size);

            try {
                if (!latch.await(sessionServerConfig.getPushTaskConfirmWaitTimeout(), TimeUnit.MILLISECONDS)) {
                    LOGGER.warn("wait Push task result timeout,result size {},all task size {}",taskResultMap.size(),size);
                    if (taskClosure != null) {
                        taskClosure.run(ProcessingResult.PermanentError, null);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error when wait result!", e);
            }

            LOGGER.info("Push all tasks success");
            if (taskClosure != null) {
                taskClosure.run(ProcessingResult.Success, null);
            }

        });
    }

    /**
     * Getter method for property <tt>tasks</tt>.
     *
     * @return property value of tasks
     */
    public Set<String> getTasks() {
        return tasks;
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