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
package com.alipay.sofa.registry.server.session.renew;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.store.DataStore;

/**
 *
 * @author kezhu.wukz
 * @version $Id: DefaultRenewService.java, v 0.1 2019-06-27 11:09 kezhu.wukz Exp $
 */
public class DefaultRenewService implements RenewService {

    /*** store publishers */
    @Autowired
    private DataStore   sessionDataStore;

    /*** calculate data node url */
    @Autowired
    private NodeManager dataNodeManager;

    @Override
    public List<RenewDatumRequest> getRenewDatumRequests(String connectId) {
        List<DatumSnapshotRequest> datumSnapshotRequests = getDatumSnapshotRequests(connectId);
        if (datumSnapshotRequests != null && !datumSnapshotRequests.isEmpty()) {
            return datumSnapshotRequests.stream()
                    .map(datumSnapshotRequest -> new RenewDatumRequest(datumSnapshotRequest.getConnectId(),
                            datumSnapshotRequest.getDataServerIp(), String.valueOf(
                            PublisherDigestUtil.getDigestValueSum(datumSnapshotRequest.getPublishers()))))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<DatumSnapshotRequest> getDatumSnapshotRequests(String connectId) {
        Map<String, Publisher> pubMap = sessionDataStore.queryByConnectId(connectId);
        if (pubMap != null && !pubMap.isEmpty()) {
            Map<String, List<Publisher>> dataServerIpToPubs = new ConcurrentHashMap<>();
            List<DatumSnapshotRequest> list = new ArrayList<>();
            pubMap.values().forEach(publisher -> {
                Node dataNode = dataNodeManager.getNode(publisher.getDataInfoId());
                List<Publisher> publishers = dataServerIpToPubs
                        .computeIfAbsent(dataNode.getNodeUrl().getIpAddress(), k -> new ArrayList<>());
                publishers.add(publisher);
            });
            for (Map.Entry<String, List<Publisher>> entry : dataServerIpToPubs.entrySet()) {
                List<Publisher> publishers = entry.getValue();
                if (!publishers.isEmpty()) {
                    DatumSnapshotRequest datumSnapshotRequest = new DatumSnapshotRequest(connectId, entry.getKey(),
                            publishers);
                    list.add(datumSnapshotRequest);
                }
            }
            return list;
        }
        return null;
    }
}