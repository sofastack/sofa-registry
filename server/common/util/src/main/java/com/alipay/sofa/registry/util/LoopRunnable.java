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
package com.alipay.sofa.registry.util;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-30 16:51 yuzhi.lyz Exp $
 */
public abstract class LoopRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoopRunnable.class);

    public abstract void runUnThrowable();

    public abstract void waitingUnThrowable();

    public void unexpectExit(Throwable e) {
        LOGGER.error("expect exit in LoopRunnable {}", Thread.currentThread().getName(), e);
    }

    public void run() {
        try {
            for (;;) {
                try {
                    runUnThrowable();
                } catch (Throwable ignored) {
                    // ignored that
                }
                try {
                    waitingUnThrowable();
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable e) {
            // in oom, this may be happen
            unexpectExit(e);
        }

    }
}