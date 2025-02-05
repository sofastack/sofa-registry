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

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.BatchRequest;
import com.alipay.sofa.registry.common.model.dataserver.ClientOffPublisher;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.UnPublisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.change.DataChangeType;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

public class BatchPutDataHandler extends AbstractDataHandler<BatchRequest> {
  private static final Logger LOGGER = DataLog.PUT_LOGGER;
  @Autowired private ThreadPoolExecutor publishProcessorExecutor;

  @Override
  public void checkParam(BatchRequest request) {
    checkSessionProcessId(request.getSessionProcessId());
    for (Object req : request.getRequest()) {
      if (req instanceof Publisher) {
        checkPublisher((Publisher) req);
      } else if (req instanceof ClientOffPublisher) {
        ParaCheckUtil.checkNotNull(
            ((ClientOffPublisher) req).getConnectId(), "ClientOffPublisher.connectId");
        ParaCheckUtil.checkNotNull(
            ((ClientOffPublisher) req).getPublisherMap(), "ClientOffPublisher.publisherMap");
      } else {
        throw new IllegalArgumentException("unsupported item in batch:" + req);
      }
    }
  }

  @Override
  public Object doHandle(Channel channel, BatchRequest request) {
    final ProcessId sessionProcessId = request.getSessionProcessId();
    processSessionProcessId(channel, sessionProcessId);

    return handleRequest(request, sessionProcessId);
  }

  public SlotAccessGenericResponse<Object> handleRequest(
      BatchRequest request, ProcessId sessionProcessId) {
    final SlotAccess slotAccess =
        checkAccess(
            dataServerConfig.getLocalDataCenter(),
            request.getSlotId(),
            request.getSlotTableEpoch(),
            request.getSlotLeaderEpoch());
    if (slotAccess.isMoved() || slotAccess.isMisMatch()) {
      // only reject the when moved
      return SlotAccessGenericResponse.failedResponse(slotAccess);
    }
    final String slotIdStr = String.valueOf(request.getSlotId());
    final Set<String> changeDataInfoIds = Sets.newHashSetWithExpectedSize(128);
    try {
      for (Object req : request.getRequest()) {
        // contains publisher and unPublisher
        if (req instanceof Publisher) {
          Publisher publisher = (Publisher) req;
          DatumVersion updatedVersion = doHandle(publisher);
          if (updatedVersion != null) {
            changeDataInfoIds.add(publisher.getDataInfoId());
          }
          if (publisher instanceof UnPublisher) {
            LOGGER.info(
                "unpub,{},{},{},{},{},{}",
                slotIdStr,
                publisher.getDataInfoId(),
                publisher.getRegisterId(),
                publisher.getVersion(),
                publisher.getRegisterTimestamp(),
                updatedVersion);
          } else {
            LOGGER.info(
                "pub,{},{},{},{},{},{}",
                slotIdStr,
                publisher.getDataInfoId(),
                publisher.getRegisterId(),
                publisher.getVersion(),
                publisher.getRegisterTimestamp(),
                updatedVersion);
          }
        } else if (req instanceof ClientOffPublisher) {
          ClientOffPublisher clientOff = (ClientOffPublisher) req;
          Map<String, DatumVersion> updatedVersion = doHandle(clientOff, sessionProcessId);
          changeDataInfoIds.addAll(updatedVersion.keySet());
          for (Map.Entry<String, Map<String, RegisterVersion>> e :
              clientOff.getPublisherMap().entrySet()) {
            final String dataInfoId = e.getKey();
            for (Map.Entry<String, RegisterVersion> ver : e.getValue().entrySet()) {
              RegisterVersion version = ver.getValue();
              LOGGER.info(
                  "off,{},{},{},{},{},{}",
                  slotIdStr,
                  dataInfoId,
                  ver.getKey(),
                  version.getVersion(),
                  version.getRegisterTimestamp(),
                  updatedVersion.get(dataInfoId));
            }
          }
        } else {
          throw new IllegalArgumentException("unsupported item in batch:" + req);
        }
      }
    } finally {
      // if has exception, try to notify the req which was handled
      if (!changeDataInfoIds.isEmpty()) {
        dataChangeEventCenter.onChange(
            changeDataInfoIds, DataChangeType.PUT, dataServerConfig.getLocalDataCenter());
      }
    }

    return SlotAccessGenericResponse.successResponse(slotAccess, null);
  }

  private DatumVersion doHandle(Publisher publisher) {
    publisher = Publisher.internPublisher(publisher);
    if (publisher.getPublishType() == PublishType.TEMPORARY) {
      // create datum for the temp publisher, we need the datum.version for check ver
      datumStorageDelegate.createEmptyDatumIfAbsent(
          dataServerConfig.getLocalDataCenter(), publisher.getDataInfoId());
      // temporary only notify session, not store
      dataChangeEventCenter.onTempPubChange(publisher, dataServerConfig.getLocalDataCenter());
      return null;
    }
    return datumStorageDelegate.putPublisher(dataServerConfig.getLocalDataCenter(), publisher);
  }

  public Map<String, DatumVersion> doHandle(
      ClientOffPublisher request, ProcessId sessionProcessId) {
    Map<String, Map<String, RegisterVersion>> publisherMap = request.getPublisherMap();
    Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(publisherMap.size());
    for (Map.Entry<String, Map<String, RegisterVersion>> e : publisherMap.entrySet()) {
      DatumVersion version =
          datumStorageDelegate.removePublishers(
              dataServerConfig.getLocalDataCenter(), e.getKey(), sessionProcessId, e.getValue());
      if (version != null) {
        ret.put(e.getKey(), version);
      }
    }
    return ret;
  }

  @Override
  public Class interest() {
    return BatchRequest.class;
  }

  @Override
  public Executor getExecutor() {
    return publishProcessorExecutor;
  }
}
