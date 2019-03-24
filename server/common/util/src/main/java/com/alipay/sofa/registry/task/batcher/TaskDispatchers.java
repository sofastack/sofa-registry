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
package com.alipay.sofa.registry.task.batcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * See {@link TaskDispatcher} for an overview.
 *
 * @author Tomasz Bak
 *
 * @author shangyu.wh modify
 * @version $Id: TaskDispatchers.java, v 0.1 2017-11-14 16:11 shangyu.wh Exp $
 */
public class TaskDispatchers {

    private static Map<String, TaskDispatcher> taskDispatcherMap   = new ConcurrentHashMap<>();

    private static final String                TASK_DISPATCHER_END = "Dispatcher";

    /**
     * @param id
     * @param taskProcessor
     * @param <ID>
     * @param <T>
     * @return
     */
    public static <ID, T> TaskDispatcher<ID, T> createDefaultSingleTaskDispatcher(String id,
                                                                                  TaskProcessor<T> taskProcessor) {
        final String name = getDispatcherName(id);

        TaskDispatcher<ID, T> taskDispatcher = taskDispatcherMap.computeIfAbsent(name,
                k->{

                    final AcceptorExecutor<ID, T> acceptorExecutor = new AcceptorExecutor<ID, T>(
                            name, 1000, 1000, 100
                    );
                    final TaskExecutors<ID, T> taskExecutor = TaskExecutors.createTaskExecutors(name, 20, taskProcessor,
                            acceptorExecutor);

                    return new TaskDispatcher<ID, T>() {
                        @Override
                        public void dispatch(ID id, T task, long expiryTime) {
                            acceptorExecutor.process(id, task, expiryTime);
                        }

                        @Override
                        public void shutdown() {
                            acceptorExecutor.shutdown();
                            taskExecutor.shutdown();
                        }

                        @Override
                        public AcceptorExecutor<ID, T> getAcceptorExecutor() {
                            return acceptorExecutor;
                        }
                    };
                });
        return taskDispatcher;
    }

    /**
     *
     */
    public static void stopDefaultSingleTaskDispatcher() {
        taskDispatcherMap.forEach((k, v) -> {
            if (v != null) {
                v.shutdown();
            }
        });
    }

    /**
     * @param id
     * @param maxBufferSize
     * @param workerCount
     * @param congestionRetryDelayMs
     * @param networkFailureRetryMs
     * @param taskProcessor
     * @param <ID>
     * @param <T>
     * @return
     */
    public static <ID, T> TaskDispatcher<ID, T> createSingleTaskDispatcher(String id,
                                                                           int maxBufferSize,
                                                                           int workerCount,
                                                                           long congestionRetryDelayMs,
                                                                           long networkFailureRetryMs,
                                                                           TaskProcessor<T> taskProcessor) {

        return taskDispatcherMap.computeIfAbsent(id,k->{

            final AcceptorExecutor<ID, T> acceptorExecutor = new AcceptorExecutor<ID, T>(
                    id, maxBufferSize, congestionRetryDelayMs, networkFailureRetryMs
            );
            final TaskExecutors<ID, T> taskExecutor = TaskExecutors.createTaskExecutors(id, workerCount, taskProcessor,
                    acceptorExecutor);

            return new TaskDispatcher<ID, T>() {
                @Override
                public void dispatch(ID id, T task, long expiryTime) {
                    acceptorExecutor.process(id, task, expiryTime);
                }

                @Override
                public void shutdown() {
                    acceptorExecutor.shutdown();
                    taskExecutor.shutdown();
                }

                @Override
                public AcceptorExecutor<ID, T> getAcceptorExecutor() {
                    return acceptorExecutor;
                }
            };
        });

    }

    public static String getDispatcherName(String name) {
        return name + TASK_DISPATCHER_END;
    }

    /**
     * Getter method for property <tt>taskDispatcherMap</tt>.
     *
     * @return property value of taskDispatcherMap
     */
    public static Map<String, TaskDispatcher> getTaskDispatcherMap() {
        return taskDispatcherMap;
    }
}