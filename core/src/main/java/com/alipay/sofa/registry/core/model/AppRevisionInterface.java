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
package com.alipay.sofa.registry.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRevisionInterface implements Serializable {
    private String                    dataId;
    private String                    group;
    private String                    instanceId;
    private Map<String, List<String>> serviceParams = new HashMap<>();

    /**
     * Getter method for property <tt>dataId</tt>.
     *
     * @return property value of dataId
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Setter method for property <tt>dataId</tt>.
     *
     * @param dataId value to be assigned to property dataId
     */
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    /**
     * Getter method for property <tt>group</tt>.
     *
     * @return property value of group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Setter method for property <tt>group</tt>.
     *
     * @param group value to be assigned to property group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Getter method for property <tt>instanceId</tt>.
     *
     * @return property value of instanceId
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Setter method for property <tt>instanceId</tt>.
     *
     * @param instanceId value to be assigned to property instanceId
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Getter method for property <tt>serviceParams</tt>.
     *
     * @return property value of serviceParams
     */
    public Map<String, List<String>> getServiceParams() {
        return serviceParams;
    }

    /**
     * Setter method for property <tt>serviceParams</tt>.
     *
     * @param serviceParams value to be assigned to property serviceParams
     */
    public void setServiceParams(Map<String, List<String>> serviceParams) {
        this.serviceParams = serviceParams;
    }
}
