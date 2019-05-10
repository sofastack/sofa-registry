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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionOpenResource.java, v 0.1 2018-03-21 11:06 shangyu.wh Exp $
 */
@Path("digest")
public class SessionDigestResource {

    /**
     * store subscribers
     */
    @Autowired
    private Interests           sessionInterests;

    /**
     * store watchers
     */
    @Autowired
    private Watchers            sessionWatchers;

    /**
     * store publishers
     */
    @Autowired
    private DataStore           sessionDataStore;

    @Autowired
    private SessionServerConfig sessionServerConfig;

    private final static String SUB = "SUB";

    private final static String PUB = "PUB";

    private final static String WAT = "WAT";

    @GET
    @Path("{type}/data/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Collection<? extends StoreData>> getSessionDataByDataInfoId(@QueryParam("dataInfoId") String dataInfoId,
                                                                                   @PathParam("type") String type) {
        Map<String, Collection<? extends StoreData>> serverList = new HashMap<>();
        if (dataInfoId != null) {
            Collection<Publisher> publishers = sessionDataStore
                .getStoreDataByDataInfoId(dataInfoId);
            Collection<Subscriber> subscribers = sessionInterests.getInterests(dataInfoId);
            Collection<Watcher> watchers = sessionWatchers.getWatchers(dataInfoId);
            fillServerList(type, serverList, publishers, subscribers, watchers);
        }

        return serverList;
    }

    @POST
    @Path("{type}/connect/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Collection<? extends StoreData>> getSessionDataByConnectId(List<String> connectIds,
                                                                                  final @PathParam("type") String type) {
        Map<String, Collection<? extends StoreData>> serverList = new HashMap<>();

        if (connectIds != null) {
            connectIds.forEach(connectId -> {
                Map pubMap = sessionDataStore.queryByConnectId(connectId);
                Map subMap = sessionInterests.queryByConnectId(connectId);
                Map watcherMap = sessionWatchers.queryByConnectId(connectId);

                Collection<Publisher> publishers =
                        pubMap != null && !pubMap.isEmpty() ? pubMap.values() : new ArrayList<>();
                Collection<Subscriber> subscribers =
                        subMap != null && !subMap.isEmpty() ? subMap.values() : new ArrayList<>();
                Collection<Watcher> watchers =
                        watcherMap != null && !watcherMap.isEmpty() ? watcherMap.values() : new ArrayList<>();
                fillServerList(type, serverList, publishers, subscribers, watchers);
            });
        }

        return serverList;
    }

    @GET
    @Path("/data/count")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSessionDataCount() {
        long countSub = sessionInterests.count();
        long countPub = sessionDataStore.count();
        long countSubW = sessionWatchers.count();

        return String.format("Subscriber count: %s, Publisher count: %s, Watcher count: %s",
            countSub, countPub, countSubW);
    }

    /**
     * return true mean push switch on
     */
    @GET
    @Path("pushSwitch")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getPushSwitch() {
        Map<String, Object> resultMap = new HashMap<>(1);
        resultMap.put("pushSwitch", !sessionServerConfig.isStopPushSwitch() ? "open" : "closed");
        return resultMap;
    }

    @GET
    @Path("getDataInfoIdList")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getDataInfoIdList() {
        Collection<String> ret = new HashSet<>();
        ret.addAll(sessionInterests.getInterestDataInfoIds());
        ret.addAll(sessionDataStore.getStoreDataInfoIds());
        return ret;
    }

    @GET
    @Path("checkSumDataInfoIdList")
    @Produces(MediaType.APPLICATION_JSON)
    public int checkSumDataInfoIdList() {
        return getDataInfoIdList().hashCode();
    }

    private void fillServerList(String type,
                                Map<String, Collection<? extends StoreData>> serverList,
                                Collection<Publisher> publishers,
                                Collection<Subscriber> subscribers, Collection<Watcher> watchers) {
        if (type != null && !type.isEmpty()) {
            String inputType = type.toUpperCase();

            switch (inputType) {
                case PUB:
                    if (!CollectionUtils.isEmpty(publishers)) {
                        serverList.put(PUB, publishers);
                    }
                    break;
                case SUB:
                    if (!CollectionUtils.isEmpty(subscribers)) {
                        serverList.put(SUB, subscribers);
                    }
                    break;
                case WAT:
                    if (!CollectionUtils.isEmpty(watchers)) {
                        serverList.put(WAT, watchers);
                    }
                    break;
                default:
                    if (!CollectionUtils.isEmpty(publishers)) {
                        serverList.put(PUB, publishers);
                    }
                    if (!CollectionUtils.isEmpty(subscribers)) {
                        serverList.put(SUB, subscribers);
                    }
                    if (!CollectionUtils.isEmpty(watchers)) {
                        serverList.put(WAT, watchers);
                    }
                    break;
            }

        } else {
            if (publishers != null) {
                serverList.put(PUB, publishers);
            }
            if (subscribers != null) {
                serverList.put(SUB, subscribers);
            }
            if (watchers != null) {
                serverList.put(WAT, watchers);
            }
        }
    }
}