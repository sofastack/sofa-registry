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

import com.alipay.sofa.registry.common.model.ClientOffPublishers;
import com.alipay.sofa.registry.common.model.dataserver.*;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.UnPublisher;
import com.alipay.sofa.registry.compress.CompressConstants;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.ExchangeCallback;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.task.RejectedDiscardHandler;
import com.alipay.sofa.registry.util.OsUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: DataNode.java, v 0.1 2017-12-01 11:30 shangyu.wh Exp $
 */
public class DataNodeServiceImpl implements DataNodeService {

  @Autowired private NodeExchanger dataNodeExchanger;

  @Autowired private SlotTableCache slotTableCache;

  @Autowired private SessionServerConfig sessionServerConfig;

  private DataWritingEngine dataWritingEngine;

  final RejectedDiscardHandler discardHandler = new RejectedDiscardHandler();
  private final ThreadPoolExecutor callbackExecutor =
      MetricsableThreadPoolExecutor.newExecutor(
          "DataNodeCallback", OsUtils.getCpuCount() * 2, 4096, discardHandler);

  @PostConstruct
  public void init() {
    this.dataWritingEngine =
        new BufferedDataWritingEngine(
            sessionServerConfig.getDataNodeExecutorWorkerSize(),
            sessionServerConfig.getDataNodeExecutorQueueSize(),
            sessionServerConfig.getDataNodeMaxBatchSize(),
            sessionServerConfig,
            slotTableCache,
            dataNodeExchanger);
  }

  @Override
  public void register(final Publisher publisher) {
    dataWritingEngine.submit(publisher);
  }

  @Override
  public void unregister(final Publisher publisher) {
    UnPublisher unPublisher = UnPublisher.of(publisher);
    dataWritingEngine.submit(unPublisher);
  }

  @Override
  public void clientOff(ClientOffPublishers clientOffPublishers) {
    if (clientOffPublishers.isEmpty()) {
      return;
    }
    Map<Integer, ClientOffPublisher> groups = groupBySlot(clientOffPublishers);
    for (Map.Entry<Integer, ClientOffPublisher> group : groups.entrySet()) {
      ClientOffPublisher clientOff = group.getValue();
      dataWritingEngine.submit(clientOff);
    }
  }

  @Override
  public void fetchDataVersion(
      String dataCenter,
      int slotId,
      Map<String, DatumVersion> interests,
      ExchangeCallback<Map<String, DatumVersion>> callback) {
    final Slot slot = getSlot(slotId);
    final String dataNodeIp = slot.getLeader();
    try {
      final GetDataVersionRequest request =
          new GetDataVersionRequest(dataCenter, ServerEnv.PROCESS_ID, slotId, interests);
      request.setSlotTableEpoch(slotTableCache.getEpoch());
      request.setSlotLeaderEpoch(slot.getLeaderEpoch());
      final CallbackHandler handler =
          new CallbackHandler() {
            @Override
            public void onCallback(Channel channel, Object message) {
              handleFetchDataVersionCallback(
                  channel, message, slotId, dataNodeIp, dataCenter, callback);
            }

            @Override
            public void onException(Channel channel, Throwable exception) {
              callback.onException(channel, exception);
            }

            @Override
            public Executor getExecutor() {
              return callbackExecutor;
            }
          };
      Request<GetDataVersionRequest> getDataVersionRequestRequest =
          new SimpleRequest<>(request, getUrl(slot), handler);
      Response response = dataNodeExchanger.request(getDataVersionRequestRequest);
      Response.ResultStatus result = (Response.ResultStatus) response.getResult();
      if (result != Response.ResultStatus.SUCCESSFUL) {
        throw new RequestException("response not success, status=" + result);
      }
    } catch (RequestException e) {
      throw new RuntimeException(
          StringFormatter.format(
              "GetDataVersion fail {}@{}, slotId={}", dataNodeIp, dataCenter, slotId, e));
    }
  }

  void handleFetchDataVersionCallback(
      Channel channel,
      Object message,
      int slotId,
      String dataNodeIp,
      String dataCenter,
      ExchangeCallback<Map<String, DatumVersion>> callback) {
    SlotAccessGenericResponse<Map<String, DatumVersion>> genericResponse =
        (SlotAccessGenericResponse<Map<String, DatumVersion>>) message;
    if (genericResponse.isSuccess()) {
      Map<String, DatumVersion> map = genericResponse.getData();
      DatumUtils.intern(map);
      callback.onCallback(channel, map);
    } else {
      callback.onException(
          channel,
          new RuntimeException(
              StringFormatter.format(
                  "GetDataVersion failed, {}@{}, slotId={}, access={}, msg:{}",
                  dataNodeIp,
                  dataCenter,
                  slotId,
                  genericResponse.getSlotAccess(),
                  genericResponse.getMessage())));
    }
  }

  @Override
  public SubDatum fetch(String dataInfoId, String dataCenter) {
    String dataNodeIp = null;
    int slotId = -1;
    try {
      final Slot slot = getSlot(dataInfoId);
      dataNodeIp = slot.getLeader();
      slotId = slot.getId();
      GetDataRequest getDataRequest =
          new GetDataRequest(ServerEnv.PROCESS_ID, dataInfoId, dataCenter, slot.getId());
      getDataRequest.setAcceptEncodes(CompressConstants.defaultCompressEncodes);
      getDataRequest.setSlotTableEpoch(slotTableCache.getEpoch());
      getDataRequest.setSlotLeaderEpoch(slot.getLeaderEpoch());
      Request<GetDataRequest> getDataRequestStringRequest =
          new Request<GetDataRequest>() {

            @Override
            public GetDataRequest getRequestBody() {
              return getDataRequest;
            }

            @Override
            public URL getRequestUrl() {
              return getUrl(slot);
            }

            @Override
            public Integer getTimeout() {
              return sessionServerConfig.getDataNodeExchangeForFetchDatumTimeoutMillis();
            }
          };

      Response response = dataNodeExchanger.request(getDataRequestStringRequest);
      Object result = response.getResult();
      SlotAccessGenericResponse<SubDatum> genericResponse =
          (SlotAccessGenericResponse<SubDatum>) result;
      if (genericResponse.isSuccess()) {
        final SubDatum datum = genericResponse.getData();
        if (datum == null) {
          return null;
        }
        return SubDatum.intern(datum);
      } else {
        throw new RuntimeException(
            StringFormatter.format(
                "GetData got fail response {}, {}, {}, slotId={} msg:{}",
                dataNodeIp,
                dataInfoId,
                dataCenter,
                slotId,
                genericResponse.getMessage()));
      }
    } catch (RequestException e) {
      throw new RuntimeException(
          StringFormatter.format(
              "GetData fail {}, {}, {}, slotId={}", dataNodeIp, dataInfoId, dataCenter, slotId),
          e);
    }
  }

  private Slot getSlot(String dataInfoId) {
    final int slotId = slotTableCache.slotOf(dataInfoId);
    Slot slot = slotTableCache.getSlot(slotId);
    if (slot == null) {
      throw new RequestException(
          StringFormatter.format("slot not found for {}, slotId={}", dataInfoId, slotId));
    }
    return slot;
  }

  private Slot getSlot(int slotId) {
    Slot slot = slotTableCache.getSlot(slotId);
    if (slot == null) {
      throw new RequestException(StringFormatter.format("slot not found, slotId={}", slotId));
    }
    return slot;
  }

  private URL getUrl(Slot slot) {
    final String dataIp = slot.getLeader();
    if (StringUtils.isBlank(dataIp)) {
      throw new RequestException(String.format("slot has no leader, slotId=%s", slot));
    }
    return new URL(dataIp, sessionServerConfig.getDataServerPort());
  }

  private Map<Integer, ClientOffPublisher> groupBySlot(ClientOffPublishers clientOffPublishers) {
    List<Publisher> publishers = clientOffPublishers.getPublishers();
    Map<Integer, ClientOffPublisher> ret = Maps.newHashMap();
    for (Publisher publisher : publishers) {
      final String dataInfoId = publisher.getDataInfoId();
      int slotId = slotTableCache.slotOf(dataInfoId);
      ClientOffPublisher request =
          ret.computeIfAbsent(
              slotId, k -> new ClientOffPublisher(dataInfoId, clientOffPublishers.getConnectId()));
      request.addPublisher(publisher);
    }
    return ret;
  }

  private static final class Req {
    final int slotId;
    final Object req;

    Req(int slotId, Object req) {
      this.slotId = slotId;
      this.req = req;
    }
  }
}
