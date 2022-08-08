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
<<<<<<< HEAD:server/store/jdbc/src/main/java/com/alipay/sofa/registry/jdbc/config/MetadataConfig.java
package com.alipay.sofa.registry.jdbc.config;

/**
 * @author xiaojian.xj
 * @version $Id: MetadataConfig.java, v 0.1 2021年02月24日 15:20 xiaojian.xj Exp $
 */
public interface MetadataConfig {
  int getRevisionRenewIntervalMinutes();

  int getInterfaceAppsIndexRenewIntervalMinutes();

  int getInterfaceAppsExecutorPoolSize();

  int getInterfaceAppsExecutorQueueSize();

  int getClientManagerExecutorPoolSize();

  int getClientManagerExecutorQueueSize();
=======
package com.alipay.sofa.registry.server.shared.providedata;

import com.alipay.sofa.registry.common.model.metaserver.ProvideData;

/**
 * @author xiaojian.xj
 * @version $Id: FetchSystemPropertyService.java, v 0.1 2021年05月16日 13:06 xiaojian.xj Exp $
 */
public interface FetchSystemPropertyService extends ProvideDataProcessor {

  /** start start data */
  boolean start();

  boolean doFetch();

  @Override
  default boolean processData(ProvideData data) {
    return doFetch();
  }
>>>>>>> sofastack/master:server/server/shared/src/main/java/com/alipay/sofa/registry/server/shared/providedata/FetchSystemPropertyService.java
}
