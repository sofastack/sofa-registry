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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.AuthRestController;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderForwardRestController;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2024/12/13
 */
@Path("datainfoid/blacklist")
@LeaderForwardRestController
public class DataInfoIDBlacklistResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataInfoIDBlacklistResource.class);

  @Autowired private ProvideDataService provideDataService;

  @Autowired private ProvideDataNotifier provideDataNotifier;

  @POST
  @Path("add")
  @Produces(MediaType.APPLICATION_JSON)
  @AuthRestController
  public Result addBlackList(
      @FormParam("dataId") String dataId,
      @FormParam("group") String group,
      @FormParam("instanceId") String instanceId) {
    try {
      return process(dataId, group, instanceId, Operation.ADD);
    } catch (Throwable throwable) {
      LOGGER.error("Save dataid black list exception", throwable);
      return Result.failed("Save dataid black list exception");
    }
  }

  @POST
  @Path("delete")
  @Produces(MediaType.APPLICATION_JSON)
  @AuthRestController
  public Result deleteBlackList(
      @FormParam("dataId") String dataId,
      @FormParam("group") String group,
      @FormParam("instanceId") String instanceId) {
    try {
      return process(dataId, group, instanceId, Operation.DELETE);
    } catch (Throwable throwable) {
      LOGGER.error("Delete dataid black list exception", throwable);
      return Result.failed("Delete dataid black list exception");
    }
  }

  @GET
  @Path("query")
  @Produces(MediaType.APPLICATION_JSON)
  public Result queryBlackList() {
    try {
      DBResponse<PersistenceData> queryResponse =
          this.provideDataService.queryProvideData(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID);
      OperationStatus operationStatus = queryResponse.getOperationStatus();
      if (OperationStatus.SUCCESS.equals(operationStatus)) {
        PersistenceData persistenceData = queryResponse.getEntity();
        Result result = Result.success();
        result.setMessage(persistenceData.getData());
        return result;
      } else {
        return Result.success();
      }
    } catch (Throwable throwable) {
      LOGGER.error("Query dataid black list exception", throwable);
      return Result.failed("Query dataid black list exception");
    }
  }

  private Result process(String dataId, String group, String instanceId, Operation operation) {
    // 1. 参数检查
    //   检查要处理的 DataId 以及 Group 是否符合规则
    DataInfo dataInfo = new DataInfo(instanceId, dataId, group);
    Tuple<Boolean, String> checkResult = this.checkDataInfoId(dataInfo);
    if (!checkResult.o1) {
      // 不符合规则，那么拒绝添加黑名单，直接返回
      return Result.failed("Invalid dataid: " + checkResult.o2);
    }

    // 2. 查询出当前黑名单列表
    DBResponse<PersistenceData> queryResponse =
        this.provideDataService.queryProvideData(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID);

    // 3. 根据操作类型，添加 DataID 到列表中，或者删除列表中的 DataID，并保存
    Tuple<PersistenceData, Long> tuple =
        this.createNewPersistenceData(queryResponse, dataInfo, operation);
    PersistenceData newPersistenceData = tuple.o1;
    Long oldVersion = tuple.o2;
    if (!this.provideDataService.saveProvideData(newPersistenceData, oldVersion)) {
      // 保存失败
      return Result.failed("Save new black list fail");
    }

    // 4. 保存成功则通知 Session 黑名单变化了
    ProvideDataChangeEvent provideDataChangeEvent =
        new ProvideDataChangeEvent(
            ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID, newPersistenceData.getVersion());
    this.provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);

    return Result.success();
  }

  private Tuple<PersistenceData, Long> createNewPersistenceData(
      DBResponse<PersistenceData> queryResponse, DataInfo dataInfo, Operation operation) {
    OperationStatus operationStatus = queryResponse.getOperationStatus();
    if (OperationStatus.SUCCESS.equals(operationStatus)) {
      // 读取旧数据成功，其格式为 Json 字符串，解析出来
      PersistenceData oldPersistenceData = queryResponse.getEntity();
      String oldBlackListJson = oldPersistenceData.getData();
      Set<String> oldDataIdBlackList =
          JsonUtils.read(oldBlackListJson, new TypeReference<Set<String>>() {});

      // 添加或删除新的需要拉黑的数据
      if (Operation.ADD.equals(operation)) {
        oldDataIdBlackList.add(dataInfo.getDataInfoId());
      } else {
        oldDataIdBlackList.remove(dataInfo.getDataInfoId());
      }

      // 创建新数据，并返回新数据以及旧数据的版本号
      PersistenceData newPersistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID,
              JsonUtils.writeValueAsString(oldDataIdBlackList));
      return new Tuple<>(newPersistenceData, oldPersistenceData.getVersion());
    } else {
      // 没有旧数据旧直接创建新的，旧数据的版本号设置为 0
      Set<String> dataIdBlackList = new HashSet<>();
      if (Operation.ADD.equals(operation)) {
        dataIdBlackList.add(dataInfo.getDataInfoId());
      }
      PersistenceData newPersistenceData =
          PersistenceDataBuilder.createPersistenceData(
              ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID,
              JsonUtils.writeValueAsString(dataIdBlackList));
      return new Tuple<>(newPersistenceData, 0L);
    }
  }

  protected Tuple<Boolean, String> checkDataInfoId(DataInfo dataInfo) {
    return new Tuple<>(true, "");
  }

  @VisibleForTesting
  public DataInfoIDBlacklistResource setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }

  @VisibleForTesting
  public DataInfoIDBlacklistResource setProvideDataNotifier(
      ProvideDataNotifier provideDataNotifier) {
    this.provideDataNotifier = provideDataNotifier;
    return this;
  }
}

enum Operation {
  ADD,
  DELETE
}
