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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRevision implements Serializable {
    private String                            revision;
    private String                            appName;
    private Map<String, List<String>>         baseParams   = Maps.newHashMap();
    private Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();

    /**
     * Getter method for property <tt>revision</tt>.
     *
     * @return property value of revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Setter method for property <tt>revision</tt>.
     *
     * @param revision value to be assigned to property revision
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Getter method for property <tt>appName</tt>.
     *
     * @return property value of appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Setter method for property <tt>appName</tt>.
     *
     * @param appName value to be assigned to property appName
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Getter method for property <tt>baseParams</tt>.
     *
     * @return property value of baseParams
     */
    public Map<String, List<String>> getBaseParams() {
        return baseParams;
    }

    /**
     * Setter method for property <tt>baseParams</tt>.
     *
     * @param baseParams value to be assigned to property baseParams
     */
    public void setBaseParams(Map<String, List<String>> baseParams) {
        this.baseParams = baseParams;
    }

    public Map<String, AppRevisionInterface> getInterfaceMap() {
        return interfaceMap;
    }

    public void setInterfaceMap(Map<String, AppRevisionInterface> interfaceMap) {
        this.interfaceMap = interfaceMap;
    }

    public static AppRevision convert(AppRevisionRegister register) {
        AppRevision revision = new AppRevision();
        String appName = register.getAppName();
        if (ValueConstants.DISABLE_DATA_ID_CASE_SENSITIVE) {
            appName = appName.toUpperCase();
        }
        revision.setAppName(appName);
        revision.setRevision(register.getRevision());
        revision.setBaseParams(register.getBaseParams());
        Map<String, AppRevisionInterface> interfaceMap = new HashMap<>();
        for (AppRevisionInterface inf : register.getInterfaceList()) {
            String dataInfoId = DataInfo.toDataInfoId(inf.getDataId(), inf.getInstanceId(),
                inf.getGroup());
            interfaceMap.put(dataInfoId, inf);
        }
        revision.setInterfaceMap(interfaceMap);
        return revision;
    }

    @Override
    public String toString() {
        return "AppRevision{" + "revision='" + revision + '\'' + ", appName='" + appName + '\''
               + ", baseParams=" + (baseParams == null ? "0" : baseParams.size()) + '\''
               + ", interfaceMap=" + (interfaceMap == null ? "0" : baseParams.size()) + '\'' + '}';
    }
}
