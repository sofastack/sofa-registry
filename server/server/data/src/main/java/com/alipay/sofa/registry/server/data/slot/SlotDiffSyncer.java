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
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDigestResult;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherResult;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.DataChangeType;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.multi.cluster.loggers.Loggers;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotMetrics.RemoteSyncLeader;
import com.alipay.sofa.registry.server.data.pubiterator.DatumBiConsumer;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-20 13:56 yuzhi.lyz Exp $
 */
public final class SlotDiffSyncer {
  private final Logger DIFF_LOGGER;
  private final DataServerConfig dataServerConfig;

  private final DatumStorageDelegate datumStorageDelegate;
  private final DataChangeEventCenter dataChangeEventCenter;
  private final SessionLeaseManager sessionLeaseManager;

  private final SyncSlotAcceptorManager syncSlotAcceptorManager;

  public SlotDiffSyncer(
      DataServerConfig dataServerConfig,
      DatumStorageDelegate datumStorageDelegate,
      DataChangeEventCenter dataChangeEventCenter,
      SessionLeaseManager sessionLeaseManager,
      SyncSlotAcceptorManager syncSlotAcceptorManager,
      Logger diffLogger) {
    this.dataServerConfig = dataServerConfig;
    this.datumStorageDelegate = datumStorageDelegate;
    this.dataChangeEventCenter = dataChangeEventCenter;
    this.sessionLeaseManager = sessionLeaseManager;
    this.syncSlotAcceptorManager = syncSlotAcceptorManager;
    this.DIFF_LOGGER = diffLogger;
  }

  DataSlotDiffPublisherResult processSyncPublisherResp(
      boolean syncLocal,
      String syncDataCenter,
      int slotId,
      GenericResponse<DataSlotDiffPublisherResult> resp,
      String targetAddress,
      Map<String, DatumSummary> summaryMap) {
    if (resp == null || !resp.isSuccess()) {
      DIFF_LOGGER.error(
          "DiffPublisherFailed, syncLocal={}, syncDataCenter={}, slotId={} from {}, resp={}",
          syncLocal,
          syncDataCenter,
          slotId,
          targetAddress,
          resp);
      return null;
    }
    DataSlotDiffPublisherResult result = resp.getData();
    final String slotIdStr = String.valueOf(slotId);

    // sync from session
    final ProcessId sessionProcessId = result.getSessionProcessId();
    if (syncLocal && sessionProcessId != null) {
      sessionLeaseManager.renewSession(sessionProcessId);
    }

    if (result.isEmpty()) {
      DIFF_LOGGER.info(
          "DiffPublisherEmpty, syncLocal={}, syncDataCenter={}, slotId={} from {}",
          syncLocal,
          syncDataCenter,
          slotId,
          targetAddress);
      return result;
    }

    final Set<String> changeDataIds = Sets.newHashSet();
    for (Map.Entry<String, List<Publisher>> e : result.getUpdatedPublishers().entrySet()) {
      // cache the dataInfoId as key
      final String dataInfoId = WordCache.getWordCache(e.getKey());
      final List<Publisher> publishers = e.getValue();
      Publisher.internPublisher(publishers);
      DatumVersion datumVersion =
          datumStorageDelegate.putPublisher(syncDataCenter, dataInfoId, publishers);
      if (datumVersion != null) {
        changeDataIds.add(dataInfoId);
      }
      if (!syncLocal) {
        for (Publisher publisher : publishers) {
          Loggers.MULTI_PUT_LOGGER.info(
              "pub,{},{},{},{},{},{},{}",
              syncDataCenter,
              slotIdStr,
              publisher.getDataInfoId(),
              publisher.getRegisterId(),
              publisher.getVersion(),
              publisher.getRegisterTimestamp(),
              datumVersion);
        }
      }
    }
    // for sync publishers
    for (Map.Entry<String, List<String>> e : result.getRemovedPublishers().entrySet()) {
      final String dataInfoId = e.getKey();
      final List<String> registerIds = e.getValue();
      final DatumSummary summary = summaryMap.get(dataInfoId);
      if (summary == null) {
        throw new IllegalArgumentException(
            StringFormatter.format(
                "dataCenter: {} sync publisher with not exist local DatumSummary: {}",
                syncDataCenter,
                dataInfoId));
      }
      Map<String, RegisterVersion> versionMap = summary.getPublisherVersions(registerIds);
      DatumVersion datumVersion =
          datumStorageDelegate.removePublishers(
              syncDataCenter, dataInfoId, sessionProcessId, versionMap);
      if (datumVersion != null) {
        changeDataIds.add(dataInfoId);
      }

      if (!syncLocal) {
        for (Entry<String, RegisterVersion> entry : versionMap.entrySet()) {
          Loggers.MULTI_PUT_LOGGER.info(
              "unpub,{},{},{},{},{},{},{}",
              syncDataCenter,
              slotIdStr,
              dataInfoId,
              entry.getKey(),
              entry.getValue().getVersion(),
              entry.getValue().getRegisterTimestamp(),
              datumVersion);
        }
      }
    }

    triggerDataChange(syncLocal, syncDataCenter, sessionProcessId, changeDataIds);
    DIFF_LOGGER.info(
        "DiffPublisher, synLocal={}, syncDataCenter={}, slotId={} from {}, updatedP {}:{}, removedP {}:{}",
        syncLocal ? 'Y' : 'N',
        syncDataCenter,
        slotId,
        targetAddress,
        result.getUpdatedPublishers().size(),
        result.getUpdatedPublishersCount(),
        result.getRemovedPublishersCount(),
        result.getRemovedPublishers().size());
    return result;
  }

  boolean syncPublishers(
      String localDataCenter,
      boolean syncLocal,
      String syncDataCenter,
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
        DIFF_LOGGER.info("syncing publishers break, slotId={} from {}", slotId, targetAddress);
        return true;
      }
      // maybe to many publishers, spit round
      int pubSize = DatumSummary.countPublisherSize(round.values());
      if (syncSession) {
        SyncSession.observeSyncSessionPub(slotId, pubSize);
      } else if (syncLocal) {
        SyncLeader.observeSyncLeaderPub(slotId, pubSize);
      } else {
        RemoteSyncLeader.observeSyncLeaderPub(syncDataCenter, slotId, pubSize);
      }

      DataSlotDiffPublisherRequest request =
          new DataSlotDiffPublisherRequest(
              localDataCenter, slotTableEpoch, slotId, syncSlotAcceptorManager, round.values());

      GenericResponse<DataSlotDiffPublisherResult> resp =
          (GenericResponse<DataSlotDiffPublisherResult>)
              exchanger.requestRaw(targetAddress, request).getResult();
      DataSlotDiffPublisherResult result =
          processSyncPublisherResp(syncLocal, syncDataCenter, slotId, resp, targetAddress, round);
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
      String localDataCenter,
      String syncDataCenter,
      boolean syncLocal,
      int slotId,
      String targetAddress,
      long slotLeaderEpoch,
      ClientSideExchanger exchanger,
      long slotTableEpoch,
      String summaryTargetIp,
      int maxPublishers,
      SyncContinues continues,
      Map<String, DatumSummary> summaryMap) {
    final boolean syncSession = summaryTargetIp != null;
    if (syncSession) {
      SyncSession.observeSyncSessionId(slotId, summaryMap.size());
    } else if (syncLocal) {
      SyncLeader.observeSyncLeaderId(slotId, summaryMap.size());
    } else {
      RemoteSyncLeader.observeSyncLeaderId(syncDataCenter, slotId, summaryMap.size());
    }
    Map<String, DatumDigest> digestMap = PublisherDigestUtil.digest(summaryMap);
    DataSlotDiffDigestRequest request =
        DataSlotDiffDigestRequest.buildRequest(
            localDataCenter,
            slotTableEpoch,
            slotId,
            slotLeaderEpoch,
            digestMap,
            syncSlotAcceptorManager);
    Response exchangeResp = exchanger.requestRaw(targetAddress, request);
    GenericResponse<DataSlotDiffDigestResult> resp =
        (GenericResponse<DataSlotDiffDigestResult>) exchangeResp.getResult();
    DataSlotDiffDigestResult result =
        processSyncDigestResp(syncLocal, syncDataCenter, slotId, resp, targetAddress, summaryMap);
    if (result == null) {
      return false;
    }
    if (result.getUpdateAndAddSize() == 0) {
      // no change of update and add
      return true;
    }
    final Map<String, DatumSummary> newSummaryMap = getSummaryForSyncPublishers(result, summaryMap);
    return syncPublishers(
        localDataCenter,
        syncLocal,
        syncDataCenter,
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
      boolean syncLocal,
      String syncDataCenter,
      int slotId,
      GenericResponse<DataSlotDiffDigestResult> resp,
      String targetAddress,
      Map<String, DatumSummary> summaryMap) {
    if (resp == null || !resp.isSuccess()) {
      DIFF_LOGGER.error(
          "DiffDigestFailed, syncLocal={}, syncDataCenter={}, slotId={} from {}, resp={}",
          syncLocal,
          syncDataCenter,
          slotId,
          targetAddress,
          resp);
      return null;
    }
    DataSlotDiffDigestResult result = resp.getData();

    // sync from session
    final ProcessId sessionProcessId = result.getSessionProcessId();
    if (sessionProcessId != null) {
      sessionLeaseManager.renewSession(sessionProcessId);
    }

    if (result.isEmpty()) {
      DIFF_LOGGER.info(
          "DiffDigestEmpty, syncLocal={}, syncDataCenter={}, slotId={} from {}",
          syncLocal,
          syncDataCenter,
          slotId,
          targetAddress);
      return result;
    }
    // do nothing with added dataInfoId, the added publishers would sync and update by
    // sync.publisher
    // if we create a new empty datum when absent, it's dangerous.
    // if some not expect error occurs when sync publisher and no publisher write to datum,
    // it maybe trigger a empty push with bigger datum.version which created by new empty
    final Set<String> changeDataIds = Sets.newHashSet();
    for (String removeDataInfoId : result.getRemovedDataInfoIds()) {
      if (datumStorageDelegate.removePublishers(
              syncDataCenter,
              removeDataInfoId,
              sessionProcessId,
              summaryMap.get(removeDataInfoId).getPublisherVersions())
          != null) {
        changeDataIds.add(removeDataInfoId);
      }
    }

    triggerDataChange(syncLocal, syncDataCenter, sessionProcessId, changeDataIds);
    DIFF_LOGGER.info(
        "DiffDigest, syncLocal={}, syncDataCenter={}, slotId={} from {}, update={}, add={}, remove={}, adds={}, removes={}",
        syncLocal ? 'Y' : 'N',
        syncDataCenter,
        slotId,
        targetAddress,
        result.getUpdatedDataInfoIds().size(),
        result.getAddedDataInfoIds().size(),
        result.getRemovedDataInfoIds().size(),
        result.getAddedDataInfoIds(),
        result.getRemovedDataInfoIds());
    return result;
  }

  private void triggerDataChange(
      boolean syncLocal,
      String syncDataCenter,
      ProcessId sessionProcessId,
      Set<String> changeDataIds) {
    if (needTrigger(syncLocal, sessionProcessId) && !changeDataIds.isEmpty()) {
      dataChangeEventCenter.onChange(
          changeDataIds,
          syncLocal ? DataChangeType.SYNC : DataChangeType.SYNC_REMOTE,
          syncDataCenter);
    }
  }

  /**
   * need to trigger change: 1.sync remote data; 2.sync local session;
   *
   * @param syncLocal
   * @param sessionProcessId
   * @return
   */
  private boolean needTrigger(boolean syncLocal, ProcessId sessionProcessId) {
    if (!syncLocal) {
      // sync remote, need to trigger change
      return true;
    }
    // sync local session, need to trigger change
    return sessionProcessId != null;
  }

  /**
   * summary == null means can not assembly summary at first(migrating); do
   * getDatumSummary(sessionIp)
   *
   * @param slotId slotId
   * @param sessionIp sessionIp
   * @param exchanger exchanger
   * @param slotTableEpoch slotTableEpoch
   * @param continues continues
   * @param summary summary
   * @param slotLeaderEpoch slotLeaderEpoch
   * @param syncDataCenter syncDataCenter
   * @return boolean
   * @throws RequestException RequestException
   */
  public boolean syncSession(
      String syncDataCenter,
      int slotId,
      String sessionIp,
      long slotLeaderEpoch,
      ClientSideExchanger exchanger,
      long slotTableEpoch,
      SyncContinues continues,
      Map<String, DatumSummary> summary)
      throws RequestException {
    ParaCheckUtil.checkNotBlank(sessionIp, "sessionIp");

    // summary == null means can not assembly summary before(eg:migrating);
    // can not change to CollectionUtils.isEmpty
    if (summary == null) {
      // SingletonMap unsupported computeIfAbsent
      final Map<String, Map<String, DatumSummary>> datumSummary =
          Maps.newHashMapWithExpectedSize(1);
      datumSummary.put(sessionIp, Maps.newHashMapWithExpectedSize(64));

      datumStorageDelegate.foreach(
          dataServerConfig.getLocalDataCenter(),
          slotId,
          DatumBiConsumer.publisherGroupsBiConsumer(
              datumSummary, Collections.singleton(sessionIp), syncSlotAcceptorManager));
      summary = datumSummary.get(sessionIp);
    }

    return sync(
        syncDataCenter,
        syncDataCenter,
        true,
        slotId,
        sessionIp,
        slotLeaderEpoch,
        exchanger,
        slotTableEpoch,
        sessionIp,
        dataServerConfig.getSlotSyncPublisherDigestMaxNum(),
        continues,
        summary);
  }

  public boolean syncSlotLeader(
      String localDataCenter,
      String syncDataCenter,
      boolean syncLocal,
      int slotId,
      String slotLeaderIp,
      long slotLeaderEpoch,
      ClientSideExchanger exchanger,
      long slotTableEpoch,
      SyncContinues continues)
      throws RequestException {
    ParaCheckUtil.checkNotBlank(slotLeaderIp, "slotLeaderIp");
    Map<String, DatumSummary> summaries = Maps.newHashMap();
    datumStorageDelegate.foreach(
        syncDataCenter,
        slotId,
        DatumBiConsumer.publisherGroupsBiConsumer(summaries, syncSlotAcceptorManager));
    return sync(
        localDataCenter,
        syncDataCenter,
        syncLocal,
        slotId,
        slotLeaderIp,
        slotLeaderEpoch,
        exchanger,
        slotTableEpoch,
        null,
        dataServerConfig.getSlotSyncPublisherDigestMaxNum(),
        continues,
        summaries);
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
  public DatumStorageDelegate getDatumStorageDelegate() {
    return datumStorageDelegate;
  }

  @VisibleForTesting
  public DataServerConfig getDataServerConfig() {
    return dataServerConfig;
  }
}
