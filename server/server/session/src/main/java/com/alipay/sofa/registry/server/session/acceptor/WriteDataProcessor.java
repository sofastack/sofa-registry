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
package com.alipay.sofa.registry.server.session.acceptor;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataRequest.WriteDataRequestType;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.renew.RenewService;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.google.common.collect.Lists;

/**
 *
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version 1.0: WriteDataProcessor.java, v 0.1 2019-06-06 12:50 shangyu.wh Exp $
 */
public class WriteDataProcessor {

    private static final Logger                     LOGGER              = LoggerFactory
                                                                            .getLogger(WriteDataProcessor.class);

    private static final Logger                     RENEW_LOGGER        = LoggerFactory
                                                                            .getLogger(
                                                                                ValueConstants.LOGGER_NAME_RENEW,
                                                                                "[WriteDataProcessor]");

    private final TaskListenerManager               taskListenerManager;

    private final SessionServerConfig               sessionServerConfig;

    private final RenewService                      renewService;

    private final String                            connectId;

    private long                                    beginTimestamp;

    private AtomicLong                              lastUpdateTimestamp = new AtomicLong(0);

    private AtomicBoolean                           writeDataLock       = new AtomicBoolean(false);

    private ConcurrentLinkedQueue<WriteDataRequest> acceptorQueue       = new ConcurrentLinkedQueue();

    public WriteDataProcessor(String connectId, TaskListenerManager taskListenerManager,
                              SessionServerConfig sessionServerConfig, RenewService renewService) {
        this.connectId = connectId;
        this.taskListenerManager = taskListenerManager;
        this.sessionServerConfig = sessionServerConfig;
        this.renewService = renewService;

        this.beginTimestamp = System.currentTimeMillis();
        this.lastUpdateTimestamp.set(beginTimestamp);
    }

    private boolean halt() {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("halt: connectId={}", connectId);
        }
        return writeDataLock.compareAndSet(false, true);
    }

    public void resume() {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("resume: connectId={}", connectId);
        }
        flushQueue();
        writeDataLock.compareAndSet(true, false);
        flushQueue();
    }

    public void process(WriteDataRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("process: connectId={}, requestType={}, requestBody={}", connectId,
                request.getRequestType(), request.getRequestBody());
        }
        if (request.getRequestType() == WriteDataRequestType.DATUM_SNAPSHOT) {
            doHandle(request);
        } else {
            if (writeDataLock.get()) {
                acceptorQueue.add(request);
            } else {
                flushQueue();
                doHandle(request);
            }
        }

        if (isWriteRequest(request)) {
            refreshUpdateTime();
        }

    }

    /**
     *
     * @param request
     * @return
     */
    private boolean isWriteRequest(WriteDataRequest request) {
        return request.getRequestType() != WriteDataRequestType.RENEW_DATUM;
    }

    /**
     * Ensure that the queue data is sent out
     */
    private void flushQueue() {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("flushQueue: connectId={}", connectId);
        }

        while (!acceptorQueue.isEmpty()) {
            WriteDataRequest writeDataRequest = acceptorQueue.poll();
            if (writeDataRequest == null) {
                break;
            }
            doHandle(writeDataRequest);
        }
    }

    private void doHandle(WriteDataRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("doHandle: connectId={}, requestType={}, requestBody={}", connectId,
                request.getRequestType(), request.getRequestBody());
        }

        switch (request.getRequestType()) {
            case PUBLISHER: {
                doPublishAsync(request);
            }
                break;
            case UN_PUBLISHER: {
                doUnPublishAsync(request);
            }
                break;
            case CLIENT_OFF: {
                doClientOffAsync(request);
            }
                break;
            case RENEW_DATUM: {
                if (renewAndSnapshotInSilence()) {
                    return;
                }
                doRenewAsync(request);
            }
                break;
            case DATUM_SNAPSHOT: {
                if (renewAndSnapshotInSilence()) {
                    return;
                }
                halt();
                doSnapshotAsync(request);
                resume();
            }
                break;
            default:
                LOGGER.warn("Unknown request type, requestType={}, requestBody={}", connectId,
                    request.getRequestType(), request.getRequestBody());

        }
    }

    private void doRenewAsync(WriteDataRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("doRenewAsync: connectId={}, requestType={}, requestBody={}",
                connectId, request.getRequestType(), request.getRequestBody());
        }

        RenewDatumRequest renewDatumRequest = (RenewDatumRequest) request.getRequestBody();
        TaskEvent taskEvent = new TaskEvent(renewDatumRequest, TaskType.RENEW_DATUM_TASK);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void doClientOffAsync(WriteDataRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("doClientOffAsync: connectId={}, requestType={}, requestBody={}",
                connectId, request.getRequestType(), request.getRequestBody());
        }

        String connectId = request.getConnectId();
        TaskEvent taskEvent = new TaskEvent(Lists.newArrayList(connectId),
            TaskType.CANCEL_DATA_TASK);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void doUnPublishAsync(WriteDataRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("doUnPublishAsync: connectId={}, requestType={}, requestBody={}",
                connectId, request.getRequestType(), request.getRequestBody());
        }

        Publisher unPublisher = (Publisher) request.getRequestBody();
        TaskEvent taskEvent = new TaskEvent(unPublisher, TaskType.UN_PUBLISH_DATA_TASK);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void doPublishAsync(WriteDataRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("doPublishAsync: connectId={}, requestType={}, requestBody={}",
                connectId, request.getRequestType(), request.getRequestBody());
        }

        Publisher publisher = (Publisher) request.getRequestBody();
        TaskEvent taskEvent = new TaskEvent(publisher, TaskType.PUBLISH_DATA_TASK);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void doSnapshotAsync(WriteDataRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("doSnapshotAsync: connectId={}, requestType={}, requestBody={}",
                connectId, request.getRequestType(), request.getRequestBody());
        }

        String connectId = (String) request.getRequestBody();
        List<DatumSnapshotRequest> datumSnapshotRequests = renewService
            .getDatumSnapshotRequests(connectId);
        if (datumSnapshotRequests != null) {
            for (DatumSnapshotRequest datumSnapshotRequest : datumSnapshotRequests) {
                TaskEvent taskEvent = new TaskEvent(datumSnapshotRequest,
                    TaskType.DATUM_SNAPSHOT_TASK);
                taskListenerManager.sendTaskEvent(taskEvent);
            }
        }

    }

    /**
     * In silence, do not renew and snapshot
     */
    private boolean renewAndSnapshotInSilence() {
        boolean renewAndSnapshotInSilence = System.currentTimeMillis()
                                            - this.lastUpdateTimestamp.get() < this.sessionServerConfig
            .getRenewAndSnapshotSilentPeriodSec() * 1000L;
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug(
                "renewAndSnapshotInSilence: connectId={}, renewAndSnapshotInSilence={}", connectId,
                renewAndSnapshotInSilence);
        }
        return renewAndSnapshotInSilence;
    }

    private void refreshUpdateTime() {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("refreshUpdateTime: connectId={}", connectId);
        }
        lastUpdateTimestamp.set(System.currentTimeMillis());
    }
}