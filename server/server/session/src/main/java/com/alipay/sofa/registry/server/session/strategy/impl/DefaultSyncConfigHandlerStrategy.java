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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;
import com.alipay.sofa.registry.server.session.strategy.SyncConfigHandlerStrategy;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSyncConfigHandlerStrategy implements SyncConfigHandlerStrategy {
  @Override
  public void handleSyncConfigResponse(SyncConfigResponse syncConfigResponse) {
    List<String> list = Lists.newArrayList(ValueConstants.DEFAULT_DATA_CENTER);
    syncConfigResponse.setAvailableSegments(list);
  }
}
