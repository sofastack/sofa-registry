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

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.CONNECT_ID_SPLIT;

import com.alipay.sofa.registry.common.model.*;
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.sessionserver.PubSubDataInfoIdRequest;
import com.alipay.sofa.registry.common.model.sessionserver.PubSubDataInfoIdResp;
import com.alipay.sofa.registry.common.model.sessionserver.QueryPublisherRequest;
import com.alipay.sofa.registry.common.model.sessionserver.SimplePublisher;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.ReporterUtils;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.cache.Value;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.FetchPubSubDataInfoIdService;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.OsUtils;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author shangyu.wh
 * @version $Id: SessionOpenResource.java, v 0.1 2018-03-21 11:06 shangyu.wh Exp $
 */
@Path("digest")
public class SessionDigestResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientManagerResource.class);

  /** store subscribers */
  @Autowired private Interests sessionInterests;

  /** store watchers */
  @Autowired private Watchers sessionWatchers;

  /** store publishers */
  @Autowired private DataStore sessionDataStore;

  @Autowired private CacheService sessionDatumCacheService;

  @Autowired private MetaServerService metaNodeService;

  @Autowired private SessionServerConfig sessionServerConfig;

  @Resource private FetchStopPushService fetchStopPushService;

  @Autowired private FetchPubSubDataInfoIdService fetchPubSubDataInfoIdService;

  @Autowired private NodeExchanger sessionConsoleExchanger;

  @Autowired private AppRevisionRepository appRevisionRepository;

  @Autowired private InterfaceAppsRepository interfaceAppsRepository;

  private static final String LOCAL_ADDRESS = NetUtil.getLocalAddress().getHostAddress();

  @Autowired private MetaServerService mataNodeService;

  private static final String SUB = "SUB";

  private static final String PUB = "PUB";

  private static final String WAT = "WAT";

  private static final String SESSION = "SESSION";

  private static final String DATA = "DATA";

  private static final String META = "META";

  private final ThreadPoolExecutor pubSubQueryZoneExecutor =
      MetricsableThreadPoolExecutor.newExecutor(
          "PubSubQueryZoneExecutor",
          OsUtils.getCpuCount(),
          100,
          new ThreadPoolExecutor.CallerRunsPolicy());

  @PostConstruct
  public void init() {
    MetricRegistry metrics = new MetricRegistry();
    metrics.register("pushSwitch", (Gauge<Map>) () -> getPushSwitch());
    ReporterUtils.startSlf4jReporter(60, metrics);
  }

  @GET
  @Path("/metadata/allRevisionIds")
  @Produces(MediaType.APPLICATION_JSON)
  public Set<String> allRevisionIds() {
    return appRevisionRepository.allRevisionIds();
  }

  @GET
  @Path("/metadata/allServiceMapping")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Map<String, InterfaceMapping>> allServiceMapping() {
    return interfaceAppsRepository.allServiceMapping();
  }

  @GET
  @Path("{type}/data/query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Collection<? extends StoreData>> getSessionDataByDataInfoId(
      @QueryParam("dataInfoId") String dataInfoId, @PathParam("type") String type) {
    Map<String, Collection<? extends StoreData>> serverList = new HashMap<>();
    if (dataInfoId != null) {
      Collection<Publisher> publishers = sessionDataStore.getDatas(dataInfoId);
      Collection<Subscriber> subscribers = sessionInterests.getDatas(dataInfoId);
      Collection<Watcher> watchers = sessionWatchers.getDatas(dataInfoId);
      fillServerList(type, serverList, publishers, subscribers, watchers);
    }

    return serverList;
  }

  @POST
  @Path("{type}/connect/query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Collection<? extends StoreData>> getSessionDataByConnectId(
      List<String> queryConnectIds, final @PathParam("type") String type) {
    List<ConnectId> connectIds = new ArrayList<>(queryConnectIds.size());
    for (String queryConnectId : queryConnectIds) {
      String connectId = queryConnectId;
      if (!queryConnectId.contains(CONNECT_ID_SPLIT)) {
        connectId =
            connectId
                + CONNECT_ID_SPLIT
                + LOCAL_ADDRESS
                + ":"
                + sessionServerConfig.getServerPort();
      }
      connectIds.add(ConnectId.parse(connectId));
    }

    Map<String, Collection<? extends StoreData>> serverList = new HashMap<>();

    connectIds.forEach(
        connectId -> {
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

    return serverList;
  }

  @GET
  @Path("/data/queryDetail")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<PubSubDataInfoIdResp> queryDetail(@QueryParam("ips") String ips) {
    HashSet<String> ipSet = Sets.newHashSet(ips.split(","));
    if (CollectionUtils.isEmpty(ipSet)) {
      return new GenericResponse().fillFailed("param ips is empty");
    }
    PubSubDataInfoIdResp localResp = fetchPubSubDataInfoIdService.queryByIps(ipSet);
    List<URL> servers = Sdks.getOtherConsoleServers(null, sessionServerConfig, metaNodeService);

    List<PubSubDataInfoIdResp> resps = Lists.newArrayListWithExpectedSize(servers.size() + 1);
    resps.add(localResp);
    if (servers.size() > 0) {
      Map<URL, CommonResponse> map =
          Sdks.concurrentSdkSend(
              pubSubQueryZoneExecutor,
              servers,
              (URL url) -> {
                final PubSubDataInfoIdRequest req = new PubSubDataInfoIdRequest(ipSet);
                return (CommonResponse)
                    sessionConsoleExchanger.request(new SimpleRequest(req, url)).getResult();
              },
              5000);

      for (Entry<URL, CommonResponse> entry : map.entrySet()) {
        if (entry.getValue() instanceof GenericResponse) {
          GenericResponse response = (GenericResponse) entry.getValue();
          if (response.isSuccess()) {
            resps.add((PubSubDataInfoIdResp) response.getData());
            continue;
          }
        }
        LOGGER.error(
            "url={} query pub and sub dataInfoIds fail, msg:{}.",
            entry.getKey().getIpAddress(),
            entry.getValue());
      }
    }
    return new GenericResponse<PubSubDataInfoIdResp>().fillSucceed(merge(resps));
  }

  protected PubSubDataInfoIdResp merge(List<PubSubDataInfoIdResp> resps) {
    if (resps.size() == 1) {
      return resps.get(0);
    }

    Map<String, Set<String>> pubDataInfoIds = Maps.newHashMap();
    Map<String, Set<String>> subDataInfoIds = Maps.newHashMap();
    for (PubSubDataInfoIdResp resp : resps) {
      if (!CollectionUtils.isEmpty(resp.getPubDataInfoIds())) {
        for (Entry<String, Set<String>> pubEntry : resp.getPubDataInfoIds().entrySet()) {
          Set<String> set =
              pubDataInfoIds.computeIfAbsent(pubEntry.getKey(), k -> Sets.newHashSet());
          set.addAll(pubEntry.getValue());
        }
      }

      if (!CollectionUtils.isEmpty(resp.getSubDataInfoIds())) {
        for (Entry<String, Set<String>> subEntry : resp.getSubDataInfoIds().entrySet()) {
          Set<String> set =
              subDataInfoIds.computeIfAbsent(subEntry.getKey(), k -> Sets.newHashSet());
          set.addAll(subEntry.getValue());
        }
      }
    }
    return new PubSubDataInfoIdResp(pubDataInfoIds, subDataInfoIds);
  }

  @GET
  @Path("/data/count")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSessionDataCount() {
    Tuple<Long, Long> countSub = sessionInterests.count();
    Tuple<Long, Long> countPub = sessionDataStore.count();
    Tuple<Long, Long> countSubW = sessionWatchers.count();

    return String.format(
        "Subscriber count: %s, Publisher count: %s, Watcher count: %s",
        countSub.o2, countPub.o2, countSubW.o2);
  }

  @GET
  @Path("pushSwitch")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getPushSwitch() {
    Map<String, Object> resultMap = new HashMap<>(1);
    resultMap.put("pushSwitch", !fetchStopPushService.isStopPushSwitch() ? "open" : "closed");
    return resultMap;
  }

  @GET
  @Path("getDataInfoIdList")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<String> getDataInfoIdList() {
    Collection<String> ret = new HashSet<>();
    ret.addAll(sessionInterests.getDataInfoIds());
    ret.addAll(sessionDataStore.getDataInfoIds());
    return ret;
  }

  @GET
  @Path("{type}/getDataInfoIdListGroupByCount")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Map.Entry<String, Integer>> getDataInfoIdListGroupByCount(
      final @PathParam("type") String type) {
    List<Map.Entry<String, Integer>> list = new ArrayList<>();
    String inputType = type.toUpperCase();
    switch (inputType) {
      case PUB:
        sessionDataStore
            .getDatas()
            .forEach((key, value) -> list.add(new AbstractMap.SimpleEntry<>(key, value.size())));
        break;
      case WAT:
        sessionWatchers
            .getDatas()
            .forEach((key, value) -> list.add(new AbstractMap.SimpleEntry<>(key, value.size())));
        break;
      default:
        sessionInterests
            .getDatas()
            .forEach((key, value) -> list.add(new AbstractMap.SimpleEntry<>(key, value.size())));
        break;
    }
    list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
    return list;
  }

  @GET
  @Path("checkSumDataInfoIdList")
  @Produces(MediaType.APPLICATION_JSON)
  public int checkSumDataInfoIdList() {
    return getDataInfoIdList().hashCode();
  }

  private void fillServerList(
      String type,
      Map<String, Collection<? extends StoreData>> serverList,
      Collection<Publisher> publishers,
      Collection<Subscriber> subscribers,
      Collection<Watcher> watchers) {
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

  @GET
  @Path("{type}/serverList/query")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getServerListAll(@PathParam("type") String type) {
    List<String> serverList = new ArrayList<>();
    if (type != null && !type.isEmpty()) {
      String inputType = type.toUpperCase();

      switch (inputType) {
        case SESSION:
          serverList = getSessionServerList();
          break;
        case DATA:
          serverList = getDataServerList();
          break;
        case META:
          serverList = getMetaServerLeader();
          break;
        default:
          serverList = new ArrayList<>();
          break;
      }
    }
    return serverList;
  }

  public List<String> getSessionServerList() {
    return mataNodeService.getSessionServerList(sessionServerConfig.getSessionServerRegion());
  }

  public List<String> getDataServerList() {
    return new ArrayList<>(mataNodeService.getDataServerList());
  }

  public List<String> getMetaServerLeader() {
    return Lists.newArrayList(mataNodeService.getMetaServerLeader());
  }

  @GET
  @Path("/data/zone/queryPublisher")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<List<SimplePublisher>> queryZonePublisher(
      @QueryParam("dataInfoId") String dataInfoId) {
    Collection<Publisher> publishers = this.sessionDataStore.getDatas(dataInfoId);
    List<SimplePublisher> allPublishers =
        publishers.stream().map(PublisherUtils::convert).collect(Collectors.toList());

    List<URL> otherSessions =
        Sdks.getOtherConsoleServers(null, this.sessionServerConfig, this.metaNodeService);
    if (!CollectionUtils.isEmpty(otherSessions)) {
      Map<URL, CommonResponse> respMap =
          Sdks.concurrentSdkSend(
              pubSubQueryZoneExecutor,
              otherSessions,
              (URL url) -> {
                final QueryPublisherRequest req = new QueryPublisherRequest(dataInfoId);
                return (CommonResponse)
                    sessionConsoleExchanger.request(new SimpleRequest(req, url)).getResult();
              },
              5000);

      for (Entry<URL, CommonResponse> entry : respMap.entrySet()) {
        CommonResponse response = entry.getValue();
        if (response instanceof GenericResponse) {
          GenericResponse<List<SimplePublisher>> genericResponse =
              (GenericResponse<List<SimplePublisher>>) response;
          if (genericResponse.isSuccess()) {
            List<SimplePublisher> subPublishers = genericResponse.getData();
            allPublishers.addAll(subPublishers);
          } else {
            LOGGER.error(
                "url={} query publishers fail, response:{}.",
                entry.getKey().getIpAddress(),
                entry.getValue());
          }
        } else {
          LOGGER.error(
              "url={} query publishers fail, unexpect response type, response:{}.",
              entry.getKey().getIpAddress(),
              entry.getValue());
        }
      }
    }
    return new GenericResponse<List<SimplePublisher>>().fillSucceed(allPublishers);
  }

  @GET
  @Path("/data/queryPublisher")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<List<SimplePublisher>> queryPublisher(
      @QueryParam("dataInfoId") String dataInfoId) {
    try {
      if (StringUtils.isBlank(dataInfoId)) {
        return new GenericResponse<List<SimplePublisher>>()
            .fillFailed("data info id can not be empty");
      }

      String dataCenter = sessionServerConfig.getSessionServerDataCenter();
      Key key =
          new Key(
              DatumKey.class.getName(),
              new DatumKey(dataInfoId, Collections.singleton(dataCenter)));
      Value value = sessionDatumCacheService.getValue(key);
      MultiSubDatum datum = (MultiSubDatum) value.getPayload();
      Map<String, SubDatum> datumMap = datum.getDatumMap();

      List<SimplePublisher> result = new ArrayList<>();
      for (SubDatum subDatum : datumMap.values()) {
        List<SubPublisher> publishers = subDatum.mustGetPublishers();
        for (SubPublisher publisher : publishers) {
          SimplePublisher simplePublisher =
              new SimplePublisher(publisher.getClientId(), publisher.getSrcAddressString(), "");
          result.add(simplePublisher);
        }
      }
      return new GenericResponse<List<SimplePublisher>>().fillSucceed(result);
    } catch (Throwable throwable) {
      LOGGER.error("Query publisher from data exception", throwable);
      return new GenericResponse<List<SimplePublisher>>()
          .fillFailed("query publisher from data fail");
    }
  }
}
