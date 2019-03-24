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
package com.alipay.sofa.registry.server.data.resource;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.data.cache.DatumCache;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataDigestResource.java, v 0.1 2018-10-19 16:16 shangyu.wh Exp $
 */
@Path("digest")
public class DataDigestResource {

    @GET
    @Path("datum/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Datum> getDatumByDataInfoId(@QueryParam("dataId") String dataId,
                                                   @QueryParam("group") String group,
                                                   @QueryParam("instanceId") String instanceId,
                                                   @QueryParam("dataCenter") String dataCenter) {
        Map<String, Datum> retList = new HashMap<>();

        if (!isBlank(dataId) && !isBlank(instanceId) && !isBlank(group)) {

            String dataInfoId = DataInfo.toDataInfoId(dataId, instanceId, group);
            if (isBlank(dataCenter)) {
                retList = DatumCache.get(dataInfoId);
            } else {
                retList.put(dataCenter, DatumCache.get(dataCenter, dataInfoId));
            }

        }
        return retList;
    }

    @POST
    @Path("connect/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, Publisher>> getPublishersByConnectId(Map<String, String> map) {
        Map<String, Map<String, Publisher>> ret = new HashMap<>();
        if (map != null && !map.isEmpty()) {
            map.forEach((ip, port) -> {
                String connectId = NetUtil.genHost(ip, Integer.valueOf(port));
                if (!connectId.isEmpty()) {
                    Map<String, Publisher> publisherMap = DatumCache.getByHost(connectId);
                    if (publisherMap != null && !publisherMap.isEmpty()) {
                        ret.put(connectId, publisherMap);
                    }
                }
            });
        }
        return ret;
    }

    @GET
    @Path("datum/count")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDatumCount() {
        StringBuilder sb = new StringBuilder("CacheDigest");
        try {

            Map<String, Map<String, Datum>> allMap = DatumCache.getAll();
            if (!allMap.isEmpty()) {
                for (Entry<String, Map<String, Datum>> dataCenterEntry : allMap.entrySet()) {
                    String dataCenter = dataCenterEntry.getKey();
                    Map<String, Datum> datumMap = dataCenterEntry.getValue();
                    sb.append(String.format(" [Datum] size of datum in %s is %s",
                            dataCenter, datumMap.size()));
                    int pubCount = datumMap.values().stream().map(Datum::getPubMap)
                            .filter(map -> map != null && !map.isEmpty()).mapToInt(Map::size).sum();
                    sb.append(String.format(",[Publisher] size of publisher in %s is %s",
                            dataCenter, pubCount));
                }
            } else {
                sb.append(" datum cache is empty");
            }

        } catch (Throwable t) {
            sb.append(" cache digest error!");
        }

        return sb.toString();
    }

    private boolean isBlank(String dataInfoId) {
        return dataInfoId == null || dataInfoId.isEmpty();
    }
}