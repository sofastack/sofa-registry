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
package com.alipay.sofa.registry.server.data.event;

import java.util.Map;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;

/**
 *
 * @author kezhu.wukz
 * @version $Id: RemoteDataServerChangeEvent.java, v 0.1 2019-12-17 20:13 kezhu.wukz Exp $
 */
public class RemoteDataServerChangeEvent implements Event {

    private final String          dataCenter;
    private Map<String, DataNode> remoteDataServerMap;

    private long                  remoteDataCenterVersion;

    private long                  version;

    /**
     * constructor
     * @param remoteDataServerMap
     * @param version
     * @param remoteDataCenterVersion
     */
    public RemoteDataServerChangeEvent(String dataCenter,
                                       Map<String, DataNode> remoteDataServerMap, long version,
                                       long remoteDataCenterVersion) {
        this.dataCenter = dataCenter;
        this.remoteDataServerMap = remoteDataServerMap;
        this.version = version;
        this.remoteDataCenterVersion = remoteDataCenterVersion;
    }

    /**
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * Getter method for property <tt>remoteDataServerMap</tt>.
     *
     * @return property value of remoteDataServerMap
     */
    public Map<String, DataNode> getRemoteDataServerMap() {
        return remoteDataServerMap;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Getter method for property <tt>remoteDataCenterVersion</tt>.
     *
     * @return property value of remoteDataCenterVersion
     */
    public long getRemoteDataCenterVersion() {
        return remoteDataCenterVersion;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RemoteDataServerChangeEvent{");
        sb.append("dataCenter='").append(dataCenter).append('\'');
        sb.append(", remoteDataServerMap=").append(remoteDataServerMap);
        sb.append(", remoteDataCenterVersion=").append(remoteDataCenterVersion);
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }
}