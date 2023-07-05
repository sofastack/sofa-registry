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
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version : PersistenceParser.java, v 0.1 2021年10月27日 14:27 xiaojian.xj Exp $
 */
public class PersistenceDataParser {

  public static boolean parse2BoolIgnoreCase(
      PersistenceData persistenceData, boolean defaultValue) {
    if (persistenceData == null || StringUtils.isBlank(persistenceData.getData())) {
      return defaultValue;
    }
    return Boolean.parseBoolean(persistenceData.getData());
  }

  public static boolean parse2BoolIgnoreCase(
      DBResponse<PersistenceData> response, boolean defaultValue) {
    if (response == null
        || response.getEntity() == null
        || response.getOperationStatus() != OperationStatus.SUCCESS) {
      return defaultValue;
    }
    return parse2BoolIgnoreCase(response.getEntity(), defaultValue);
  }
}
