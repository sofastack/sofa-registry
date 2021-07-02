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
package com.alipay.sofa.registry.server.data.slot;

import static com.alipay.sofa.registry.server.data.slot.SlotMetrics.SyncLeader;
import static com.alipay.sofa.registry.server.data.slot.SlotMetrics.SyncSession;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestResult;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherResult;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.DataChangeType;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-20 13:56 yuzhi.lyz Exp $
 */
public final class SlotDiffSyncer {
  private static final Logger LOGGER = LoggerFactory.getLogger(SlotDiffSyncer.class);
  private static final Logger DIFF_LOGGER = LoggerFactory.getLogger("SYNC-DIFF");
  private final DataServerConfig dataServerConfig;

  private final DatumStorage datumStorage;
  private final DataChangeEventCenter dataChangeEventCenter;
  private final SessionLeaseManager sessionLeaseManager;

  SlotDiffSyncer(
      DataServerConfig dataServerConfig,
      DatumStorage datumStorage,
      DataChangeEventCenter dataChangeEventCenter,
      SessionLeaseManager sessionLeaseManager) {
    this.dataServerConfig = dataServerConfig;
    this.datumStorage = datumStorage;
    this.dataChangeEventCenter = dataChangeEventCenter;
    this.sessionLeaseManager = sessionLeaseManager;
  }

  DataSlotDiffPublisherResult processSyncPublisherResp(
      int slotId,
      GenericResponse<DataSlotDiffPublisherResult> resp,
      String targetAddress,
      Map<String, DatumSummary> summaryMap) {
    if (resp == null || !resp.isSuccess()) {
      LOGGER.error("DiffPublisherFailed, slotId={} from {}, resp={}", slotId, targetAddress, resp);
      return null;
    }
    DataSlotDiffPublisherResult result = resp.getData();

    // sync from session
    final ProcessId sessionProcessId = result.getSessionProcessId();
    if (sessionProcessId != null) {
      sessionLeaseManager.renewSession(sessionProcessId);
    }

    if (result.isEmpty()) {
      DIFF_LOGGER.info("DiffPublisherEmpty, slotId={} from {}", slotId, targetAddress);
      return result;
    }

    final Set<String> changeDataIds = Sets.newHashSet();
    for (Map.Entry<String, List<Publisher>> e : result.getUpdatedPublishers().entrySet()) {
      // cache the dataInfoId as key
      final String dataInfoId = WordCache.getWordCache(e.getKey());
      final List<Publisher> publishers = e.getValue();
      Publisher.internPublisher(publishers);
      if (datumStorage.put(dataInfoId, publishers) != null) {
        changeDataIds.add(dataInfoId);
      }
    }
    // for sync publishers
    for (Map.Entry<String, List<String>> e : result.getRemovedPublishers().entrySet()) {
      final String dataInfoId = e.getKey();
      final List<String> registerIds = e.getValue();
      final DatumSummary summary = summaryMap.get(dataInfoId);
      if (summary == null) {
        throw new IllegalArgumentException(
            "sync publisher with not exist local DatumSummary:" + dataInfoId);
      }
      Map<String, RegisterVersion> versionMap = summary.getPublisherVersions(registerIds);
      if (datumStorage.remove(dataInfoId, sessionProcessId, versionMap) != null) {
        changeDataIds.add(dataInfoId);
      }
    }
    if (sessionProcessId != null && !changeDataIds.isEmpty()) {
      // only trigger event when sync from session
      dataChangeEventCenter.onChange(
          changeDataIds, DataChangeType.SYNC, dataServerConfig.getLocalDataCenter());
    }
    DIFF_LOGGER.info(
        "DiffPublisher, slotId={} from {}, updatedP {}:{}, removedP {}:{}",
        slotId,
        targetAddress,
        result.getUpdatedPublishers().size(),
        result.getUpdatedPublishersCount(),
        result.getRemovedPublishersCount(),
        result.getRemovedPublishers().size());
    return result;
  }

  boolean syncPublishers(
      int slotId,
      String targetAddress,
      ClientSideExchanger exchanger,
      long slotTableEpoch,
      Map<String, DatumSummary> summaryMap,
      int maxPublishers,
      SyncContinues continues,
      boolean syncSession) {
    // need the empty dataInfoId to add updatePublisher
    Map<String, DatumSummary> round = pickSummaries(summaryMap, maxPublishers);
    // sync for the existing dataInfoIds.publisher
    while (!summaryMap.isEmpty()) {
      if (!continues.continues()) {
        LOGGER.info("syncing publishers break, slotId={} from {}", slotId, targetAddress);
        return true;
      }
      // maybe to many publishers, spit round
      if (syncSession) {
        SyncSession.observeSyncSessionPub(slotId, DatumSummary.countPublisherSize(round.values()));
      } else {
        SyncLeader.observeSyncLeaderPub(slotId, DatumSummary.countPublisherSize(round.values()));
      }
      DataSlotDiffPublisherRequest request =
          new DataSlotDiffPublisherRequest(slotTableEpoch, slotId, round.values());

      GenericResponse<DataSlotDiffPublisherResult> resp =
          (GenericResponse<DataSlotDiffPublisherResult>)
              exchanger.requestRaw(targetAddress, request).getResult();
      DataSlotDiffPublisherResult result =
          processSyncPublisherResp(slotId, resp, targetAddress, round);
      if (result == null) {
        return false;
      }
      if (!result.isHasRemain()) {
        // the sync round has finish, enter next round
        round.keySet().forEach(d -> summaryMap.remove(d));
        round = pickSummaries(summaryMap, maxPublishers);
      } else {
        // has remain, remove the synced dataInfoIds, enter next round
        Set<String> synced = result.syncDataInfoIds();
        for (String dataInfoId : synced) {
          round.remove(dataInfoId);
          summaryMap.remove(dataInfoId);
        }
      }
    }
    return true;
  }

  boolean sync(
      int slotId,
      String targetAddress,
      ClientSideExchanger exchanger,
      long slotTableEpoch,
      String summaryTargetIp,
      int maxPublishers,
      SyncContinues continues,
      Map<String, DatumSummary> summaryMap) {
    final boolean syncSession = summaryTargetIp != null;
    if (syncSession) {
      SyncSession.observeSyncSessionId(slotId, summaryMap.size());
    } else {
      SyncLeader.observeSyncLeaderId(slotId, summaryMap.size());
    }
    Map<String, DatumDigest> digestMap = PublisherDigestUtil.digest(summaryMap);
    DataSlotDiffDigestRequest request =
        new DataSlotDiffDigestRequest(slotTableEpoch, slotId, digestMap);
    Response exchangeResp = exchanger.requestRaw(targetAddress, request);
    GenericResponse<DataSlotDiffDigestResult> resp =
        (GenericResponse<DataSlotDiffDigestResult>) exchangeResp.getResult();
    DataSlotDiffDigestResult result =
        processSyncDigestResp(slotId, resp, targetAddress, summaryMap);
    if (result == null) {
      return false;
    }
    if (result.getUpdateAndAddSize() == 0) {
      // no change of update and add
      return true;
    }
    final Map<String, DatumSummary> newSummaryMap = getSummaryForSyncPublishers(result, summaryMap);
    return syncPublishers(
        slotId,
        targetAddress,
        exchanger,
        slotTableEpoch,
        newSummaryMap,
        maxPublishers,
        continues,
        syncSession);
  }

  static Map<String, DatumSummary> getSummaryForSyncPublishers(
      DataSlotDiffDigestResult result, Map<String, DatumSummary> digestSummaryMap) {
    final Map<String, DatumSummary> newSummaryMap =
        Maps.newHashMapWithExpectedSize(result.getUpdateAndAddSize());
    for (String add : result.getAddedDataInfoIds()) {
      newSummaryMap.put(add, new DatumSummary(add));
    }
    for (String update : result.getUpdatedDataInfoIds()) {
      newSummaryMap.put(update, digestSummaryMap.get(update));
    }
    return newSummaryMap;
  }

  DataSlotDiffDigestResult processSyncDigestResp(
      int slotId,
      GenericResponse<DataSlotDiffDigestResult> resp,
      String targetAddress,
      Map<String, DatumSummary> summaryMap) {
    if (resp == null || !resp.isSuccess()) {
      LOGGER.error("DiffDigestFailed, slotId={} from {}, resp={}", slotId, targetAddress, resp);
      return null;
    }
    DataSlotDiffDigestResult result = resp.getData();

    // sync from session
    final ProcessId sessionProcessId = result.getSessionProcessId();
    if (sessionProcessId != null) {
      sessionLeaseManager.renewSession(sessionProcessId);
    }

    if (result.isEmpty()) {
      DIFF_LOGGER.info("DiffDigestEmpty, slotId={} from {}", slotId, targetAddress);
      return result;
    }
    // do nothing with added dataInfoId, the added publishers would sync and update by
    // sync.publisher
    // if we create a new empty datum when absent, it's dangerous.
    // if some not expect error occurs when sync publisher and no publisher write to datum,
    // it maybe trigger a empty push with bigger datum.version which created by new empty
    final Set<String> changeDataIds = Sets.newHashSet();
    for (String removeDataInfoId : result.getRemovedDataInfoIds()) {
      if (datumStorage.remove(
              removeDataInfoId,
              sessionProcessId,
              summaryMap.get(removeDataInfoId).getPublisherVersions())
          != null) {
        changeDataIds.add(removeDataInfoId);
      }
    }

    if (sessionProcessId != null && !changeDataIds.isEmpty()) {
      // only trigger event when sync from session
      dataChangeEventCenter.onChange(
          changeDataIds, DataChangeType.SYNC, dataServerConfig.getLocalDataCenter());
    }
    DIFF_LOGGER.info(
        "DiffDigest, slotId={} from {}, update={}, add={}, remove={}, adds={}, removes={}",
        slotId,
        targetAddress,
        result.getUpdatedDataInfoIds().size(),
        result.getAddedDataInfoIds().size(),
        result.getRemovedDataInfoIds().size(),
        result.getAddedDataInfoIds(),
        result.getRemovedDataInfoIds());
    return result;
  }

  /**
   * summary == null means can not assembly summary at first(migrating); do
   * getDatumSummary(sessionIp)
   *
   * @param slotId
   * @param sessionIp
   * @param exchanger
   * @param slotTableEpoch
   * @param continues
   * @param summary
   * @return
   * @throws RequestException
   */
  public boolean syncSession(
      int slotId,
      String sessionIp,
      SessionNodeExchanger exchanger,
      long slotTableEpoch,
      SyncContinues continues,
      Map<String, DatumSummary> summary)
      throws RequestException {
    ParaCheckUtil.checkNotBlank(sessionIp, "sessionIp");

    // summary == null means can not assembly summary before(eg:migrating);
    // can not change to CollectionUtils.isEmpty
    if (summary == null) {
      Map<String, Map<String, DatumSummary>> datumSummary =
          datumStorage.getDatumSummary(slotId, Collections.singleton(sessionIp));
      summary = datumSummary.get(sessionIp);
    }

    return sync(
        slotId,
        sessionIp,
        exchanger,
        slotTableEpoch,
        sessionIp,
        dataServerConfig.getSlotSyncPublisherDigestMaxNum(),
        continues,
        summary);
  }

  public boolean syncSlotLeader(
      int slotId,
      String slotLeaderIp,
      DataNodeExchanger exchanger,
      long slotTableEpoch,
      SyncContinues continues)
      throws RequestException {
    ParaCheckUtil.checkNotBlank(slotLeaderIp, "slotLeaderIp");
    Map<String, DatumSummary> summary = datumStorage.getDatumSummary(slotId);
    return sync(
        slotId,
        slotLeaderIp,
        exchanger,
        slotTableEpoch,
        null,
        dataServerConfig.getSlotSyncPublisherDigestMaxNum(),
        continues,
        summary);
  }

  static Map<String, DatumSummary> pickSummaries(Map<String, DatumSummary> syncSummaries, int n) {
    Map<String, DatumSummary> m = new HashMap<>();
    for (Map.Entry<String, DatumSummary> e : syncSummaries.entrySet()) {
      // at least pick one
      m.put(e.getKey(), e.getValue());
      int versionSize = e.getValue().getPublisherVersions().size();
      if (versionSize == 0) {
        // is empty, budget=1
        versionSize = 1;
      }
      n -= versionSize;
      if (n <= 0) {
        break;
      }
    }
    return m;
  }

  @VisibleForTesting
  DatumStorage getDatumStorage() {
    return datumStorage;
  }

  @VisibleForTesting
  DataServerConfig getDataServerConfig() {
    return dataServerConfig;
  }
}
