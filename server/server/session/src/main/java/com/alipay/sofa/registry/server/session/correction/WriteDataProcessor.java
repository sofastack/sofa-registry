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
package com.alipay.sofa.registry.server.session.correction;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author shangyu.wh
 * @version 1.0: WriteDataProcessor.java, v 0.1 2019-06-06 12:50 shangyu.wh Exp $
 */
public class WriteDataProcessor {

    private static final Logger             LOGGER              = LoggerFactory
                                                                    .getLogger(WriteDataAcceptorImpl.class);

    private long                            beginTimestamp;

    private AtomicLong                      lastUpdateTimestamp = new AtomicLong(0);

    private AtomicBoolean                   writeDataLock       = new AtomicBoolean(false);

    private BlockingDeque<WriteDataRequest> acceptorQueue       = new LinkedBlockingDeque();

    private final String                    connectId;

    public WriteDataProcessor(String connectId) {
        this.connectId = connectId;
        this.beginTimestamp = System.currentTimeMillis();
        this.lastUpdateTimestamp.set(beginTimestamp);
    }

    public void refreashUpdateTime() {
        lastUpdateTimestamp.set(System.currentTimeMillis());
    }

    public boolean checkLastUpdateTimes(long duration) {
        final long age = System.currentTimeMillis() - lastUpdateTimestamp.get();
        return age > duration;
    }

    public long getLastUpdateTimestap() {
        return lastUpdateTimestamp.get();
    }

    public boolean halt() {
        return writeDataLock.compareAndSet(false, true);
    }

    public void resume() {
        writeDataLock.compareAndSet(true, false);
    }

    public void process(WriteDataRequest request) {

        if (writeDataLock.get()) {
            acceptorQueue.add(request);
        } else {
            while (!acceptorQueue.isEmpty()) {
                WriteDataRequest writeDataRequest = acceptorQueue.poll();
                doHandle(writeDataRequest);
            }
            doHandle(request);
        }

    }

    private void doHandle(WriteDataRequest request) {
        switch (request.getRequestType()) {
            case PUBLISHER:
                break;
            case CLIENT_OFF:
                break;
            case UN_PUBLISHER:
                break;
            default:
                LOGGER.warn("request type, {}", request);

        }
    }
}