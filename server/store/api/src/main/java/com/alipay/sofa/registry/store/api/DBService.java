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
package com.alipay.sofa.registry.store.api;

import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;

/**
 *
 * @author shangyu.wh
 * @version $Id: DBService.java, v 0.1 2018-04-18 12:06 shangyu.wh Exp $
 */
public interface DBService {

    /**
     * open db
     * @param dbName
     * @param entityClass
     */
    void openDB(String dbName, Class entityClass);

    /**
     * add
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    boolean put(String key, Object value) throws Exception;

    /**
     * get
     * @param key
     * @return
     * @throws Exception
     */
    @ReadOnLeader
    DBResponse get(String key) throws Exception;

    /**
     * update
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    boolean update(String key, Object value) throws Exception;

    /**
     * delete
     * @param key
     * @return
     * @throws Exception
     */
    boolean remove(String key) throws Exception;
}