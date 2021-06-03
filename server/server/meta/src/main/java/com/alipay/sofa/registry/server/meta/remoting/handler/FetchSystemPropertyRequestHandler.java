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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.CLIENT_OFF_PODS_DATA_ID;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyRequest;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.provide.data.ClientManagerService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Handle session node's query request
 *
 * @author xiaojian.xj
 * @version $Id: FetchSystemPropertyRequestHandler.java, v 0.1 2021-05-06 15:12 xiaojian.xj Exp $
 */
public class FetchSystemPropertyRequestHandler
    extends BaseMetaServerHandler<FetchSystemPropertyRequest> {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(FetchSystemPropertyRequestHandler.class, "[DBService]");

  @Autowired private ProvideDataService provideDataService;

  @Autowired private ClientManagerService clientManagerService;

  @Override
  public void checkParam(FetchSystemPropertyRequest request) {
    Assert.isTrue(request != null, "get system data request is null.");
    Assert.isTrue(
        StringUtils.isNotEmpty(request.getDataInfoId()),
        "get system data request dataInfoId is empty.");
  }

  @Override
  public Object doHandle(Channel channel, FetchSystemPropertyRequest request) {
    try {
      DB_LOGGER.info("get system data {}", request);

      if (CLIENT_OFF_PODS_DATA_ID.equals(request.getDataInfoId())) {
        return fetchClientOffSet(request);
      } else {
        return fetchSystemData(request);
      }
    } catch (Exception e) {
      DB_LOGGER.error("get system data {} from db error!", request.getDataInfoId(), e);
      throw new RuntimeException("Get system data from db error!", e);
    }
  }

  private Object fetchSystemData(FetchSystemPropertyRequest request) {
    DBResponse<PersistenceData> ret = provideDataService.queryProvideData(request.getDataInfoId());
    OperationStatus status = ret.getOperationStatus();
    PersistenceData persistenceData = ret.getEntity();

    if (status == OperationStatus.SUCCESS) {
      ProvideData data =
          new ProvideData(
              new ServerDataBox(persistenceData.getData()),
              request.getDataInfoId(),
              persistenceData.getVersion());

      FetchSystemPropertyResult result;
      if (data.getVersion() > request.getVersion()) {
        result = new FetchSystemPropertyResult(true, data);
      } else {
        result = new FetchSystemPropertyResult(false);
      }
      if (DB_LOGGER.isInfoEnabled()) {
        DB_LOGGER.info("get SystemProperty {} from DB success!", result);
      }
      return result;
    } else if (status == OperationStatus.NOTFOUND) {
      FetchSystemPropertyResult result = new FetchSystemPropertyResult(false);
      DB_LOGGER.warn("has not found system data from DB dataInfoId:{}", request.getDataInfoId());
      return result;
    } else {
      DB_LOGGER.error("get Data DB status error!");
      throw new RuntimeException("Get Data DB status error!");
    }
  }

  private Object fetchClientOffSet(FetchSystemPropertyRequest request) {
    DBResponse<ProvideData> ret = clientManagerService.queryClientOffSet();
    OperationStatus status = ret.getOperationStatus();
    ProvideData data = ret.getEntity();

    if (status == OperationStatus.SUCCESS) {
      FetchSystemPropertyResult result;
      if (data.getVersion() > request.getVersion()) {
        result = new FetchSystemPropertyResult(true, data);
      } else {
        result = new FetchSystemPropertyResult(false);
      }
      if (DB_LOGGER.isInfoEnabled()) {
        DB_LOGGER.info("get SystemProperty {} from DB success!", result);
      }
      return result;
    } else if (status == OperationStatus.NOTFOUND) {
      FetchSystemPropertyResult result = new FetchSystemPropertyResult(false);
      DB_LOGGER.warn("has not found system data from DB dataInfoId:{}", request.getDataInfoId());
      return result;
    } else {
      DB_LOGGER.error("get Data DB status error!");
      throw new RuntimeException("Get Data DB status error!");
    }
  }

  @Override
  public Class interest() {
    return FetchSystemPropertyRequest.class;
  }
}
