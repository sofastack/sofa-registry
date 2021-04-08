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

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.metaserver.FetchProvideDataRequest;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handle session node's query request, such as get ProvideData by dataInfoId
 *
 * @author shangyu.wh
 * @version $Id: GetNodesRequestHandler.java, v 0.1 2018-03-02 15:12 shangyu.wh Exp $
 */
public class FetchProvideDataRequestHandler extends BaseMetaServerHandler<FetchProvideDataRequest> {

  private static final Logger DB_LOGGER =
      LoggerFactory.getLogger(FetchProvideDataRequestHandler.class, "[DBService]");

  @Autowired private ProvideDataService provideDataService;

  @Override
  public Object doHandle(Channel channel, FetchProvideDataRequest fetchProvideDataRequest) {
    try {
      DBResponse<PersistenceData> ret =
          provideDataService.queryProvideData(fetchProvideDataRequest.getDataInfoId());

      if (ret.getOperationStatus() == OperationStatus.SUCCESS) {
        PersistenceData data = ret.getEntity();
        ProvideData provideData =
            new ProvideData(
                new ServerDataBox(data.getData()),
                fetchProvideDataRequest.getDataInfoId(),
                data.getVersion());
        DB_LOGGER.info("get ProvideData {} from DB success!", provideData);
        return provideData;
      } else if (ret.getOperationStatus() == OperationStatus.NOTFOUND) {
        ProvideData provideData =
            new ProvideData(null, fetchProvideDataRequest.getDataInfoId(), null);
        DB_LOGGER.warn(
            "has not found data from DB dataInfoId:{}", fetchProvideDataRequest.getDataInfoId());
        return provideData;
      } else {
        DB_LOGGER.error("get Data DB status error!");
        throw new RuntimeException("Get Data DB status error!");
      }

    } catch (Exception e) {
      DB_LOGGER.error(
          "get persistence Data dataInfoId {} from db error!",
          fetchProvideDataRequest.getDataInfoId());
      throw new RuntimeException("Get persistence Data from db error!", e);
    }
  }

  @Override
  public Class interest() {
    return FetchProvideDataRequest.class;
  }
}
