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
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.event.AfterWorkingProcess;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.renew.DatumLeaseManager;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * handling snapshot request
 *
 * @author kezhu.wukz
 * @version $Id: RenewDatumHandler.java, v 0.1 2019-05-30 15:48 kezhu.wukz Exp $
 */
public class RenewDatumHandler extends AbstractServerHandler<RenewDatumRequest> implements
                                                                               AfterWorkingProcess {

    /** LOGGER */
    private static final Logger LOGGER       = LoggerFactory.getLogger(RenewDatumHandler.class);

    private static final Logger RENEW_LOGGER = LoggerFactory.getLogger(
                                                 ValueConstants.LOGGER_NAME_RENEW,
                                                 "[RenewDatumHandler]");

    private final AtomicBoolean renewEnabled = new AtomicBoolean(false);

    @Autowired
    private DatumLeaseManager   datumLeaseManager;

    @Autowired
    private DatumCache          datumCache;

    @Autowired
    private ThreadPoolExecutor  renewDatumProcessorExecutor;

    @Autowired
    private DataServerConfig    dataServerConfig;

    @Override
    public Executor getExecutor() {
        return renewDatumProcessorExecutor;
    }

    @Override
    protected void logRequest(Channel channel, RenewDatumRequest request) {
    }

    @Override
    public void checkParam(RenewDatumRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getConnectId(), "RenewDatumRequest.connectId");
        ParaCheckUtil.checkNotBlank(request.getDigestSum(), "RenewDatumRequest.digestSum");
    }

    @Override
    public Object doHandle(Channel channel, RenewDatumRequest request) {
        if (RENEW_LOGGER.isDebugEnabled()) {
            RENEW_LOGGER.debug("doHandle: request={}", request);
        }

        if (!renewEnabled.get()) {
            LOGGER.warn("[forward] Renew request refused, renewEnabled is false, request: {}",
                request);
            GenericResponse response = new GenericResponse();
            response.setSuccess(false);
            response.setMessage("Renew request refused, renewEnabled is false yet");
            return response;
        }

        boolean theSame = renewDatum(request);

        return new GenericResponse<Boolean>().fillSucceed(theSame);
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
        return RenewDatumRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }

    /**
     * 1. Update the timestamp corresponding to connectId in datumCache
     * 2. Compare checksum: Get all pubs corresponding to the connId from datumCache and calculate checksum.
     */
    private boolean renewDatum(RenewDatumRequest request) {
        String connectId = request.getConnectId();
        String renewDigest = request.getDigestSum();

        // Get all pubs corresponding to the connectId from datumCache
        Map<String, Publisher> publisherMap = datumCache.getOwnByConnectId(connectId);
        String cacheDigest = null;
        if (publisherMap != null && publisherMap.values().size() > 0) {
            cacheDigest = String.valueOf(PublisherDigestUtil.getDigestValueSum(publisherMap
                .values()));
        }

        // record the renew timestamp
        datumLeaseManager.renew(connectId);

        boolean result = StringUtils.equals(renewDigest, cacheDigest);

        if (!result) {
            RENEW_LOGGER.info("Digest different! renewDatumRequest={}", request);
        }
        return result;
    }

    @Override
    public void afterWorkingProcess() {
        /*
         * After the snapshot data is synchronized during startup, it is queued and then placed asynchronously into
         * DatumCache. When the notification becomes WORKING, there may be data in the queue that is not executed
         * to DatumCache. So it need to sleep for a while.
         */
        try {
            TimeUnit.MILLISECONDS.sleep(dataServerConfig.getRenewEnableDelaySec());
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        renewEnabled.set(true);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
