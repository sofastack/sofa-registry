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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.server.session.listener.PushTaskSender;
import com.alipay.sofa.registry.server.session.strategy.TaskMergeProcessorStrategy;
import com.alipay.sofa.registry.task.listener.TaskEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author shangyu.wh
 * @version $Id: TaskMergeUtil.java, v 0.1 2018-12-10 18:35 shangyu.wh Exp $
 */
public class DefaultPushTaskMergeProcessor<T extends PushTaskSender> implements
                                                                     TaskMergeProcessorStrategy<T> {

    private T pushTaskSender;

    @Override
    public void init(T pushTaskSender) {
        this.pushTaskSender = pushTaskSender;
    }

    @Override
    public void handleEvent(TaskEvent event) {
        pushTaskSender.executePushAsync(event);
    }

    @Override
    public AtomicInteger getPutTaskSize() {
        return null;
    }

    @Override
    public AtomicInteger getOverrideTaskSize() {
        return null;
    }

    @Override
    public AtomicInteger getSendTaskSize() {
        return null;
    }

    @Override
    public Integer getPendingTaskSize() {
        return null;
    }

}