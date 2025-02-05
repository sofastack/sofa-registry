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
package com.alipay.sofa.registry.common.model.dataserver;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;

/**
 * request to get versions of specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataVersionRequest.java, v 0.1 2017-12-01 下午4:58 qian.lqlq Exp $
 */
public class GetDataVersionRequest extends AbstractSlotRequest {

  private static final long serialVersionUID = 8942977145684175886L;

  private final String dataCenter;
  // dataInfoId:max(push.version)
  private final Map<String, DatumVersion> interests;

  public GetDataVersionRequest(
      String dataCenter,
      ProcessId sessionProcessId,
      int slotId,
      Map<String, DatumVersion> interests) {
    super(slotId, sessionProcessId);
    this.dataCenter = dataCenter;
    this.interests = Collections.unmodifiableMap(Maps.newHashMap(interests));
  }

  public Map<String, DatumVersion> getInterests() {
    return interests;
  }

  public String getDataCenter() {
    return dataCenter;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "GetDataVer:{},{},{},{},interests={}",
        getSlotId(),
        dataCenter,
        getSlotLeaderEpoch(),
        getSlotTableEpoch(),
        interests.size());
  }
}
