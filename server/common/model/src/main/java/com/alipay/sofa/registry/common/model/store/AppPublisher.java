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

import com.alipay.sofa.registry.common.model.AppRegisterServerDataBox;

import java.util.List;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AppPublisher.java, v 0.1 2020年11月10日 23:15 xiaojian.xj Exp $
 */
public class AppPublisher extends Publisher {

    private List<AppRegisterServerDataBox> appDataList;

    /**
     * Getter method for property <tt>appDataList</tt>.
     *
     * @return property value of appDataList
     */
    public List<AppRegisterServerDataBox> getAppDataList() {
        return appDataList;
    }

    /**
     * Setter method for property <tt>appDataList</tt>.
     *
     * @param appDataList value to be assigned to property appDataList
     */
    public void setAppDataList(List<AppRegisterServerDataBox> appDataList) {
        this.appDataList = appDataList;
    }
}