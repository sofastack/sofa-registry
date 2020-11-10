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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.event.DatumSnapshotEvent;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.renew.DatumLeaseManager;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * handling snapshot request
 *
 * @author kezhu.wukz
 * @version $Id: ClientOffProcessor.java, v 0.1 2019-05-30 15:48 kezhu.wukz Exp $
 */
public class DatumSnapshotHandler extends AbstractServerHandler<DatumSnapshotRequest> {

    private static final Logger   RENEW_LOGGER                = LoggerFactory.getLogger(
                                                                  ValueConstants.LOGGER_NAME_RENEW,
                                                                  "[DatumSnapshotHandler]");

    /** Limited List Printing */
    private static final int      LIMITED_LIST_SIZE_FOR_PRINT = 10;

    @Autowired
    private DataChangeEventCenter dataChangeEventCenter;

    @Autowired
    private DatumLeaseManager     datumLeaseManager;

    @Autowired
    private DatumCache            datumCache;

    @Autowired
    private ThreadPoolExecutor    renewDatumProcessorExecutor;

    @Override
    public Executor getExecutor() {
        return renewDatumProcessorExecutor;
    }

    @Override
    public void checkParam(DatumSnapshotRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getConnectId(), "DatumSnapshotRequest.connectId");
        ParaCheckUtil.checkNotEmpty(request.getPublishers(), "DatumSnapshotRequest.publishers");
    }

    @Override
    public Object doHandle(Channel channel, DatumSnapshotRequest request) {
        RENEW_LOGGER.info("Received datumSnapshotRequest: {}", request);

        String connectId = WordCache.getInstance().getWordCache(request.getConnectId());

        // convert to pubMap, and wrap it by WordCache
        Map<String, Publisher> pubMap = new HashMap<>();
        List<Publisher> publishers = request.getPublishers();
        if (publishers != null) {
            for (Publisher publisher : publishers) {
                Publisher.internPublisher(publisher);
                pubMap.put(publisher.getRegisterId(), publisher);
            }
        }

        // diff the cache and snapshot
        boolean isDiff = true;
        Map<String, Publisher> cachePubMap = datumCache.getByConnectId(connectId);
        if (cachePubMap == null) {
            RENEW_LOGGER
                .info(
                    ">>>>>>> connectId={}, cachePubMap.size=0, pubMap.size={}, isDiff={}, the diff is: pubMap={}",
                    connectId, pubMap.size(), isDiff, limitedToString(pubMap.values()));
        } else {
            List diffPub1 = subtract(pubMap, cachePubMap);
            List diffPub2 = subtract(cachePubMap, pubMap);
            if (diffPub1.size() == 0 && diffPub2.size() == 0) {
                isDiff = false;
            }
            RENEW_LOGGER
                .info(
                    ">>>>>>> connectId={}, cachePubMap.size={}, pubMap.size={}, isDiff={}, the diff is: pubMap-cachePubMap=(size:{}){}, cachePubMap-pubMap=(size:{}){}",
                    connectId, cachePubMap.size(), pubMap.size(), isDiff, diffPub1.size(),
                    limitedToString(diffPub1), diffPub2.size(), limitedToString(diffPub2));
        }

        if (isDiff) {
            // build DatumSnapshotEvent and send to eventCenter
            dataChangeEventCenter.onChange(new DatumSnapshotEvent(connectId, cachePubMap, pubMap));
        }

        // record the renew timestamp
        datumLeaseManager.renew(connectId);

        return CommonResponse.buildSuccessResponse();
    }

    /**
     * Limited List Printing
     */
    private String limitedToString(Collection<Publisher> publishers) {
        Iterator<Publisher> it = publishers.iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int i = 1;
        for (;;) {
            Publisher e = it.next();
            sb.append("Publisher{dataInfoId='").append(e.getDataInfoId()).append('\'');
            sb.append(", cell='").append(e.getCell()).append('\'');
            sb.append(", registerId='").append(e.getRegisterId()).append('\'');
            sb.append(", version=").append(e.getVersion());
            sb.append(", sourceAddress=").append(e.getSourceAddress());
            sb.append(", registerTimestamp=").append(e.getRegisterTimestamp());
            sb.append(", clientRegisterTimestamp=").append(e.getClientRegisterTimestamp());
            sb.append('}');
            if (!it.hasNext() || i++ >= LIMITED_LIST_SIZE_FOR_PRINT)
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    private List subtract(Map<String, Publisher> pubMap1, Map<String, Publisher> pubMap2) {
        List list = new ArrayList();
        for (Map.Entry<String, Publisher> entry : pubMap1.entrySet()) {
            String registerId = entry.getKey();
            Publisher publisher1 = entry.getValue();
            Publisher publisher2 = pubMap2.get(registerId);
            if (publisher2 == null
                || PublisherDigestUtil.getDigestValue(publisher1) != PublisherDigestUtil
                    .getDigestValue(publisher2)) {
                list.add(publisher1);
            }
        }
        return list;
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
        return DatumSnapshotRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}
