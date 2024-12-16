package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.filter.AuthRestController;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareRestController;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

/**
 * @author huicha
 * @date 2024/12/13
 */
@Path("datainfoid/blacklist")
@AuthRestController
@LeaderAwareRestController
public class DataInfoIDBlacklistResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataInfoIDBlacklistResource.class);

  @Autowired
  private ProvideDataService provideDataService;

  @Autowired
  private ProvideDataNotifier provideDataNotifier;

  @Autowired
  private DefaultCommonConfig defaultCommonConfig;

  @POST
  @Path("add")
  @Produces(MediaType.APPLICATION_JSON)
  public Result addBlackList(@FormParam("dataCenter") String dataCenter,
                             @FormParam("dataId") String dataId,
                             @FormParam("group") String group,
                             @FormParam("instanceId") String instanceId) {
    try {
      return process(dataCenter, dataId, group, instanceId, Operation.ADD);
    } catch (Throwable throwable) {
      LOGGER.error("Save dataid black list exception", throwable);
      return Result.failed("Save dataid black list exception");
    }
  }

  @POST
  @Path("delete")
  @Produces(MediaType.APPLICATION_JSON)
  public Result deleteBlackList(@FormParam("dataCenter") String dataCenter,
                                @FormParam("dataId") String dataId,
                                @FormParam("group") String group,
                                @FormParam("instanceId") String instanceId) {
    try {
      return process(dataCenter, dataId, group, instanceId, Operation.DELETE);
    } catch (Throwable throwable) {
      LOGGER.error("Delete dataid black list exception", throwable);
      return Result.failed("Delete dataid black list exception");
    }
  }

  private Result process(String dataCenter, String dataId, String group, String instanceId, Operation operation) {
    // 1. 参数检查
    // 1.1. 检查 DataCenter 是否就是当前 Meta 的所属 DataCenter
    String clusterId = defaultCommonConfig.getClusterId(TableEnum.PROVIDE_DATA.getTableName(), ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID);
    if (!StringUtils.equals(dataCenter, clusterId)) {
      // 给定的机房不是当前机房，那么拒绝添加黑名单，直接返回
      return Result.failed("Invalid data center");
    }

    // 1.2. 检查要处理的 DataId 以及 Group 是否符合规则
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
    Tuple<PersistenceData, Long> tuple = this.createNewPersistenceData(queryResponse, dataInfo, operation);
    PersistenceData newPersistenceData = tuple.o1;
    Long oldVersion = tuple.o2;
    if (!this.provideDataService.saveProvideData(newPersistenceData, oldVersion)) {
      // 保存失败
      return Result.failed("Save new black list fail");
    }

    // 4. 保存成功则通知 Session 黑名单变化了
    ProvideDataChangeEvent provideDataChangeEvent =
            new ProvideDataChangeEvent(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID,
                    newPersistenceData.getVersion());
    this.provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);

    return Result.success();
  }

  private Tuple<PersistenceData, Long> createNewPersistenceData(DBResponse<PersistenceData> queryResponse, DataInfo dataInfo, Operation operation) {
    OperationStatus operationStatus = queryResponse.getOperationStatus();
    if (OperationStatus.SUCCESS.equals(operationStatus)) {
      // 读取旧数据成功，其格式为 Json 字符串，解析出来
      PersistenceData oldPersistenceData = queryResponse.getEntity();
      String oldBlackListJson = oldPersistenceData.getData();
      Set<String> oldDataIdBlackList = JsonUtils.read(oldBlackListJson, new TypeReference<Set<String>>() {});

      // 添加或删除新的需要拉黑的数据
      if (Operation.ADD.equals(operation)) {
        oldDataIdBlackList.add(dataInfo.getDataInfoId());
      } else {
        oldDataIdBlackList.remove(dataInfo.getDataInfoId());
      }

      // 创建新数据，并返回新数据以及旧数据的版本号
      PersistenceData newPersistenceData = PersistenceDataBuilder
              .createPersistenceData(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID,
                      JsonUtils.writeValueAsString(oldDataIdBlackList));
      return new Tuple<>(newPersistenceData, oldPersistenceData.getVersion());
    } else {
      // 没有旧数据旧直接创建新的，旧数据的版本号设置为 0
      Set<String> dataIdBlackList = new HashSet<>();
      if (Operation.ADD.equals(operation)) {
        dataIdBlackList.add(dataInfo.getDataInfoId());
      }
      PersistenceData newPersistenceData = PersistenceDataBuilder
              .createPersistenceData(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID,
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
  public DataInfoIDBlacklistResource setProvideDataNotifier(ProvideDataNotifier provideDataNotifier) {
    this.provideDataNotifier = provideDataNotifier;
    return this;
  }

  @VisibleForTesting
  public DataInfoIDBlacklistResource setDefaultCommonConfig(DefaultCommonConfig defaultCommonConfig) {
    this.defaultCommonConfig = defaultCommonConfig;
    return this;
  }
}

enum Operation {
  ADD,
  DELETE
}

