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
package com.alipay.sofa.registry.server.meta.bootstrap;

import com.alipay.sofa.registry.server.meta.bootstrap.config.MultiClusterMetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MultiClusterMetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.multi.cluster.DefaultMultiClusterSlotTableSyncer;
import com.alipay.sofa.registry.server.meta.multi.cluster.remote.RemoteClusterMetaExchanger;
import com.alipay.sofa.registry.server.meta.multi.cluster.remote.RemoteClusterSlotSyncHandler;
import com.alipay.sofa.registry.server.meta.resource.MultiClusterSyncResource;
import com.alipay.sofa.registry.server.meta.resource.MultiDatumResource;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaojian.xj
 * @version : MultiClusterMetaServerConfiguration.java, v 0.1 2022年04月29日 20:56 xiaojian.xj Exp $
 */
@Configuration
@EnableConfigurationProperties
public class MultiClusterMetaServerConfiguration {

  @Configuration
  public static class MultiClusterConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MultiClusterMetaServerConfig multiClusterMetaServerConfig() {
      return new MultiClusterMetaServerConfigBean();
    }

    @Bean
    public DefaultMultiClusterSlotTableSyncer multiClusterSlotTableSyncer() {
      return new DefaultMultiClusterSlotTableSyncer();
    }

    @Bean
    public RemoteClusterMetaExchanger remoteClusterMetaExchanger() {
      return new RemoteClusterMetaExchanger();
    }

    @Bean
    public MultiClusterSyncResource multiClusterSyncResource() {
      return new MultiClusterSyncResource();
    }

    @Bean
    public MultiDatumResource multiDatumResource() {
      return new MultiDatumResource();
    }
  }

  @Configuration
  public static class MultiClusterRemotingConfiguration {

    @Bean(name = "remoteMetaServerHandlers")
    public Collection<AbstractServerHandler> metaServerHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(remoteClusterSlotSyncHandler());
      return list;
    }

    @Bean
    public RemoteClusterSlotSyncHandler remoteClusterSlotSyncHandler() {
      return new RemoteClusterSlotSyncHandler();
    }
  }
}
