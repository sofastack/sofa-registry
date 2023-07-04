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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.server.shared.util.PersistenceDataParser;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : FetchStopPushService.java, v 0.1 2022年07月21日 15:00 xiaojian.xj Exp $
 */
public class FetchStopPushService {

  @Autowired private ProvideDataService provideDataService;

  public boolean isStopPush() {
    DBResponse<PersistenceData> stopPushResp =
        provideDataService.queryProvideData(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    return PersistenceDataParser.parse2BoolIgnoreCase(stopPushResp, false);
  }

  /**
   * Setter method for property <tt>provideDataService</tt>.
   *
   * @param provideDataService value to be assigned to property provideDataService
   * @return FetchStopPushService
   */
  @VisibleForTesting
  public FetchStopPushService setProvideDataService(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
    return this;
  }
}
