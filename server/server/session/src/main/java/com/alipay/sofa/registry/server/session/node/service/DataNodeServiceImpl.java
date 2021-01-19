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
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.common.model.ClientOffPublishers;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.dataserver.*;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shangyu.wh
 * @version $Id: DataNode.java, v 0.1 2017-12-01 11:30 shangyu.wh Exp $
 */
public class DataNodeServiceImpl implements DataNodeService {

    private static final Logger   LOGGER = LoggerFactory.getLogger(DataNodeServiceImpl.class);

    @Autowired
    private NodeExchanger         dataNodeExchanger;

    @Autowired
    private SlotTableCache        slotTableCache;

    @Autowired
    private SessionServerConfig   sessionServerConfig;

    private AsyncHashedWheelTimer asyncHashedWheelTimer;

    @PostConstruct
    public void init() {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        asyncHashedWheelTimer = new AsyncHashedWheelTimer(threadFactoryBuilder.setNameFormat(
            "Registry-DataNodeServiceImpl-Retry-WheelTimer").build(), 100, TimeUnit.MILLISECONDS,
            1024, sessionServerConfig.getDataNodeRetryExecutorThreadSize(),
            sessionServerConfig.getDataNodeRetryExecutorQueueSize(), threadFactoryBuilder
                .setNameFormat("Registry-DataNodeServiceImpl-Retry-WheelExecutor-%d").build(),
            new TaskFailedCallback() {
                @Override
                public void executionRejected(Throwable e) {
                    LOGGER.error("executionRejected: " + e.getMessage(), e);
                }

                @Override
                public void executionFailed(Throwable e) {
                    LOGGER.error("executionFailed: " + e.getMessage(), e);
                }
            });
    }

    @Override
    public void register(final Publisher publisher) {
        String bizName = "PublishData";
        Request<PublishDataRequest> request = buildPublishDataRequest(publisher);
        try {
            sendRequest(bizName, request);
        } catch (RequestException e) {
            doRetryAsync(bizName, request, e, sessionServerConfig.getPublishDataTaskRetryTimes(),
                sessionServerConfig.getPublishDataTaskRetryFirstDelay(),
                sessionServerConfig.getPublishDataTaskRetryIncrementDelay());
        }
    }

    private Request<PublishDataRequest> buildPublishDataRequest(Publisher publisher) {
        return new Request<PublishDataRequest>() {
            private AtomicInteger retryTimes = new AtomicInteger();

            @Override
            public PublishDataRequest getRequestBody() {
                PublishDataRequest publishDataRequest = new PublishDataRequest(publisher);
                return publishDataRequest;
            }

            @Override
            public URL getRequestUrl() {
                return getUrl(publisher.getDataInfoId());
            }

            @Override
            public AtomicInteger getRetryTimes() {
                return retryTimes;
            }
        };
    }

    @Override
    public void unregister(final Publisher publisher) {
        String bizName = "UnPublishData";
        Request<UnPublishDataRequest> request = buildUnPublishDataRequest(publisher);
        try {
            sendRequest(bizName, request);
        } catch (RequestException e) {
            doRetryAsync(bizName, request, e, sessionServerConfig.getUnPublishDataTaskRetryTimes(),
                sessionServerConfig.getUnPublishDataTaskRetryFirstDelay(),
                sessionServerConfig.getUnPublishDataTaskRetryIncrementDelay());
        }
    }

    private Request<UnPublishDataRequest> buildUnPublishDataRequest(Publisher publisher) {
        return new Request<UnPublishDataRequest>() {

            private AtomicInteger retryTimes = new AtomicInteger();

            @Override
            public UnPublishDataRequest getRequestBody() {
                UnPublishDataRequest unPublishDataRequest = new UnPublishDataRequest(
                    publisher.getDataInfoId(), publisher.getRegisterId(),
                    publisher.getRegisterTimestamp(), ServerEnv.PROCESS_ID, publisher.getVersion());
                return unPublishDataRequest;
            }

            @Override
            public URL getRequestUrl() {
                return getUrl(publisher.getDataInfoId());
            }

            @Override
            public AtomicInteger getRetryTimes() {
                return retryTimes;
            }
        };
    }

    @Override
    public void clientOff(ClientOffPublishers clientOffPublishers) {
        if (clientOffPublishers.isEmpty()) {
            return;
        }
        //group by dataInfoId
        String bizName = "ClientOff";
        List<Request<ClientOffRequest>> requests = buildClientOffRequest(clientOffPublishers);
        for (Request<ClientOffRequest> req : requests) {
            try {
                sendRequest(bizName, req);
            } catch (RequestException e) {
                doRetryAsync(bizName, req, e, sessionServerConfig.getCancelDataTaskRetryTimes(),
                    sessionServerConfig.getCancelDataTaskRetryFirstDelay(),
                    sessionServerConfig.getCancelDataTaskRetryIncrementDelay());

            }
        }
    }

    private List<Request<ClientOffRequest>> buildClientOffRequest(ClientOffPublishers clientOffPublishers) {
        List<Request<ClientOffRequest>> ret = Lists.newArrayList();
        Map<Integer, ClientOffRequest> groups = groupBySlot(clientOffPublishers);
        for (Map.Entry<Integer, ClientOffRequest> group : groups.entrySet()) {
            ret.add(new Request<ClientOffRequest>() {
                private AtomicInteger retryTimes = new AtomicInteger();

                @Override
                public ClientOffRequest getRequestBody() {
                    return group.getValue();
                }

                @Override
                public URL getRequestUrl() {
                    return getUrl(group.getKey());
                }

                @Override
                public AtomicInteger getRetryTimes() {
                    return retryTimes;
                }
            });
        }
        return ret;
    }

    @Override
    public Map<String/*datacenter*/, Map<String/*datainfoid*/, DatumVersion>> fetchDataVersion(URL dataNodeUrl,
                                                                                                 int slotId) {
        try {
            Request<GetDataVersionRequest> getDataVersionRequestRequest = new Request<GetDataVersionRequest>() {
                @Override
                public GetDataVersionRequest getRequestBody() {
                    GetDataVersionRequest getDataVersionRequest = new GetDataVersionRequest(
                        ServerEnv.PROCESS_ID, slotId);
                    return getDataVersionRequest;
                }

                @Override
                public URL getRequestUrl() {
                    return dataNodeUrl;
                }
            };

            Response response = dataNodeExchanger.request(getDataVersionRequestRequest);
            Object result = response.getResult();
            SlotAccessGenericResponse<Map<String, Map<String, DatumVersion>>> genericResponse = (SlotAccessGenericResponse<Map<String, Map<String, DatumVersion>>>) result;
            if (genericResponse.isSuccess()) {
                Map<String, Map<String, DatumVersion>> map = genericResponse.getData();
                return map;
            } else {
                throw new RuntimeException(String.format(
                    "fetchDataVersion fail response! access=%s, msg:%s",
                    genericResponse.getSlotAccess(), genericResponse.getMessage()));
            }
        } catch (RequestException e) {
            throw new RuntimeException("fetchDataVersion request error", e);
        }
    }

    @Override
    public Datum fetchDataCenter(String dataInfoId, String dataCenterId) {

        Map<String/*datacenter*/, Datum> map = getDatumMap(dataInfoId, dataCenterId);
        if (map != null && map.size() > 0) {
            return map.get(dataCenterId);
        }
        return null;
    }

    @Override
    public Map<String/*datacenter*/, Datum> fetchGlobal(String dataInfoId) {
        //get all dataCenter data
        return getDatumMap(dataInfoId, null);
    }

    @Override
    public Map<String, Datum> getDatumMap(String dataInfoId, String dataCenterId) {
        Map<String/*datacenter*/, Datum> map;
        try {
            //dataCenter null means all dataCenters
            GetDataRequest getDataRequest = new GetDataRequest(ServerEnv.PROCESS_ID, dataInfoId, dataCenterId);

            Request<GetDataRequest> getDataRequestStringRequest = new Request<GetDataRequest>() {

                @Override
                public GetDataRequest getRequestBody() {
                    return getDataRequest;
                }

                @Override
                public URL getRequestUrl() {
                    return getUrl(dataInfoId);
                }

                @Override
                public Integer getTimeout() {
                    return sessionServerConfig.getDataNodeExchangeForFetchDatumTimeOut();
                }
            };

            Response response = dataNodeExchanger.request(getDataRequestStringRequest);
            Object result = response.getResult();
            SlotAccessGenericResponse<Map<String, Datum>> genericResponse = (SlotAccessGenericResponse<Map<String, Datum>>) result;
            if (genericResponse.isSuccess()) {
                map = genericResponse.getData();
                if (CollectionUtils.isEmpty(map)) {
                    LOGGER.warn("GetDataRequest get response contains no datum!dataInfoId={}", dataInfoId);
                } else {
                    map.forEach((dataCenter, datum) -> Datum.internDatum(datum));
                }
            } else {
                throw new RuntimeException(
                        String.format("GetDataRequest has got fail response!dataInfoId:%s msg:%s", dataInfoId,
                                genericResponse.getMessage()));
            }
        } catch (RequestException e) {
            throw new RuntimeException(
                    String.format("Get data request to data node error!dataInfoId:%s msg:%s ", dataInfoId,
                            e.getMessage()), e);
        }

        return map;
    }

    private CommonResponse sendRequest(String bizName, Request request) throws RequestException {
        Response response = dataNodeExchanger.request(request);
        Object result = response.getResult();
        CommonResponse commonResponse = (CommonResponse) result;
        if (!commonResponse.isSuccess()) {
            throw new RuntimeException(String.format(
                "[%s] response not success, failed! target url: %s, request: %s, message: %s",
                bizName, request.getRequestUrl(), request.getRequestBody(),
                commonResponse.getMessage()));
        }
        return commonResponse;
    }

    private void doRetryAsync(String bizName, Request request, Exception e, int maxRetryTimes, long firstDelay,
                              long incrementDelay) {
        int retryTimes = request.getRetryTimes().incrementAndGet();
        if (retryTimes <= maxRetryTimes) {
            LOGGER.warn("{} failed, will retry again, retryTimes: {}, msg: {}", bizName, retryTimes, e.getMessage());
            asyncHashedWheelTimer.newTimeout(timeout -> {
                try {
                    sendRequest(bizName, request);
                } catch (RequestException ex) {
                    doRetryAsync(bizName, request, ex, maxRetryTimes, firstDelay, incrementDelay);
                }
            }, getDelayTime(retryTimes, firstDelay, incrementDelay), TimeUnit.MILLISECONDS);
        } else {
            LOGGER.error(String.format(
                    "%s failed, retryTimes have exceeded! stop retry! retryTimes: %s, url: %s, request: %s, msg: %s",
                    bizName, (retryTimes - 1), request.getRequestUrl(), request.getRequestBody(), e.getMessage()), e);
        }
    }

    private long getDelayTime(int retry, long firstDelay, long incrementDelay) {
        long initialSleepTime = TimeUnit.MILLISECONDS.toMillis(firstDelay);
        long increment = TimeUnit.MILLISECONDS.toMillis(incrementDelay);
        long result = initialSleepTime + (increment * (retry - 1));
        return Math.max(result, 0L);
    }

    private URL getUrl(String dataInfoId) {
        final Slot slot = slotTableCache.getSlot(dataInfoId);
        return getUrl(slot.getId());
    }

    private URL getUrl(int slotId) {
        final String dataIp = slotTableCache.getLeader(slotId);
        if (StringUtils.isBlank(dataIp)) {
            throw new RequestException(String.format("slot has no leader, slotId=%s", slotId));
        }
        return new URL(dataIp, sessionServerConfig.getDataServerPort());
    }

    private Map<Integer, ClientOffRequest> groupBySlot(ClientOffPublishers clientOffPublishers) {
        List<Publisher> publishers = clientOffPublishers.getPublishers();
        Map<Integer, ClientOffRequest> ret = Maps.newHashMap();
        for (Publisher publisher : publishers) {
            final String dataInfoId = publisher.getDataInfoId();
            int slotId = slotTableCache.slotOf(dataInfoId);
            ClientOffRequest request = ret.computeIfAbsent(slotId,
                    k -> new ClientOffRequest(ServerEnv.PROCESS_ID, clientOffPublishers.getConnectId()));
            request.addPublisher(publisher);
        }
        return ret;
    }
}