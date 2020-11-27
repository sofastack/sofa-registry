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
package com.alipay.sofa.registry.refresh;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public abstract class AbstractRefreshable implements Refreshable {

    protected final Logger   logger    = LoggerFactory.getLogger(getClass());

    private final AtomicLong counter   = new AtomicLong();

    private final AtomicLong timestamp = new AtomicLong();

    @Override
    public void refresh() {
        counter.incrementAndGet();
        timestamp.set(System.currentTimeMillis());
        if (logger.isInfoEnabled()) {
            logger.info("[refresh][times-{}] start", getRefreshCount());
        }
        doRefresh();
        if (logger.isInfoEnabled()) {
            logger.info("[refresh][times-{}] end", getRefreshCount());
        }
    }

    protected abstract void doRefresh();

    @Override
    public long getRefreshCount() {
        return counter.get();
    }

    @Override
    public long getLastUpdateTime() {
        return timestamp.get();
    }
}
