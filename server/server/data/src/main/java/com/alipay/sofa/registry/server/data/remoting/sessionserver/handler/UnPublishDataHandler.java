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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.UnPublishDataRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.UnPublisher;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.correction.DatumLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.forward.ForwardService;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * processor to unPublish specific data
 *
 * @author qian.lqlq
 * @version $Id: UnPublishDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class UnPublishDataHandler extends AbstractServerHandler<UnPublishDataRequest> {

    /** LOGGER */
    private static final Logger   LOGGER = LoggerFactory.getLogger(UnPublishDataHandler.class);

    @Autowired
    private ForwardService        forwardService;

    @Autowired
    private DataChangeEventCenter dataChangeEventCenter;

    @Autowired
    private DataServerConfig      dataServerConfig;

    @Autowired
    private DatumLeaseManager     datumLeaseManager;

    @Autowired
    private DatumCache            datumCache;

    @Override
    public void checkParam(UnPublishDataRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "UnPublishDataRequest.dataInfoId");
        ParaCheckUtil.checkNotBlank(request.getRegisterId(), "UnPublishDataRequest.registerId");
    }

    @Override
    public Object doHandle(Channel channel, UnPublishDataRequest request) {
        if (forwardService.needForward()) {
            LOGGER.warn("[forward] UnPublish request refused, request: {}", request);
            CommonResponse response = new CommonResponse();
            response.setSuccess(false);
            response.setMessage("Request refused, Server status is not working");
            return response;
        }

        dataChangeEventCenter.onChange(
            new UnPublisher(request.getDataInfoId(), request.getRegisterId(), request
                .getRegisterTimestamp()), dataServerConfig.getLocalDataCenter());

        // Attempt to get connectId from datumCache (Datum may not exist), and record the reNew timestamp
        String connectId = getConnectId(request);
        if (connectId != null) {
            datumLeaseManager.reNew(connectId);
        }

        return CommonResponse.buildSuccessResponse();
    }

    /**
     * get connectId from datumCache
     */
    private String getConnectId(UnPublishDataRequest request) {
        String dataInfoId = request.getDataInfoId();
        String dataCenter = dataServerConfig.getLocalDataCenter();
        Datum datum = datumCache.get(dataCenter, dataInfoId);
        Map<String, Publisher> pubMap = datum.getPubMap();
        if (pubMap != null) {
            Publisher publisher = pubMap.get(request.getRegisterId());
            return publisher.getSourceAddress().getAddressString();
        }
        return null;
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
        return UnPublishDataRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}
