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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.NodeServerOperateInfo;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : NodeOperatingService.java, v 0.1 2022年01月05日 15:08 xiaojian.xj Exp $
 */
public class NodeOperatingService {

  @Autowired protected ProvideDataService provideDataService;

  public NodeOperatingService() {}

  public NodeOperatingService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
  }

  public Tuple<Long, NodeServerOperateInfo> queryOperateInfoAndVersion() {
    DBResponse<PersistenceData> response =
        provideDataService.queryProvideData(ValueConstants.NODE_SERVER_OPERATING_DATA_ID);

    if (response.getOperationStatus() == OperationStatus.NOTFOUND) {
      return null;
    }

    String entityData = PersistenceDataBuilder.getEntityData(response.getEntity());
    NodeServerOperateInfo read = JsonUtils.read(entityData, NodeServerOperateInfo.class);
    return new Tuple<>(response.getEntity().getVersion(), read);
  }

  /**
   * Setter method for property <tt>provideDataService</tt>.
   *
   * @param provideDataService value to be assigned to property provideDataService
   */
  @VisibleForTesting
  public void setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
  }
}
