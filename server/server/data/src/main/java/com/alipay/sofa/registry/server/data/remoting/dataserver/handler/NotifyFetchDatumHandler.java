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
package com.alipay.sofa.registry.server.data.remoting.dataserver.handler;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.common.model.dataserver.NotifyFetchDatumRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DataServerCache;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.renew.LocalDataServerCleanHandler;
import com.alipay.sofa.registry.server.data.util.TimeUtil;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author qian.lqlq
 * @version $Id: NotifyFetchDatumProcessor.java, v 0.1 2018-04-29 15:10 qian.lqlq Exp $
 */
public class NotifyFetchDatumHandler extends AbstractServerHandler<NotifyFetchDatumRequest> {

    private static final Logger         LOGGER = LoggerFactory
                                                   .getLogger(NotifyFetchDatumHandler.class);

    @Autowired
    private DataServerCache             dataServerCache;

    @Autowired
    private DataServerConnectionFactory dataServerConnectionFactory;

    @Autowired
    private DataChangeEventCenter       dataChangeEventCenter;

    @Autowired
    private Exchange                    boltExchange;

    @Autowired
    private DataServerConfig            dataServerConfig;

    @Autowired
    private DatumCache                  datumCache;

    @Autowired
    private LocalDataServerCleanHandler localDataServerCleanHandler;

    @Override
    public void checkParam(NotifyFetchDatumRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getIp(), "ip");
    }

    @Override
    public Object doHandle(Channel channel, NotifyFetchDatumRequest request) {
        ParaCheckUtil.checkNotBlank(request.getIp(), "ip");

        //receive other data NotifyFetchDatumRequest,must delay clean datum task until fetch all datum
        localDataServerCleanHandler.reset();

        Map<String, Map<String, Long>> versionMap = request.getDataVersionMap();
        long version = request.getChangeVersion();
        String ip = request.getIp();
        if (version >= dataServerCache.getCurVersion()) {
            if (versionMap.isEmpty()) {
                LOGGER.info(
                        "[NotifyFetchDatumHandler] get changeVersion map is empty,change version is {},current version is {},ip is {}",
                        version, dataServerCache.getCurVersion(), ip);
                dataServerCache.synced(version, ip);
            } else {
                ExecutorFactory.getCommonExecutor().execute(() -> {
                    for (Entry<String, Map<String, Long>> dataCenterEntry : versionMap.entrySet()) {
                        String dataCenter = dataCenterEntry.getKey();
                        Map<String, Long> map = dataCenterEntry.getValue();
                        for (Entry<String, Long> dataInfoEntry : map.entrySet()) {
                            String dataInfoId = dataInfoEntry.getKey();
                            Datum datum = datumCache.get(dataCenter, dataInfoId);
                            if (datum != null) {
                                long inVersion = dataInfoEntry.getValue();
                                long currentVersion = datum.getVersion();
                                if (currentVersion > inVersion) {
                                    LOGGER.info(

                                            "[NotifyFetchDatumHandler] ignore fetch because changeVersion {} is less than {},dataInfoId={},dataCenter={}",
                                            inVersion, currentVersion, dataInfoId, dataCenter);
                                    continue;
                                } else if (datum.getVersion() == dataInfoEntry.getValue()) {
                                    //if version same,maybe remove publisher all by LocalDataServerCleanHandler,so must fetch from other node
                                    if (!datum.getPubMap().isEmpty()) {
                                        continue;
                                    }
                                }
                            }
                            fetchDatum(ip, dataCenter, dataInfoId);
                        }
                    }
                    dataServerCache.synced(version, ip);
                });
            }
        } else {
            LOGGER.info("[NotifyFetchDatumHandler] ignore notify because changeVersion {} is less than {},ip is {}",
                    version, dataServerCache.getCurVersion(), ip);
        }
        return CommonResponse.buildSuccessResponse();
    }

    /**
     * 拉取数据
     *
     * @param targetIp
     * @param dataCenter
     * @param dataInfoId
     */
    private void fetchDatum(String targetIp, String dataCenter, String dataInfoId) {
        while (dataServerConnectionFactory.getConnection(targetIp) != null) {
            Connection connection = dataServerConnectionFactory.getConnection(targetIp);
            if (connection == null || !connection.isFine()) {
                throw new RuntimeException(String.format("connection of %s is not available",
                    targetIp));
            }
            try {
                Server syncServer = boltExchange.getServer(dataServerConfig.getSyncDataPort());
                GenericResponse<Map<String, Datum>> response = (GenericResponse<Map<String, Datum>>) syncServer
                    .sendSync(syncServer.getChannel(connection.getRemoteAddress()),
                        new GetDataRequest(dataInfoId, dataCenter),
                        dataServerConfig.getRpcTimeout());
                if (response.isSuccess()) {
                    Datum datum = response.getData().get(dataCenter);
                    if (datum != null) {
                        dataChangeEventCenter.sync(DataChangeTypeEnum.COVER,
                            DataSourceTypeEnum.BACKUP, datum);
                        LOGGER
                            .info(
                                "[NotifyFetchDatumHandler] fetch datum success,dataInfoId={},dataCenter={},targetIp={}",
                                datum.getDataInfoId(), datum.getDataCenter(), targetIp);
                    }
                    break;
                } else {
                    throw new RuntimeException(response.getMessage());
                }
            } catch (Exception e) {
                LOGGER.error("[NotifyFetchDatumHandler] fetch datum error", e);
                TimeUtil.randomDelay(500);
            }
        }
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return CommonResponse.buildFailedResponse(msg);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return NotifyFetchDatumRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}