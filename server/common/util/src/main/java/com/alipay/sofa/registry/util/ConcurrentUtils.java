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
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
public final class ConcurrentUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentUtils.class);

    private ConcurrentUtils() {
    }

    public static abstract class SafeParaLoop<T> {

        private static final Logger logger = LoggerFactory.getLogger(SafeParaLoop.class);

        private List<T>             list;

        private Executor            executors;

        public SafeParaLoop(Collection<T> list) {
            this(MoreExecutors.directExecutor(), list);
        }

        public SafeParaLoop(Executor executors, Collection<T> src) {
            this.executors = executors;
            this.list = src == null ? null : Lists.newLinkedList(src);
        }

        public void run() {
            if (list == null) {
                return;
            }
            for (T t : list) {
                executors.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doRun0(t);
                        } catch (Exception e) {
                            logger.error("[SafeParaLoop][{}]", getInfo(t), e);
                        }
                    }
                });

            }
        }

        protected abstract void doRun0(T t) throws Exception;

        String getInfo(T t) {
            return t.toString();
        }
    }

    public static Thread createDaemonThread(String name, Runnable r) {
        Thread t = new Thread(r, name);
        t.setDaemon(true);
        return t;
    }

    public static void objectWaitUninterruptibly(Object o, int timeoutMs) {
        try {
            o.wait(timeoutMs);
        } catch (InterruptedException ignored) {
            LOGGER.warn("Interrupted waiting", ignored);
        }
    }
}
