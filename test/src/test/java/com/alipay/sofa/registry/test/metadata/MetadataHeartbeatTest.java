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
package com.alipay.sofa.registry.test.metadata;

import com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaojian.xj
 * @version $Id: MetadataHeartbeatTest.java, v 0.1 2021年04月02日 16:14 xiaojian.xj Exp $
 */
public class MetadataHeartbeatTest extends MetadataTest {

  public class HeartbeatRunner extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      for (AppRevision appRevision : appRevisionList) {
        MetaHeartbeatResponse response =
            appRevisionHandlerStrategy.heartbeat(
                Collections.singletonList(appRevision.getRevision()));
        if (response.getStatusCode() == ValueConstants.METADATA_STATUS_DATA_NOT_FOUND) {
          RegisterResponse result = new RegisterResponse();
          appRevisionHandlerStrategy.handleAppRevisionRegister(appRevision, result, "");
        }
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }
}
