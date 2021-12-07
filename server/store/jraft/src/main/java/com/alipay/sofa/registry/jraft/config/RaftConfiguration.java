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
package com.alipay.sofa.registry.jraft.config;

import com.alipay.sofa.registry.jraft.repository.impl.AppRevisionRaftRepository;
import com.alipay.sofa.registry.jraft.repository.impl.InterfaceAppsRaftRepository;
import com.alipay.sofa.registry.store.api.config.StoreApiConfiguration;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author xiaojian.xj
 * @version $Id: JdbcConfiguration.java, v 0.1 2021年01月17日 16:28 xiaojian.xj Exp $
 */
@Configuration
@Import({StoreApiConfiguration.class})
@EnableConfigurationProperties
@ConditionalOnProperty(
    value = SpringContext.PERSISTENCE_PROFILE_ACTIVE,
    havingValue = SpringContext.META_STORE_API_RAFT)
public class RaftConfiguration {

  @Configuration
  public static class RepositoryBeanConfiguration {
    @Bean
    public AppRevisionRepository appRevisionRaftRepository() {
      return new AppRevisionRaftRepository();
    }

    @Bean
    public InterfaceAppsRepository interfaceAppsRaftRepository() {
      return new InterfaceAppsRaftRepository();
    }
  }
}
