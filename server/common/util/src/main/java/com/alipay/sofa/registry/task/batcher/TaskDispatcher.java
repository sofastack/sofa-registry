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

/**
 * Task dispatcher takes task from clients, and delegates their execution to a configurable number of workers.
 * The task can be processed one at a time or in batches. Only non-expired tasks are executed, and if a newer
 * task with the same id is scheduled for execution, the old one is deleted. Lazy dispatch of work (only on demand)
 * to workers, guarantees that data are always up to date, and no stale task processing takes place.
 * <h3>Task processor</h3>
 * A client of this component must provide an implementation of {@link TaskProcessor} interface, which will do
 * the actual work of task processing. This implementation must be thread safe, as it is called concurrently by
 * multiple threads.
 *
 * @author Tomasz Bak
 *
 * @author shangyu.wh modify
 * @version $Id: TaskDispatcher.java, v 0.1 2017-11-14 15:23 shangyu.wh Exp $
 */
public interface TaskDispatcher<ID, T> {

    /**
     *
     * @param id
     * @param task
     * @param expiryTime
     */
    void dispatch(ID id, T task, long expiryTime);

    /**
     *
     */
    void shutdown();

    AcceptorExecutor<ID, T> getAcceptorExecutor();
}