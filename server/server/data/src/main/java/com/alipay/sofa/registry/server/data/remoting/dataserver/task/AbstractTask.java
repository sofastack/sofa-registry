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
package com.alipay.sofa.registry.server.data.remoting.dataserver.task;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.event.StartTaskTypeEnum;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author qian.lqlq
 * @version $Id: AbstractTask.java, v 0.1 2018-03-07 21:24 qian.lqlq Exp $
 */
public abstract class AbstractTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

    /**
     * get name of task
     *
     * @return
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (Throwable e) {
            LOGGER.error("[{}] task execute failed", getName(), e);
        }
    }

    /**
     * biz handle
     */
    public abstract void handle();

    /**
     * get delay of task
     *
     * @return
     */
    public abstract int getDelay();

    /**
     * get initial delay of task
     *
     * @return
     */
    public abstract int getInitialDelay();

    /**
     * get unit of delay time or initial delay time
     *
     * @return
     */
    public abstract TimeUnit getTimeUnit();

    /**
     * get type to match post
     * @return
     */
    public abstract StartTaskTypeEnum getStartTaskTypeEnum();
}