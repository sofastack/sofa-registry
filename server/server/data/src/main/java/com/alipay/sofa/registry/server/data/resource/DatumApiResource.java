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

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.BatchRequest;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.UnPublisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.google.common.collect.Lists;
import java.util.*;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author kezhu.wukz
 * @version $Id: DatumApiResource.java, v 0.1 2019-07-01 16:16 kezhu.wukz Exp $
 */
@Path("datum/api")
public class DatumApiResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatumApiResource.class);

  private static final ProcessId TEMP_SESSION_PROCESSID = ServerEnv.PROCESS_ID;

  @Autowired DataServerConfig dataServerConfig;

  @Autowired DatumStorageDelegate datumStorageDelegate;

  @Autowired AbstractServerHandler batchPutDataHandler;

  @Autowired SlotManager slotManager;

  /**
   * curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP"}' -H
   * "Content-Type: application/json" -X POST http://localhost:9622/datum/api/get
   *
   * @param datumParam datumParam
   * @return CommonResponse
   */
  @POST
  @Path("/get")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse get(DatumParam datumParam) {
    try {
      validateAndCorrect(datumParam);
    } catch (RuntimeException e) {
      LOGGER.error(e.getMessage(), e);
      return CommonResponse.buildFailedResponse(e.getMessage());
    }

    Datum datum = datumStorageDelegate.get(datumParam.getDataCenter(), datumParam.getDataInfoId());
    if (datum == null) {
      return getNotFoundResponse(datumParam);
    }

    return createResponse(datum);
  }

  @GET
  @Path("/summary/query")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Integer> summary(String dataCenter) {
    if (StringUtils.isBlank(dataCenter)) {
      dataCenter = dataServerConfig.getLocalDataCenter();
    }

    Map<String, Map<String, Integer>> pubCount = datumStorageDelegate.getLocalPubCount();
    return pubCount.get(dataCenter);
  }

  /**
   * curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP"}' -H
   * "Content-Type: application/json" -X POST http://localhost:9622/datum/api/delete
   *
   * @param datumParam datumParam
   * @return CommonResponse
   */
  @POST
  @Path("/delete")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse delete(DatumParam datumParam) {
    try {
      checkTestApiEnable();
      validateAndCorrect(datumParam);
    } catch (RuntimeException e) {
      LOGGER.error(e.getMessage(), e);
      return CommonResponse.buildFailedResponse(e.getMessage());
    }

    Datum datum = datumStorageDelegate.get(datumParam.getDataCenter(), datumParam.getDataInfoId());
    if (datum == null) {
      return getNotFoundResponse(datumParam);
    }
    datumStorageDelegate.cleanLocal(datumParam.getDataCenter(), datumParam.getDataInfoId());
    // get the newly datum
    datum = datumStorageDelegate.get(datumParam.getDataCenter(), datumParam.getDataInfoId());
    return createResponse(datum);
  }

  /**
   * curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP",
   * "publisherDataBox":"1234", "publisherCell":"TestZone2", "publisherConnectId":"127.0.0.1:80"} '
   * -H "Content-Type: application/json" -X POST http://localhost:9622/datum/api/pub/add curl -i -d
   * '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP", "publisherDataBox":"1234",
   * "publisherCell":"TestZone2"} ' -H "Content-Type: application/json" -X POST
   * http://localhost:9622/datum/api/pub/add "publisherConnectId" can be unspecified. If not
   * specified, the connectId of the first pub under the datum can be automatically selected (for
   * testing purposes, it is generally not necessary to specify the connectId artificially).
   *
   * @param datumParam datumParam
   * @return CommonResponse
   */
  @POST
  @Path("/pub/add")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse addPub(DatumParam datumParam) {
    try {
      checkTestApiEnable();
      validateAndCorrect(datumParam);

      // build pub
      Datum datum =
          datumStorageDelegate.get(datumParam.getDataCenter(), datumParam.getDataInfoId());
      if (datum == null) {
        return getNotFoundResponse(datumParam);
      }
      Publisher publisher = buildPublisher(datum, datumParam);

      // build request and invoke
      final int slotId = slotManager.slotOf(publisher.getDataInfoId());
      final Slot slot = slotManager.getSlot(dataServerConfig.getLocalDataCenter(), slotId);
      BatchRequest batchRequest =
          new BatchRequest(
              publisher.getSessionProcessId(), slotId, Collections.singletonList(publisher));
      batchRequest.setSlotTableEpoch(slotManager.getSlotTableEpoch());
      batchRequest.setSlotLeaderEpoch(slot.getLeaderEpoch());
      batchPutDataHandler.doHandle(null, batchRequest);
      // get the newly datum
      datum = datumStorageDelegate.get(datumParam.getDataCenter(), datumParam.getDataInfoId());
      return createResponse(datum);
    } catch (RuntimeException e) {
      LOGGER.error(e.getMessage(), e);
      return CommonResponse.buildFailedResponse(e.getMessage());
    }
  }

  /**
   * curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP",
   * "publisherRegisterId":"98de0e41-2a4d-44d7-b1f7-c520660657e8"} ' -H "Content-Type:
   * application/json" -X POST http://localhost:9622/datum/api/pub/delete
   *
   * @param datumParam datumParam
   * @return CommonResponse
   */
  @POST
  @Path("/pub/delete")
  @Produces(MediaType.APPLICATION_JSON)
  public CommonResponse deletePub(DatumParam datumParam) {
    try {
      checkTestApiEnable();
      validateAndCorrect(datumParam);

      // build pub
      Datum datum =
          datumStorageDelegate.get(datumParam.getDataCenter(), datumParam.getDataInfoId());
      if (datum == null) {
        return getNotFoundResponse(datumParam);
      }
      UnPublisher publisher = buildUnPublisher(datum, datumParam);

      // build request and invoke
      final int slotId = slotManager.slotOf(publisher.getDataInfoId());
      final Slot slot = slotManager.getSlot(dataServerConfig.getLocalDataCenter(), slotId);
      BatchRequest batchRequest =
          new BatchRequest(
              publisher.getSessionProcessId(), slotId, Collections.singletonList(publisher));
      batchRequest.setSlotTableEpoch(slotManager.getSlotTableEpoch());
      batchRequest.setSlotLeaderEpoch(slot.getLeaderEpoch());
      batchPutDataHandler.doHandle(null, batchRequest);
      // get the newly datum
      datum = datumStorageDelegate.get(datumParam.getDataCenter(), datumParam.getDataInfoId());
      return createResponse(datum);
    } catch (RuntimeException e) {
      LOGGER.error(e.getMessage(), e);
      return CommonResponse.buildFailedResponse(e.getMessage());
    }
  }

  /**
   * curl -d '{"dataCenter":"registry-cloud-test-b"}' -H "Content-Type: application/json"
   * http://localhost:9622/datum/api/getDatumVersions
   *
   * @param datumParam datumParam
   * @return Object
   */
  @POST
  @Path("/getDatumVersions")
  @Produces(MediaType.APPLICATION_JSON)
  public Object getDatumVersions(DatumParam datumParam) {
    Map<String, Long> datumVersions = _getDatumVersions(datumParam);

    return datumVersions;
  }

  private Map<String, Long> _getDatumVersions(DatumParam datumParam) {
    Map<String, Long> datumVersions = new HashMap<>();
    if (dataServerConfig.isLocalDataCenter(datumParam.getDataCenter())) {
      Map<String, Datum> localDatums =
          datumStorageDelegate.getAll(dataServerConfig.getLocalDataCenter());
      return DatumUtils.getVersions(localDatums);
    } else {
      // TODO need support remote datecenter
      LOGGER.error("unsupport remote datacenter, {}", datumParam.getDataCenter());
    }
    return datumVersions;
  }

  /**
   * curl -d '{"dataCenter":"registry-cloud-test-b"}' -H "Content-Type: application/json"
   * http://localhost:9622/datum/api/getDatumVersions
   *
   * @param datumParam datumParam
   * @return Object
   */
  @POST
  @Path("/getRemoteDatumVersions")
  @Produces(MediaType.APPLICATION_JSON)
  public Object getRemoteDatumVersions(DatumParam datumParam) {
    throw new UnsupportedOperationException();
  }

  /**
   * curl -H "Content-Type: application/json" http://localhost:9622/datum/api/getDatumSizes
   *
   * @return Object
   */
  @GET
  @Path("/getDatumSizes")
  @Produces(MediaType.APPLICATION_JSON)
  public Object getDatumSizes() {
    Map<String, Integer> datumSizes = new HashMap<>();

    Map<String, Datum> localDatums =
        datumStorageDelegate.getAll(dataServerConfig.getLocalDataCenter());
    int localDatumSize = localDatums.size();
    datumSizes.put(dataServerConfig.getLocalDataCenter(), localDatumSize);
    // TODO remote cluster
    return datumSizes;
  }

  /**
   * curl -d '{"dataCenter":"registry-cloud-test-b",
   * "dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP"}' -H "Content-Type:
   * application/json" http://localhost:9622/datum/api/getDatumVersion
   *
   * @param datumParam datumParam
   * @return Object
   */
  @POST
  @Path("/getDatumVersion")
  @Produces(MediaType.APPLICATION_JSON)
  public Object getDatumVersion(DatumParam datumParam) {
    String format = "dataCenter:%s, dataInfoId:%s, dataServer:%s, version:%s";
    String dataServer = null;
    Long version = null;
    if (dataServerConfig.isLocalDataCenter(datumParam.getDataCenter())) {
      Map<String, Datum> localDatums =
          datumStorageDelegate.getAll(dataServerConfig.getLocalDataCenter());
      Datum datum = localDatums.get(datumParam.getDataInfoId());
      if (datum != null) {
        version = datum.getVersion();
      }
    } else {
      // TODO unsupport remote cluster
      version = null;
    }

    return String.format(
        format, datumParam.getDataCenter(), datumParam.getDataInfoId(), dataServer, version);
  }

  private CommonResponse getNotFoundResponse(DatumParam datumParam) {
    return CommonResponse.buildFailedResponse(
        String.format(
            "datum not found by dataCenter=%s, dataInfoId=%s",
            datumParam.getDataCenter(), datumParam.getDataInfoId()));
  }

  private void validateAndCorrect(DatumParam datumParam) {
    Validate.notNull(datumParam, "datumParam is null");
    Validate.notEmpty(datumParam.getDataInfoId(), "datumParam.dataInfoId is empty");
    if (StringUtils.isBlank(datumParam.getDataCenter())) {
      datumParam.setDataCenter(dataServerConfig.getLocalDataCenter());
    }
  }

  Publisher buildPublisher(Datum datum, DatumParam datumParam) {
    Validate.notNull(datumParam.getPublisherRegisterId(), "datumParam.publisherRegisterId is null");
    Validate.notNull(datumParam.getPublisherCell(), "datumParam.publisherCell is null");
    Validate.notNull(datumParam.getPublisherDataBox(), "datumParam.publisherDataBox is null");

    Publisher publisher = new Publisher();
    publisher.setSessionProcessId(TEMP_SESSION_PROCESSID);
    publisher.setDataInfoId(datumParam.getDataInfoId());
    publisher.setRegisterId(datumParam.getPublisherRegisterId());
    publisher.setRegisterTimestamp(datumParam.getPublisherRegisterTimestamp());
    publisher.setVersion(datumParam.getPublisherVersion());
    if (datumParam.getPublisherConnectId() != null) {
      String[] parts = datumParam.getPublisherConnectId().split(ValueConstants.CONNECT_ID_SPLIT);
      if (parts.length < 2) {
        throw new IllegalArgumentException(
            "datumParam.publisherConnectId format invalid, should be clientIp:clientPort_sessionIp:sessionPort");
      }
      publisher.setSourceAddress(URL.valueOf(parts[0]));
      publisher.setTargetAddress(URL.valueOf(parts[1]));
    } else {
      Collection<Publisher> publishers = datum.getPubMap().values();
      if (publishers.size() == 0) {
        throw new IllegalArgumentException(
            "datumParam.publisherConnectId is null, and datum has no pubs");
      }
      Publisher firstPub = publishers.iterator().next();
      publisher.setSourceAddress(firstPub.getSourceAddress());
      publisher.setTargetAddress(firstPub.getTargetAddress());
    }
    publisher.setCell(datumParam.getPublisherCell());
    ServerDataBox dataBox = new ServerDataBox(datumParam.getPublisherDataBox());
    List<ServerDataBox> dataList = Lists.newArrayList(dataBox);
    publisher.setDataList(dataList);
    return publisher;
  }

  private UnPublisher buildUnPublisher(Datum datum, DatumParam datumParam) {
    Validate.notNull(datumParam.getPublisherRegisterId(), "datumParam.publisherRegisterId is null");

    UnPublisher publisher =
        new UnPublisher(
            datumParam.getDataInfoId(),
            TEMP_SESSION_PROCESSID,
            datumParam.getPublisherRegisterId(),
            datumParam.getPublisherRegisterTimestamp(),
            datumParam.getPublisherVersion());

    Publisher _publisher = datum.getPubMap().get(datumParam.getPublisherRegisterId());
    if (_publisher == null) {
      throw new IllegalArgumentException(
          String.format("No pub of registerId(%s)", datumParam.getPublisherRegisterId()));
    }
    publisher.setSourceAddress(_publisher.getSourceAddress());
    publisher.setTargetAddress(_publisher.getTargetAddress());

    return publisher;
  }

  private void checkTestApiEnable() {
    Validate.isTrue(dataServerConfig.isEnableTestApi(), "This api is disabled");
  }

  private CommonResponse createResponse(Datum datum) {
    return CommonResponse.buildSuccessResponse(
        String.format("datum=%s, publishers=%s", datum, datum.getPubMap()));
  }
}
