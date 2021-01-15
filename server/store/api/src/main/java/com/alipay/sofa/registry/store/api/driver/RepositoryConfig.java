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
package com.alipay.sofa.registry.store.api.driver;

import com.alipay.sofa.registry.store.api.driver.RepositoryManager.RepositoryType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author xiaojian.xj
 * @version $Id: RepositoryConfig.java, v 0.1 2021年01月17日 14:08 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = RepositoryConfig.PRE_FIX)
public class RepositoryConfig {

    public static final String                      PRE_FIX          = "jdbc";

    //application.properties
    private String                                  globalRepositoryType;
    /**
     * set RepositoryType config to Repository
     */
    private static final Map<Class, RepositoryType> repositoryConfig = new ConcurrentHashMap<>();

    public RepositoryType getRepositoryType(Class clazz) {

        if (repositoryConfig.containsKey(clazz)) {
            return repositoryConfig.get(clazz);
        }
        return RepositoryType.getByCode(globalRepositoryType);
    }

    /**
     * Getter method for property <tt>globalRepositoryType</tt>.
     *
     * @return property value of globalRepositoryType
     */
    public String getGlobalRepositoryType() {
        return globalRepositoryType;
    }

    /**
     * Setter method for property <tt>globalRepositoryType</tt>.
     *
     * @param globalRepositoryType value to be assigned to property globalRepositoryType
     */
    public void setGlobalRepositoryType(String globalRepositoryType) {
        this.globalRepositoryType = globalRepositoryType;
    }
}