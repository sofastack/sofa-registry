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

import static com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.HandlerMetrics.GetData.GET_DATUM_N_COUNTER;
import static com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.HandlerMetrics.GetData.GET_DATUM_Y_COUNTER;
import static com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.HandlerMetrics.GetData.GET_PUBLISHER_COUNTER;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.compress.CompressUtils;
import com.alipay.sofa.registry.compress.Compressor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.data.providedata.CompressDatumService;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : BaseGetDataHandler.java, v 0.1 2022年06月20日 16:02 xiaojian.xj Exp $
 */
public abstract class BaseGetDataHandler<T> extends AbstractDataHandler<T> {

  private static final Logger LOGGER = DataLog.GET_LOGGER;

  @Autowired protected ThreadPoolExecutor getDataProcessorExecutor;

  @Resource private CompressDatumService compressDatumService;

  @Override
  public Executor getExecutor() {
    return getDataProcessorExecutor;
  }

  protected SlotAccessGenericResponse<SubDatum> processSingleDataCenter(
      String dataCenter,
      String dataInfoId,
      long slotTableEpoch,
      long slotLeaderEpoch,
      String[] acceptEncodes) {
    final SlotAccess slotAccessBefore =
        checkAccess(dataCenter, dataInfoId, slotTableEpoch, slotLeaderEpoch);
    if (!slotAccessBefore.isAccept()) {
      GET_DATUM_N_COUNTER.inc();
      return buildResponse(false, slotAccessBefore, null, "slotAccess not accept.");
    }

    final Datum datum = datumStorageDelegate.get(dataCenter, dataInfoId);
    // important. double check the slot access. avoid the case:
    // 1. the slot is leader, the first check pass
    // 2. slot moved and data cleaned
    // 3. get datum, but null after cleaned, dangerous!!
    // 3.1. session get datum by change.version, ignored null datum, would not push
    // 3.2. session get datum by subscriber.register, accept null datum(the pub may not exists) and
    // push empty
    // so, need a double check slot access, make sure the slot's leader not change in the getting
    final SlotAccess slotAccessAfter =
        checkAccess(dataCenter, dataInfoId, slotTableEpoch, slotLeaderEpoch);
    if (slotAccessAfter.getSlotLeaderEpoch() != slotAccessBefore.getSlotLeaderEpoch()) {
      // the slot's leader has change
      GET_DATUM_N_COUNTER.inc();
      return buildResponse(
          false, slotAccessAfter, null, "slotLeaderEpoch has change, prev=" + slotAccessBefore);
    }

    GET_DATUM_Y_COUNTER.inc();

    SubDatum zipDatum = zipDatum(dataInfoId, dataCenter, datum, acceptEncodes);

    return buildResponse(true, slotAccessAfter, zipDatum, "");
  }

  private SubDatum zipDatum(
      String dataInfoId, String dataCenter, Datum datum, String[] acceptEncodes) {
    // return SubDatum, it's serdeSize and memoryOverhead much smaller than Datum
    SubDatum subDatum = datum != null ? DatumUtils.of(datum) : null;

    Compressor compressor = compressDatumService.getCompressor(subDatum, acceptEncodes);

    SubDatum zipDatum = DatumUtils.compressSubDatum(subDatum, compressor);

    String encode = "";
    if (compressor != null) {
      encode = compressor.getEncoding();
    }

    if (subDatum != null) {
      LOGGER.info(
          "getD,{},{},{},{},encode={},dataBoxSize={},encodeSize={}",
          dataInfoId,
          dataCenter,
          subDatum.mustGetPublishers().size(),
          subDatum.getVersion(),
          CompressUtils.normalizeEncode(encode),
          zipDatum.getDataBoxBytes(),
          zipDatum.size());
      GET_PUBLISHER_COUNTER.inc(subDatum.mustGetPublishers().size());
    } else {
      LOGGER.info("getNilD,{},{}", dataInfoId, dataCenter);
    }

    return zipDatum;
  }

  private SlotAccessGenericResponse<SubDatum> buildResponse(
      boolean success, SlotAccess slotAccess, SubDatum subDatum, String msg) {
    return new SlotAccessGenericResponse<>(success, msg, slotAccess, subDatum);
  }

  /**
   * Getter method for property <tt>compressDatumService</tt>.
   *
   * @return property value of compressDatumService
   */
  @VisibleForTesting
  public CompressDatumService getCompressDatumService() {
    return compressDatumService;
  }

  /**
   * Setter method for property <tt>compressDatumService</tt>.
   *
   * @param compressDatumService value to be assigned to property compressDatumService
   * @return BaseGetDataHandler
   */
  @VisibleForTesting
  public BaseGetDataHandler setCompressDatumService(CompressDatumService compressDatumService) {
    this.compressDatumService = compressDatumService;
    return this;
  }
}
