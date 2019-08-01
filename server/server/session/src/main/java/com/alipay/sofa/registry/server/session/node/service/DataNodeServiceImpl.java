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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.dataserver.ClientOffRequest;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.common.model.dataserver.GetDataVersionRequest;
import com.alipay.sofa.registry.common.model.dataserver.PublishDataRequest;
import com.alipay.sofa.registry.common.model.dataserver.SessionServerRegisterRequest;
import com.alipay.sofa.registry.common.model.dataserver.UnPublishDataRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.SessionProcessIdGenerator;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataNode.java, v 0.1 2017-12-01 11:30 shangyu.wh Exp $
 */
public class DataNodeServiceImpl implements DataNodeService {

    private static final Logger   LOGGER = LoggerFactory.getLogger(DataNodeServiceImpl.class);

    @Autowired
    private NodeExchanger         dataNodeExchanger;

    @Autowired
    private NodeManager           dataNodeManager;

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
        Request<PublishDataRequest> request = buildPublishDataRequest(publisher);
        try {
            sendRequest(request);
        } catch (RequestException e) {
            doRetryAsync("PublishData", request, e,
                sessionServerConfig.getPublishDataTaskRetryTimes(),
                sessionServerConfig.getPublishDataTaskRetryFirstDelay(),
                sessionServerConfig.getPublishDataTaskRetryIncrementDelay());
        }
    }

    private Request<PublishDataRequest> buildPublishDataRequest(Publisher publisher) {
        return new Request<PublishDataRequest>() {
            private AtomicInteger retryTimes = new AtomicInteger();

            @Override
            public PublishDataRequest getRequestBody() {
                PublishDataRequest publishDataRequest = new PublishDataRequest();
                publishDataRequest.setPublisher(publisher);
                publishDataRequest.setSessionServerProcessId(SessionProcessIdGenerator
                    .getSessionProcessId());
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
        Request<UnPublishDataRequest> request = buildUnPublishDataRequest(publisher);
        try {
            sendRequest(request);
        } catch (RequestException e) {
            doRetryAsync("UnPublishData", request, e,
                sessionServerConfig.getUnPublishDataTaskRetryTimes(),
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
                    publisher.getRegisterTimestamp());
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
    public void clientOff(List<String> connectIds) {
        if (connectIds == null || connectIds.isEmpty()) {
            return;
        }
        //get all local dataCenter data node
        Collection<Node> nodes = dataNodeManager.getDataCenterNodes();
        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
                Request<ClientOffRequest> request = buildClientOffRequest(connectIds, node);
                try {
                    sendRequest(request);
                } catch (RequestException e) {
                    doRetryAsync("ClientOff", request, e,
                        sessionServerConfig.getCancelDataTaskRetryTimes(),
                        sessionServerConfig.getCancelDataTaskRetryFirstDelay(),
                        sessionServerConfig.getCancelDataTaskRetryIncrementDelay());

                }
            }
        }
    }

    private Request<ClientOffRequest> buildClientOffRequest(List<String> connectIds, Node node) {
        return new Request<ClientOffRequest>() {

            private AtomicInteger retryTimes = new AtomicInteger();

            @Override
            public ClientOffRequest getRequestBody() {
                ClientOffRequest clientOffRequest = new ClientOffRequest();
                clientOffRequest.setHosts(connectIds);
                clientOffRequest.setGmtOccur(System.currentTimeMillis());
                return clientOffRequest;
            }

            @Override
            public URL getRequestUrl() {
                return new URL(node.getNodeUrl().getIpAddress(),
                    sessionServerConfig.getDataServerPort());
            }

            @Override
            public AtomicInteger getRetryTimes() {
                return retryTimes;
            }
        };
    }

    @Override
    public void registerSessionProcessId(final SessionServerRegisterRequest sessionServerRegisterRequest,
                                         final URL dataUrl) {
        try {
            Request<SessionServerRegisterRequest> request = new Request<SessionServerRegisterRequest>() {
                @Override
                public SessionServerRegisterRequest getRequestBody() {
                    return sessionServerRegisterRequest;
                }

                @Override
                public URL getRequestUrl() {
                    return dataUrl;
                }
            };
            dataNodeExchanger.request(request);
        } catch (RequestException e) {
            throw new RuntimeException("DataNodeService register processId error! "
                                       + e.getMessage(), e);
        }
    }

    @Override
    public Map<String/*datacenter*/, Map<String/*datainfoid*/, Long>> fetchDataVersion(URL dataNodeUrl,
                                                                                         Collection<String> dataInfoIdList) {
        Map<String, Map<String, Long>> map = new HashMap<>();
        try {
            Request<GetDataVersionRequest> getDataVersionRequestRequest = new Request<GetDataVersionRequest>() {
                @Override
                public GetDataVersionRequest getRequestBody() {
                    GetDataVersionRequest getDataVersionRequest = new GetDataVersionRequest();
                    getDataVersionRequest.setDataInfoIds((List<String>) dataInfoIdList);
                    return getDataVersionRequest;
                }

                @Override
                public URL getRequestUrl() {
                    return dataNodeUrl;
                }
            };

            Response response = dataNodeExchanger.request(getDataVersionRequestRequest);
            Object result = response.getResult();
            GenericResponse genericResponse = (GenericResponse) result;
            if (genericResponse.isSuccess()) {
                map = (Map<String, Map<String, Long>>) genericResponse.getData();
                if (map.isEmpty()) {
                    LOGGER
                        .warn(
                            "GetDataVersionRequestRequest get response contains no data!target data Node url:{} about dataInfoIds size:{}",
                            dataNodeUrl.getAddressString(), dataInfoIdList.size());
                }
            } else {
                throw new RuntimeException("fetchDataVersion has not get fail response! msg:"
                                           + genericResponse.getMessage());
            }
        } catch (RequestException e) {
            throw new RuntimeException("Fetch data Version request error! " + e.getMessage(), e);
        }

        return map;
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
        return getDatumMap(dataInfoId);
    }

    private Map<String, Datum> getDatumMap(String dataInfoId) {
        return getDatumMap(dataInfoId, null);
    }

    @Override
    public Map<String, Datum> getDatumMap(String dataInfoId, String dataCenterId) {

        Map<String/*datacenter*/, Datum> map;

        try {
            GetDataRequest getDataRequest = new GetDataRequest();

            //dataCenter null means all dataCenters
            if (dataCenterId != null) {
                getDataRequest.setDataCenter(dataCenterId);
            }

            getDataRequest.setDataInfoId(dataInfoId);

            Request<GetDataRequest> getDataRequestStringRequest = new Request<GetDataRequest>() {

                @Override
                public GetDataRequest getRequestBody() {
                    return getDataRequest;
                }

                @Override
                public URL getRequestUrl() {
                    return getUrl(dataInfoId);
                }
            };

            Response response = dataNodeExchanger.request(getDataRequestStringRequest);
            Object result = response.getResult();
            GenericResponse genericResponse = (GenericResponse) result;
            if (genericResponse.isSuccess()) {
                map = (Map<String, Datum>) genericResponse.getData();
                if (map == null || map.isEmpty()) {
                    LOGGER.warn("GetDataRequest get response contains no datum!dataInfoId={}",dataCenterId);
                } else {
                    map.forEach((dataCenter, datum) -> Datum.processDatum(datum));
                }
            } else {
                throw new RuntimeException(String.format("GetDataRequest has got fail response!dataInfoId:%s msg:%s",dataInfoId,genericResponse.getMessage()));
            }
        } catch (RequestException e) {
            throw new RuntimeException(String.format("Get data request to data node error!dataInfoId:%s msg:%s ",dataInfoId ,e.getMessage()), e);
        }

        return map;
    }

    @Override
    public Boolean renewDatum(RenewDatumRequest renewDatumRequest) {
        Request<RenewDatumRequest> request = buildRenewDatumRequest(renewDatumRequest);
        String msgFormat = "RenewDatum get response not success, target url: %s, message: %s";
        try {
            Response response = dataNodeExchanger.request(request);
            GenericResponse genericResponse = (GenericResponse) response.getResult();
            if (genericResponse.isSuccess()) {
                return (Boolean) genericResponse.getData();
            } else {
                throw new RuntimeException(String.format(msgFormat, request.getRequestUrl(),
                    genericResponse.getMessage()));
            }
        } catch (RequestException e) {
            throw new RuntimeException(String.format(msgFormat, request.getRequestUrl(),
                e.getMessage()), e);
        }
    }

    private Request<RenewDatumRequest> buildRenewDatumRequest(RenewDatumRequest renewDatumRequest) {
        return new Request<RenewDatumRequest>() {
            private AtomicInteger retryTimes = new AtomicInteger();

            @Override
            public RenewDatumRequest getRequestBody() {
                return renewDatumRequest;
            }

            @Override
            public URL getRequestUrl() {
                return new URL(renewDatumRequest.getDataServerIP(),
                    sessionServerConfig.getDataServerPort());
            }

            @Override
            public AtomicInteger getRetryTimes() {
                return retryTimes;
            }
        };
    }

    @Override
    public void sendDatumSnapshot(DatumSnapshotRequest datumSnapshotRequest) {
        Request<DatumSnapshotRequest> request = buildDatumSnapshotRequest(datumSnapshotRequest);
        try {
            sendRequest(request);
        } catch (RequestException e) {
            doRetryAsync("DatumSnapshot", request, e,
                sessionServerConfig.getDatumSnapshotTaskRetryTimes(),
                sessionServerConfig.getDatumSnapshotTaskRetryFirstDelay(),
                sessionServerConfig.getDatumSnapshotTaskRetryIncrementDelay());
        }

    }

    private Request<DatumSnapshotRequest> buildDatumSnapshotRequest(DatumSnapshotRequest datumSnapshotRequest) {
        return new Request<DatumSnapshotRequest>() {
            private AtomicInteger retryTimes = new AtomicInteger();

            @Override
            public DatumSnapshotRequest getRequestBody() {
                return datumSnapshotRequest;
            }

            @Override
            public URL getRequestUrl() {
                return new URL(datumSnapshotRequest.getDataServerIp(),
                    sessionServerConfig.getDataServerPort());
            }

            @Override
            public AtomicInteger getRetryTimes() {
                return retryTimes;
            }
        };
    }

    private void sendRequest(Request request) throws RequestException {
        Response response = dataNodeExchanger.request(request);
        Object result = response.getResult();
        CommonResponse commonResponse = (CommonResponse) result;
        if (!commonResponse.isSuccess()) {
            throw new RuntimeException(String.format(
                "response not success, failed! target url: %s, request: %s, message: %s",
                request.getRequestUrl(), request.getRequestBody(), commonResponse.getMessage()));
        }
    }

    private void doRetryAsync(String bizName, Request request, Exception e, int maxRetryTimes, long firstDelay,
                              long incrementDelay) {
        int retryTimes = request.getRetryTimes().incrementAndGet();
        if (retryTimes <= maxRetryTimes) {
            LOGGER.warn("{} failed, will retry again, retryTimes: {}, msg: {}", bizName, retryTimes, e.getMessage());
            asyncHashedWheelTimer.newTimeout(timeout -> {
                try {
                    sendRequest(request);
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
        return result >= 0L ? result : 0L;
    }

    private URL getUrl(String dataInfoId) {
        Node dataNode = dataNodeManager.getNode(dataInfoId);
        //meta push data node has not port
        String dataIp = dataNode.getNodeUrl().getIpAddress();
        return new URL(dataIp, sessionServerConfig.getDataServerPort());
    }

}