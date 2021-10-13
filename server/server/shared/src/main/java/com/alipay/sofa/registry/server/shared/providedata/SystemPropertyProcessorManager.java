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
package com.alipay.sofa.registry.server.shared.providedata;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author shangyu.wh
 * @version 1.0: SystemPropertyProcessorManager.java, v 0.1 2019-10-09 17:39 shangyu.wh Exp $
 */
public class SystemPropertyProcessorManager {

  private Collection<AbstractFetchSystemPropertyService> systemDataProcessors = new ArrayList<>();

  private Collection<AbstractFetchPersistenceSystemProperty> systemDataPersistenceProcessors =
      new ArrayList<>();

  public void addSystemDataProcessor(AbstractFetchSystemPropertyService systemDataProcessor) {
    systemDataProcessors.add(systemDataProcessor);
  }

  public void addSystemDataPersistenceProcessor(
      AbstractFetchPersistenceSystemProperty systemDataProcessor) {
    systemDataPersistenceProcessors.add(systemDataProcessor);
  }

  public boolean doFetch(String dataInfoId) {
    for (FetchSystemPropertyService systemDataProcessor : systemDataProcessors) {
      if (systemDataProcessor.support(dataInfoId)) {
        return systemDataProcessor.doFetch();
      }
    }
    for (FetchSystemPropertyService systemDataProcessor : systemDataPersistenceProcessors) {
      if (systemDataProcessor.support(dataInfoId)) {
        return systemDataProcessor.doFetch();
      }
    }

    return false;
  }

  public boolean startFetchMetaSystemProperty() {
    boolean success = true;

    for (AbstractFetchSystemPropertyService systemDataProcessor : systemDataProcessors) {
      if (!systemDataProcessor.start()) {
        success = false;
      }
    }
    return success;
  }

  public boolean startFetchPersistenceSystemProperty() {
    boolean success = true;

    for (AbstractFetchPersistenceSystemProperty systemDataProcessor :
        systemDataPersistenceProcessors) {
      if (!systemDataProcessor.start()) {
        success = false;
      }
    }
    return success;
  }
}
