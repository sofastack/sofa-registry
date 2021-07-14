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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import java.util.Objects;

public class DataPos {
  private final String registerId;
  private final String dataInfoId;
  private int hash;

  public DataPos(String dataInfoId, String registerId) {
    this.dataInfoId = dataInfoId;
    this.registerId = registerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataPos dataPos = (DataPos) o;
    return Objects.equals(registerId, dataPos.registerId)
        && Objects.equals(dataInfoId, dataPos.dataInfoId);
  }

  @Override
  public int hashCode() {
    if (hash == 0) {
      hash = Objects.hash(dataInfoId, registerId);
    }
    return hash;
  }

  public static DataPos of(BaseInfo info) {
    return new DataPos(info.getDataInfoId(), info.getRegisterId());
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public String getRegisterId() {
    return registerId;
  }
}
