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
package com.alipay.sofa.registry.jdbc.config;

import com.alipay.sofa.registry.jdbc.elector.MetaJdbcLeaderElector;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaojian.xj
 * @version $Id: JdbcElectorConfiguration.java, v 0.1 2021年04月14日 19:47 xiaojian.xj Exp $
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(
    value = SpringContext.PERSISTENCE_PROFILE_ACTIVE,
    havingValue = SpringContext.META_STORE_API_JDBC,
    matchIfMissing = true)
public class JdbcElectorConfiguration {

  @Configuration
  public static class JdbcElectorBeanConfiguration {

    @Bean
    public LeaderElector leaderElector() {
      return new MetaJdbcLeaderElector();
    }
  }
}
