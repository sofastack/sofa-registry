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
package com.alipay.sofa.registry.jraft.repository.impl;

import com.alipay.sofa.registry.store.api.driver.RegistryRepository;
import com.alipay.sofa.registry.store.api.driver.RepositoryManager.RepositoryType;

/**
 *
 * @author xiaojian.xj
 * @version $Id: RaftRepository.java, v 0.1 2021年01月17日 15:56 xiaojian.xj Exp $
 */
public interface RaftRepository extends RegistryRepository {

    @Override
    default boolean accept(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.RAFT;
    }
}