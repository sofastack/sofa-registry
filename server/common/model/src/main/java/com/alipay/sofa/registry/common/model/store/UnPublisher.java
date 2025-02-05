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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.RegisterVersion;

/**
 * @author qian.lqlq
 * @version $Id: UnPublisher.java, v 0.1 2018-01-11 20:05 qian.lqlq Exp $
 */
public class UnPublisher extends Publisher {

  /**
   * @param dataInfoId
   * @param registerId
   * @param registerTimeStamp
   */
  public UnPublisher(
      String dataInfoId,
      ProcessId sessionProcessId,
      String registerId,
      long registerTimeStamp,
      long version) {
    setDataInfoId(dataInfoId);
    setRegisterId(registerId);
    setRegisterTimestamp(registerTimeStamp);
    setVersion(version);
    // avoid new datum dataId is null
    DataInfo dataInfo = DataInfo.valueOf(dataInfoId);
    setDataId(dataInfo.getDataId());
    setGroup(dataInfo.getGroup());
    setInstanceId(dataInfo.getInstanceId());

    setSessionProcessId(sessionProcessId);
  }

  @Override
  public DataType getDataType() {
    return DataType.UN_PUBLISHER;
  }

  public static UnPublisher of(Publisher publisher) {
    return new UnPublisher(
        publisher.getDataInfoId(),
        publisher.getSessionProcessId(),
        publisher.getRegisterId(),
        publisher.getRegisterTimestamp(),
        publisher.getVersion());
  }

  public static UnPublisher of(String dataInfoId, String registerId, RegisterVersion version) {
    return new UnPublisher(
        dataInfoId, null, registerId, version.getRegisterTimestamp(), version.getVersion());
  }
}
