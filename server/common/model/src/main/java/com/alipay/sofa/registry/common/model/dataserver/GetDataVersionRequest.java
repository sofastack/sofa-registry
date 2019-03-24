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

import java.io.Serializable;
import java.util.List;

/**
 * request to get versions of specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataVersionRequest.java, v 0.1 2017-12-01 下午4:58 qian.lqlq Exp $
 */
public class GetDataVersionRequest implements Serializable {

    private static final long serialVersionUID = 8942977145684175886L;

    private List<String>      dataInfoIds;

    /**
     * constructor
     */
    public GetDataVersionRequest() {
    }

    /**
     * Getter method for property <tt>dataInfoIds</tt>.
     *
     * @return property value of dataInfoIds
     */
    public List<String> getDataInfoIds() {
        return dataInfoIds;
    }

    /**
     * Setter method for property <tt>dataInfoIds</tt>.
     *
     * @param dataInfoIds  value to be assigned to property dataInfoIds
     */
    public void setDataInfoIds(List<String> dataInfoIds) {
        this.dataInfoIds = dataInfoIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[GetDataVersionRequest] dataInfoIds=");
        if (dataInfoIds != null) {
            sb.append(this.dataInfoIds.size());
        }
        return sb.toString();
    }
}