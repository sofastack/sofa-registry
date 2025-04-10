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

import com.alipay.sofa.registry.common.model.dataserver.*;
import com.alipay.sofa.registry.common.model.slot.MultiSlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.store.MultiSubDatumRevisions;
import com.alipay.sofa.registry.common.model.store.SubDatumRevision;
import com.alipay.sofa.registry.common.model.store.SubDatumRevisionData;
import com.alipay.sofa.registry.common.model.store.SubDatumRevisions;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DatumRevisionStorage;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2025/3/12
 */
public class GetMultiSubDatumRevisionsHandler
    extends AbstractDataHandler<GetMultiSubDatumRevisionsRequest> {

  @Autowired private DatumStorageDelegate datumStorageDelegate;

  private final DatumRevisionStorage datumRevisionStorage;

  public GetMultiSubDatumRevisionsHandler() {
    this.datumRevisionStorage = DatumRevisionStorage.getInstance();
  }

  @Override
  public void checkParam(GetMultiSubDatumRevisionsRequest request) {
    super.checkParam(request);
    ParaCheckUtil.checkNotBlank(
        request.getDataInfoId(), "GetMultiSubDatumRevisionsRequest.dataInfoId");
    ParaCheckUtil.checkNotEmpty(
        request.getDataCenters(), "GetMultiSubDatumRevisionsRequest.dataCenters");
    ParaCheckUtil.checkNotEmpty(
        request.getSlotTableEpochs(), "GetMultiSubDatumRevisionsRequest.slotTableEpochs");
    ParaCheckUtil.checkNotEmpty(
        request.getSlotLeaderEpochs(), "GetMultiSubDatumRevisionsRequest.slotLeaderEpochs");

    for (Map.Entry<String, Long> entry : request.getSlotTableEpochs().entrySet()) {
      String dataCenter = entry.getKey();
      ParaCheckUtil.checkNotNull(entry.getValue(), dataCenter + ".slotTableEpoch");
      ParaCheckUtil.checkNotNull(
          request.getSlotLeaderEpochs().get(dataCenter), dataCenter + ".slotLeaderEpoch");
    }

    checkSessionProcessId(request.getSessionProcessId());
  }

  @Override
  public MultiSlotAccessGenericResponse<MultiSubDatumRevisions> doHandle(
      Channel channel, GetMultiSubDatumRevisionsRequest request) {
    this.processSessionProcessId(channel, request.getSessionProcessId());

    List<String> dataCenters = request.getDataCenters();
    int dataCenterSize = dataCenters.size();
    String dataInfoId = request.getDataInfoId();

    Map<String, Long> slotTableEpochs = request.getSlotTableEpochs();
    Map<String, Long> slotLeaderEpochs = request.getSlotLeaderEpochs();

    Map<String, SlotAccess> slotAccessMap = Maps.newHashMapWithExpectedSize(dataCenterSize);
    Map<String, SubDatumRevisions> datumRevisionsMap =
        Maps.newHashMapWithExpectedSize(dataCenterSize);

    boolean success = true;
    StringBuilder errorMessage = new StringBuilder();
    for (String dataCenter : dataCenters) {
      Long slotTableEpoch = slotTableEpochs.get(dataCenter);
      Long slotLeaderEpoch = slotLeaderEpochs.get(dataCenter);

      // 首先检查一次数据是否可读
      final SlotAccess slotAccessBefore =
          checkAccess(dataCenter, dataInfoId, slotTableEpoch, slotLeaderEpoch);
      if (!slotAccessBefore.isAccept()) {
        success = false;
        slotAccessMap.put(dataCenter, slotAccessBefore);
        errorMessage.append(StringFormatter.format("{}:{}.", dataCenter, "slotAccess not accept."));
        continue;
      }

      // 如果可读，那么把需要的数据读取出来
      Datum datum = this.datumStorageDelegate.get(dataCenter, dataInfoId);
      if (null == datum) {
        // 虽然可读，但是也不一定能查询到数据
        MultiSubDatumRevisions multiSubDatumRevisions =
            new MultiSubDatumRevisions(dataInfoId, Collections.emptyMap());
        return new MultiSlotAccessGenericResponse<>(
            success, errorMessage.toString(), multiSubDatumRevisions, slotAccessMap);
      }

      List<DatumRevisionMark> datumRevisionMarks = datum.getDatumRevisionMarks();
      List<DatumRevisionKey> datumRevisionKeys =
          datumRevisionMarks.stream()
              .map(
                  datumRevisionMark ->
                      DatumRevisionKey.of(dataCenter, dataInfoId, datumRevisionMark))
              .collect(Collectors.toList());

      Map<DatumRevisionKey, DatumRevisionData> revisionDatum =
          this.datumRevisionStorage.loadDatumRevisions(datumRevisionKeys);

      // 然后再检查一次数据可读性，如果发生变化了那么证明前面读取出来的数据不可用
      final SlotAccess slotAccessAfter =
          checkAccess(dataCenter, dataInfoId, slotTableEpoch, slotLeaderEpoch);
      slotAccessMap.put(dataCenter, slotAccessAfter);
      if (slotAccessAfter.getSlotLeaderEpoch() != slotAccessBefore.getSlotLeaderEpoch()) {
        success = false;
        errorMessage.append(
            StringFormatter.format(
                "{}:{}.", dataCenter, "slotLeaderEpoch has change, prev=" + slotAccessBefore));
        continue;
      }

      // 读取出来的数据可读，整理
      List<SubDatumRevision> subDatumRevisionList;
      if (CollectionUtils.isEmpty(datumRevisionKeys)) {
        subDatumRevisionList = Collections.emptyList();
      } else {
        // 每一个 DatumRevisionKey 都必须有一条记录，如果 Key 对应的数据缓存丢了，那也要设置为空
        subDatumRevisionList = new ArrayList<>(datumRevisionKeys.size());
        for (DatumRevisionKey datumRevisionKey : datumRevisionKeys) {
          DatumRevisionData datumRevisionData = revisionDatum.get(datumRevisionKey);
          if (null == datumRevisionData) {
            subDatumRevisionList.add(
                SubDatumRevision.fromMissData(dataCenter, dataInfoId, datumRevisionKey.intern()));
          } else {
            subDatumRevisionList.add(
                SubDatumRevision.from(
                    dataCenter,
                    dataInfoId,
                    datumRevisionKey.intern(),
                    SubDatumRevisionData.from(datumRevisionData)));
          }
        }
      }

      SubDatumRevisions subDatumRevisions =
          new SubDatumRevisions(dataCenter, dataInfoId, subDatumRevisionList);

      // 在读取数据期间访问权限未发生变化
      datumRevisionsMap.put(dataCenter, subDatumRevisions);
    }

    MultiSubDatumRevisions multiSubDatumRevisions =
        new MultiSubDatumRevisions(dataInfoId, datumRevisionsMap);
    return new MultiSlotAccessGenericResponse<>(
        success, errorMessage.toString(), multiSubDatumRevisions, slotAccessMap);
  }

  @Override
  public Class interest() {
    return GetMultiSubDatumRevisionsRequest.class;
  }
}
