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
package com.alipay.sofa.registry.server.meta.repository;

import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;

import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: RepositoryService.java, v 0.1 2018-05-07 16:03 shangyu.wh Exp $
 */
public interface RepositoryService<K, V> {

    V put(K key, V value);

    V remove(Object key);

    V replace(K key, V value);

    @ReadOnLeader
    V get(Object key);

    @ReadOnLeader
    Map<K, V> getAllData();

    @ReadOnLeader
    Map<String, Map<K, V>> getAllDataMap();

    @ReadOnLeader
    Map<String, NodeRepository> getNodeRepositories();

    Map<K, V> replaceAll(String dataCenter, Map<K, V> map, Long version);

    @ReadOnLeader
    boolean checkVersion(K key, Long version);

    @ReadOnLeader
    Long getVersion(K key);
}