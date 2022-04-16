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
package com.alipay.sofa.registry.server.session.registry;

import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.ExchangeCallback;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : RegistryScanCallable.java, v 0.1 2022年06月25日 11:59 xiaojian.xj Exp $
 */
public class RegistryScanCallable {

  protected static final Logger SCAN_VER_LOGGER = LoggerFactory.getLogger("SCAN-VER");

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private SlotTableCache slotTableCache;

  /** transfer data to DataNode */
  @Autowired private DataNodeService dataNodeService;

  public void scanVersions(
      long round,
      String dataCenter,
      Map<String, DatumVersion> interestVersions,
      ScanCallable callable) {
    Map<Integer, Map<String, DatumVersion>> interestVersionsGroup = groupBySlot(interestVersions);

    Map<Integer, FetchVersionResult> resultMap =
        Maps.newHashMapWithExpectedSize(interestVersions.size());
    for (Map.Entry<Integer, Map<String, DatumVersion>> group : interestVersionsGroup.entrySet()) {
      final Integer slotId = group.getKey();
      try {
        final FetchVersionResult result =
            fetchDataVersionAsync(dataCenter, slotId, group.getValue(), round);
        if (result != null) {
          resultMap.put(slotId, result);
        }
      } catch (Throwable e) {
        SCAN_VER_LOGGER.info(
            "[fetchSlotVer]round={},{},{},leader={},interests={},gets={},success={}",
            round,
            slotId,
            dataCenter,
            slotTableCache.getLeader(dataCenter, slotId),
            interestVersions.size(),
            0,
            "N");

        SCAN_VER_LOGGER.error(
            "round={}, failed to fetch versions slotId={}, size={}",
            round,
            slotId,
            group.getValue().size(),
            e);
      }
    }
    final int timeoutMillis = sessionServerConfig.getDataNodeExchangeTimeoutMillis();
    final long waitDeadline = System.currentTimeMillis() + timeoutMillis + 2000;
    // wait async finish
    ConcurrentUtils.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
    // check callback result, use for.count to avoid the clock skew
    for (int i = 0; i < (timeoutMillis * 2) / 50; i++) {
      handleFetchResult(round, dataCenter, resultMap, callable);
      if (resultMap.isEmpty() || System.currentTimeMillis() > waitDeadline) {
        break;
      }
      ConcurrentUtils.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
    }
    handleFetchResult(round, dataCenter, resultMap, callable);
    if (!resultMap.isEmpty()) {
      SCAN_VER_LOGGER.error(
          "[fetchSlotVerTimeout]round={},callbacks={},{}", round, resultMap.size(), resultMap);
    }
  }

  int handleFetchResult(
      long round,
      String dataCenter,
      Map<Integer, FetchVersionResult> resultMap,
      ScanCallable callable) {
    int count = 0;
    final Iterator<Entry<Integer, FetchVersionResult>> it = resultMap.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<Integer, FetchVersionResult> e = it.next();
      FetchVersionResult result = e.getValue();
      if (result.callback == null) {
        // not finish
        continue;
      }
      it.remove();
      count++;
      // success
      if (result.callback.versions != null) {
        final long now = System.currentTimeMillis();

        // execute
        result.callback.versions.forEach(
            (dataInfoId, version) ->
                callable.execute(
                    new ScanCallableInfo(
                        round, dataCenter, e.getKey(), result.leader, dataInfoId, version, now)));
      }
    }
    return count;
  }

  private Map<Integer, Map<String, DatumVersion>> groupBySlot(
      Map<String, DatumVersion> interestVersions) {
    if (CollectionUtils.isEmpty(interestVersions)) {
      return Collections.emptyMap();
    }
    TreeMap<Integer, Map<String, DatumVersion>> ret = Maps.newTreeMap();
    for (Map.Entry<String, DatumVersion> interestVersion : interestVersions.entrySet()) {
      final String dataInfoId = interestVersion.getKey();
      Map<String, DatumVersion> map =
          ret.computeIfAbsent(
              slotTableCache.slotOf(dataInfoId), k -> Maps.newHashMapWithExpectedSize(256));
      map.put(dataInfoId, interestVersion.getValue());
    }
    return ret;
  }

  private static final class FetchVersionResult {
    final String leader;
    final int slotId;
    volatile FetchVersionCallback callback;

    FetchVersionResult(int slotId, String leader) {
      this.leader = leader;
      this.slotId = slotId;
    }

    @Override
    public String toString() {
      return StringFormatter.format(
          "FetchResult{slotId={},{},finish={}}", slotId, leader, callback != null);
    }
  }

  private static final class FetchVersionCallback {
    final Map<String, DatumVersion> versions;

    FetchVersionCallback(Map<String, DatumVersion> versions) {
      this.versions = versions;
    }
  }

  FetchVersionResult fetchDataVersionAsync(
      String dataCenter, int slotId, Map<String, DatumVersion> interestVersions, long round) {
    final String leader = slotTableCache.getLeader(dataCenter, slotId);
    if (StringUtils.isBlank(leader)) {
      SCAN_VER_LOGGER.error("[NoLeader]slotId={}, round={}", slotId, round);
      return null;
    }
    final FetchVersionResult result = new FetchVersionResult(slotId, leader);
    dataNodeService.fetchDataVersion(
        dataCenter,
        slotId,
        interestVersions,
        new ExchangeCallback<Map<String, DatumVersion>>() {
          @Override
          public void onCallback(Channel channel, Map<String, DatumVersion> message) {
            // merge the version
            Map<String, DatumVersion> mergedVersions = new HashMap<>(interestVersions);
            mergedVersions.putAll(message);
            result.callback = new FetchVersionCallback(mergedVersions);
            SCAN_VER_LOGGER.info(
                "[fetchSlotVer]round={},{},{},leader={},interests={},gets={},success={}",
                round,
                slotId,
                dataCenter,
                leader,
                interestVersions.size(),
                message.size(),
                "Y");
          }

          @Override
          public void onException(Channel channel, Throwable e) {
            result.callback = new FetchVersionCallback(null);
            SCAN_VER_LOGGER.info(
                "[fetchSlotVer]round={},{},{},leader={},interests={},gets={},success={}",
                round,
                slotId,
                dataCenter,
                leader,
                interestVersions.size(),
                0,
                "N");

            SCAN_VER_LOGGER.error(
                "round={},failed to fetch versions,slotId={},leader={},size={}",
                round,
                slotId,
                leader,
                interestVersions.size(),
                e);
          }
        });
    return result;
  }

  public interface ScanCallable {
    void execute(ScanCallableInfo callableInfo);
  }

  public final class ScanCallableInfo {
    private final long round;
    private final String dataCenter;
    private final int slotId;
    private final String leader;

    private final String dataInfoId;

    private final DatumVersion version;
    private final long currentTs;

    public ScanCallableInfo(
        long round,
        String dataCenter,
        int slotId,
        String leader,
        String dataInfoId,
        DatumVersion version,
        long currentTs) {
      this.round = round;
      this.dataCenter = dataCenter;
      this.slotId = slotId;
      this.leader = leader;
      this.dataInfoId = dataInfoId;
      this.version = version;
      this.currentTs = currentTs;
    }

    /**
     * Getter method for property <tt>round</tt>.
     *
     * @return property value of round
     */
    public long getRound() {
      return round;
    }

    /**
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
      return dataCenter;
    }

    /**
     * Getter method for property <tt>slotId</tt>.
     *
     * @return property value of slotId
     */
    public int getSlotId() {
      return slotId;
    }

    /**
     * Getter method for property <tt>leader</tt>.
     *
     * @return property value of leader
     */
    public String getLeader() {
      return leader;
    }

    /**
     * Getter method for property <tt>dataInfoId</tt>.
     *
     * @return property value of dataInfoId
     */
    public String getDataInfoId() {
      return dataInfoId;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public DatumVersion getVersion() {
      return version;
    }

    /**
     * Getter method for property <tt>currentTs</tt>.
     *
     * @return property value of currentTs
     */
    public long getCurrentTs() {
      return currentTs;
    }
  }
}
