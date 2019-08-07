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
package com.alipay.sofa.registry.client.api.model;

import java.util.List;
import java.util.Map;

/**
 * The type User data.
 *
 * @author zhuoyu.sjw
 * @version $Id : UserData.java, v 0.1 2017-11-23 14:37 zhuoyu.sjw Exp $$
 */
public interface UserData {

    /**
     * Getter method for property <tt>zoneData</tt>.
     *
     * @return property value of zoneData
     */
    Map<String, List<String>> getZoneData();

    /**
     * Gets local zone.
     *
     * @return the local zone
     */
    String getLocalZone();
}
