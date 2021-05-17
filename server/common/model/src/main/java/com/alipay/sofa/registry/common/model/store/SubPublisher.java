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

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.StringUtils;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public final class SubPublisher implements Serializable, Sizer {
  private final String registerId;
  private final String cell;
  private final String clientId;
  private final String srcAddressString;
  private final List<ServerDataBox> dataList;
  private final long registerTimestamp;
  private final long version;
  private final PublishSource publishSource;

  public SubPublisher(
      String registerId,
      String cell,
      List<ServerDataBox> dataList,
      String clientId,
      long version,
      String srcAddressString,
      long registerTimestamp,
      PublishSource publishSource) {
    this.registerId = registerId;
    this.cell = cell;
    this.clientId = clientId;
    this.version = version;
    this.srcAddressString = srcAddressString;
    this.dataList =
        dataList == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(Lists.newArrayList(dataList));
    this.registerTimestamp = registerTimestamp;
    this.publishSource = publishSource;
  }

  public String getCell() {
    return cell;
  }

  public List<ServerDataBox> getDataList() {
    return dataList;
  }

  public String getClientId() {
    return clientId;
  }

  public String getSrcAddressString() {
    return srcAddressString;
  }

  public long getRegisterTimestamp() {
    return registerTimestamp;
  }

  public int getDataBoxBytes() {
    int bytes = 0;
    for (ServerDataBox box : dataList) {
      bytes += box.byteSize();
    }
    return bytes;
  }

  public long getVersion() {
    return version;
  }

  public RegisterVersion registerVersion() {
    return RegisterVersion.of(version, registerTimestamp);
  }

  public String getRegisterId() {
    return registerId;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "SubPublisher{{},cell={},src={},datas={},bytes={},ver={},ts={}}",
        registerId,
        cell,
        srcAddressString,
        dataList.size(),
        getDataBoxBytes(),
        version,
        registerTimestamp);
  }

  public PublishSource getPublishSource() {
    return publishSource;
  }

  public int size() {
    int s =
        StringUtils.sizeof(registerId)
            + StringUtils.sizeof(cell)
            + StringUtils.sizeof(srcAddressString)
            + 40;
    if (dataList != null) {
      for (ServerDataBox box : dataList) {
        s += box.byteSize();
      }
    }
    return s;
  }
}
