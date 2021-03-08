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

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public final class SubPublisher implements Serializable {
    private final String              cell;
    private final String              clientId;
    private final String              srcAddressString;
    private final List<ServerDataBox> dataList;
    private final long                registerTimestamp;

    public SubPublisher(String cell, List<ServerDataBox> dataList, String clientId,
                        String srcAddressString, long registerTimestamp) {
        this.cell = cell;
        this.clientId = clientId;
        this.srcAddressString = srcAddressString;
        this.dataList = Collections.unmodifiableList(Lists.newArrayList(dataList));
        this.registerTimestamp = registerTimestamp;
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

    @Override
    public String toString() {
        return StringFormatter
            .format("SubPublisher{{},cell={},src={},datas={},bytes={},ts={}}", clientId, cell,
                srcAddressString, dataList.size(), getDataBoxBytes(), registerTimestamp);
    }
}
