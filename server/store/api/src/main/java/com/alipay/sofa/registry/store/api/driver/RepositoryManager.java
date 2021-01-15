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

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author xiaojian.xj
 * @version $Id: RepositoryManager.java, v 0.1 2021年01月15日 12:03 xiaojian.xj Exp $
 */
public class RepositoryManager {

    private static final Logger                         REPOSITORY_LOGGER       = LoggerFactory
                                                                                    .getLogger(
                                                                                        RepositoryManager.class,
                                                                                        "[RepositoryManager]");

    private final static Map<Class, RegistryRepository> raftRegisterRepositorys = new ConcurrentHashMap<>();

    private final static Map<Class, RegistryRepository> jdbcRegisterRepositorys = new ConcurrentHashMap<>();

    @Autowired
    private RepositoryConfig                            repositoryConfig;

    public static synchronized void registerRepository(RegistryRepository repository) {

        if (repository == null) {
            throw new RuntimeException("register repository is null.");
        }

        if (repository.accept(RepositoryType.RAFT)) {
            raftRegisterRepositorys.putIfAbsent(repository.getInterfaceClass(), repository);
            REPOSITORY_LOGGER.info("raft registerRaftRepository: " + repository);
        } else if (repository.accept(RepositoryType.JDBC)) {
            jdbcRegisterRepositorys.putIfAbsent(repository.getInterfaceClass(), repository);
            REPOSITORY_LOGGER.info("jdbc registerRaftRepository: " + repository);
        } else {
            throw new RuntimeException("register repository accept is invalid.");
        }
    }

    public RegistryRepository getRepository(Class clazz) {

        RepositoryType driver = repositoryConfig.getRepositoryType(clazz);
        RegistryRepository registryRepository = null;
        if (driver == RepositoryType.JDBC) {
            registryRepository = jdbcRegisterRepositorys.get(clazz);
        } else if (driver == RepositoryType.RAFT) {
            registryRepository = raftRegisterRepositorys.get(clazz);
        }
        if (registryRepository == null) {
            REPOSITORY_LOGGER.error("repository not exist for class: " + clazz);
            throw new RuntimeException("repository not exist.");
        }
        return registryRepository;
    }

    public enum RepositoryType {

        RAFT("raft:driver"),

        JDBC("jdbc:driver"), ;

        private String code;

        RepositoryType(String code) {
            this.code = code;
        }

        public static RepositoryType getByCode(String code) {
            if (StringUtil.isBlank(code)) {
                return null;
            }

            for (RepositoryType value : RepositoryType.values()) {
                if (StringUtil.equals(value.getCode(), code)) {
                    return value;
                }
            }
            REPOSITORY_LOGGER.error("error repository type: " + code);
            return null;
        }

        /**
         * Getter method for property <tt>code</tt>.
         *
         * @return property value of code
         */
        public String getCode() {
            return code;
        }
    }
}