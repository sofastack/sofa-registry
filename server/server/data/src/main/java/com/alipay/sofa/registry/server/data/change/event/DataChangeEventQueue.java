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
package com.alipay.sofa.registry.server.data.change.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * a queue of DataChangeEvent
 *
 * @author qian.lqlq
 * @version $Id: DataChangeEventQueue.java, v 0.1 2017-12-11 17:10 qian.lqlq Exp $
 */
public class DataChangeEventQueue {

    /**
     *
     */
    private final String                          name;

    /**
     * a block queue that stores all data change events
     */
    private final BlockingQueue<IDataChangeEvent> eventQueue;

    public DataChangeEventQueue(int queueIdx, int queueSize) {
        this.name = String.format("%s_%s", DataChangeEventQueue.class.getSimpleName(), queueIdx);
        if (queueSize <= 0) {
            eventQueue = new LinkedBlockingQueue<>();
        } else {
            eventQueue = new LinkedBlockingQueue<>(queueSize);
        }
    }

    /**
     * receive event when data changed
     *
     * @param event
     */
    public boolean onChange(IDataChangeEvent event) {
        return eventQueue.offer(event);
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    public IDataChangeEvent take() throws InterruptedException {
        return eventQueue.take();
    }
}