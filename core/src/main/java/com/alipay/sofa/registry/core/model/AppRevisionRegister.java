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
import java.util.List;
import java.util.Map;

public class AppRevisionRegister implements Serializable {
    private String                            revision;
    private String                            appName;
    private Map<String, List<String>>         baseParams;
    private Map<String, AppRevisionInterface> interfaces;

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

    /**
     * Getter method for property <tt>interfaces</tt>.
     *
     * @return property value of interfaces
     */
    public Map<String, AppRevisionInterface> getInterfaces() {
        return interfaces;
    }

    /**
     * Setter method for property <tt>interfaces</tt>.
     *
     * @param interfaces value to be assigned to property interfaces
     */
    public void setInterfaces(Map<String, AppRevisionInterface> interfaces) {
        this.interfaces = interfaces;
    }
}
