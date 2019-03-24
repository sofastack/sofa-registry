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

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    public DataNodeServiceImpl() {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        asyncHashedWheelTimer = new AsyncHashedWheelTimer(threadFactoryBuilder.setNameFormat(
            "Registry-DataNodeServiceImpl-WheelTimer").build(), 100, TimeUnit.MILLISECONDS, 1024,
            threadFactoryBuilder.setNameFormat("Registry-DataNodeServiceImpl-WheelExecutor-%d")
                .build(), new TaskFailedCallback() {
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

        try {

            Request<PublishDataRequest> publisherRequest = new Request<PublishDataRequest>() {

                private URL url;

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
                    if (url == null) {
                        url = getUrl(publisher.getDataInfoId());
                    }
                    return url;
                }
            };

            Response response = dataNodeExchanger.request(publisherRequest);

            Object result = response.getResult();
            if (result instanceof CommonResponse) {
                CommonResponse commonResponse = (CommonResponse) result;
                if (!commonResponse.isSuccess()) {
                    LOGGER.error(
                        "PublishDataRequest get server response failed!target url:{},message:{}",
                        publisherRequest.getRequestUrl(), commonResponse.getMessage());
                    throw new RuntimeException(
                        "PublishDataRequest get server response failed! msg:"
                                + commonResponse.getMessage());
                }
            }
        } catch (RequestException e) {
            LOGGER.error("DataNodeService register new publisher error! " + e.getRequestMessage(),
                e);
            throw new RuntimeException("DataNodeService register new publisher error! "
                                       + e.getRequestMessage(), e);
        }
    }

    @Override
    public void unregister(final Publisher publisher) {
        try {
            Request<UnPublishDataRequest> unPublishRequest = new Request<UnPublishDataRequest>() {

                private URL url;

                @Override
                public UnPublishDataRequest getRequestBody() {
                    UnPublishDataRequest unPublishDataRequest = new UnPublishDataRequest();
                    unPublishDataRequest.setDataInfoId(publisher.getDataInfoId());
                    unPublishDataRequest.setRegisterId(publisher.getRegisterId());
                    unPublishDataRequest.setRegisterTimestamp(publisher.getRegisterTimestamp());
                    return unPublishDataRequest;
                }

                @Override
                public URL getRequestUrl() {
                    if (url == null) {
                        url = getUrl(publisher.getDataInfoId());
                    }
                    return url;
                }
            };

            Response response = dataNodeExchanger.request(unPublishRequest);

            Object result = response.getResult();
            if (result instanceof CommonResponse) {
                CommonResponse commonResponse = (CommonResponse) result;
                if (!commonResponse.isSuccess()) {
                    LOGGER.error(
                        "UnPublishRequest get server response failed!target url:{},message:{}",
                        unPublishRequest.getRequestUrl(), commonResponse.getMessage());
                    throw new RuntimeException("UnPublishRequest get server response failed! msg:"
                                               + commonResponse.getMessage());
                }
            }

        } catch (RequestException e) {
            LOGGER.error("Unregister publisher to data node error! " + e.getRequestMessage(), e);
            throw new RuntimeException("Unregister publisher to data node error! "
                                       + e.getRequestMessage(), e);
        }

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
                Request<ClientOffRequest> clientOffRequestRequest = new Request<ClientOffRequest>() {

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
                try {

                    Response response = dataNodeExchanger.request(clientOffRequestRequest);
                    Object result = response.getResult();
                    if (result instanceof CommonResponse) {
                        CommonResponse commonResponse = (CommonResponse) result;
                        if (!commonResponse.isSuccess()) {
                            LOGGER
                                .error(
                                    "ClientOff RequestRequest get response failed!target url:{},message:{}",
                                    node.getNodeUrl(), commonResponse.getMessage());
                            throw new RuntimeException(
                                "ClientOff RequestRequest get response failed! msg:"
                                        + commonResponse.getMessage());
                        }
                    } else {
                        LOGGER
                            .error(
                                "ClientOff Request has not get response or response type illegal!url:{}",
                                node.getNodeUrl());
                        throw new RuntimeException(
                            "ClientOff Request has not get response or response type illegal!");
                    }
                } catch (Exception e) {
                    LOGGER.error("Client Off request error! ", e);
                    clientOffRetry(clientOffRequestRequest);
                }

            }
        }
    }

    private void clientOffRetry(Request<ClientOffRequest> clientOffRequestRequest) {

        URL url = clientOffRequestRequest.getRequestUrl();

        int retryTimes = clientOffRequestRequest.getRetryTimes().incrementAndGet();

        if (retryTimes <= sessionServerConfig.getCancelDataTaskRetryTimes()) {
            asyncHashedWheelTimer.newTimeout(timeout -> {
                try {
                    Response response = dataNodeExchanger.request(clientOffRequestRequest);
                    Object result = response.getResult();
                    if (result instanceof CommonResponse) {
                        CommonResponse commonResponse = (CommonResponse) result;
                        if (!commonResponse.isSuccess()) {
                            LOGGER.error(
                                    "ClientOff retry RequestRequest get response failed!retryTimes={},target url:{},message:{}",
                                    retryTimes,
                                    url, commonResponse.getMessage());
                            throw new RuntimeException(
                                    "ClientOff retry RequestRequest get response failed! msg:" +
                                            commonResponse.getMessage());
                        }
                    } else {
                        LOGGER.error(
                                "ClientOff retry Request has not get response or response type illegal!retryTimes={},url:{}",
                                retryTimes, url);
                        throw new RuntimeException(
                                "ClientOff retry Request has not get response or response type illegal!");
                    }
                } catch (Exception e) {
                    clientOffRetry(clientOffRequestRequest);
                }
            }, getBlockTime(retryTimes), TimeUnit.MILLISECONDS);
        } else {
            LOGGER.error("ClientOff retryTimes have exceeded! stop retry! retryTimes={}, url={}, request={}",
                    retryTimes, url, clientOffRequestRequest.getRequestBody());
        }
    }

    private long getBlockTime(int retry) {
        long initialSleepTime = TimeUnit.MILLISECONDS.toMillis(sessionServerConfig
            .getCancelDataTaskRetryFirstDelay());
        long increment = TimeUnit.MILLISECONDS.toMillis(sessionServerConfig
            .getCancelDataTaskRetryIncrementDelay());
        long result = initialSleepTime + (increment * (retry - 1));
        return result >= 0L ? result : 0L;
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
            LOGGER.error("DataNodeService register processId error! " + e.getRequestMessage(), e);
            throw new RuntimeException("DataNodeService register processId error! "
                                       + e.getRequestMessage(), e);
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
            if (result instanceof GenericResponse) {
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
                    LOGGER.error("fetchDataVersion has not get fail response!msg:{}",
                        genericResponse.getMessage());
                    throw new RuntimeException("fetchDataVersion has not get fail response! msg:"
                                               + genericResponse.getMessage());
                }
            } else {
                LOGGER
                    .error("GetDataVersionRequestRequest has not get response or response type illegal!");
            }

        } catch (RequestException e) {
            LOGGER.error("Fetch data Version request error! " + e.getRequestMessage(), e);
            throw new RuntimeException(
                "Fetch data Version request error! " + e.getRequestMessage(), e);
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

    @Override public Map<String, Datum> getDatumMap(String dataInfoId, String dataCenterId) {

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
            if (result instanceof GenericResponse) {
                GenericResponse genericResponse = (GenericResponse) result;
                if (genericResponse.isSuccess()) {
                    map = (Map<String, Datum>) genericResponse.getData();
                    if (map == null || map.isEmpty()) {
                        LOGGER.warn("GetDataRequest get response contains no datum!");
                    } else {
                        map.forEach((dataCenter, datum) -> Datum.processDatum(datum));
                    }
                } else {
                    LOGGER.error("GetDataRequest has not get fail response!msg:{}", genericResponse.getMessage());
                    throw new RuntimeException("GetDataRequest has not get fail response! msg:" +
                            genericResponse.getMessage());
                }
            } else {
                LOGGER.error("GetDataRequest has not get response or response type illegal!");
                throw new RuntimeException("GetDataRequest has not get response or response type illegal!");
            }
        } catch (RequestException e) {
            LOGGER.error("Get data request to data node error! " + e.getRequestMessage(), e);
            throw new RuntimeException(
                    "Get data request to data node error! " + e.getRequestMessage(), e);
        }

        return map;
    }

    private URL getUrl(String dataInfoId) {

        Node dataNode = dataNodeManager.getNode(dataInfoId);
        if (dataNode != null) {
            //meta push data node has not port
            String dataIp = dataNode.getNodeUrl().getIpAddress();
            return new URL(dataIp, sessionServerConfig.getDataServerPort());
        }
        return null;
    }

}