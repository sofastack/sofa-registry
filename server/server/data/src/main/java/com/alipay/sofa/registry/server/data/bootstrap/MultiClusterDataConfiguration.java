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
package com.alipay.sofa.registry.server.data.bootstrap;

import com.alipay.sofa.registry.common.model.slot.filter.MultiSyncDataAcceptorManager;
import com.alipay.sofa.registry.server.data.multi.cluster.client.handler.RemoteDataChangeNotifyHandler;
import com.alipay.sofa.registry.server.data.multi.cluster.dataserver.handler.MultiClusterSlotDiffDigestRequestHandler;
import com.alipay.sofa.registry.server.data.multi.cluster.dataserver.handler.MultiClusterSlotDiffPublisherRequestHandler;
import com.alipay.sofa.registry.server.data.multi.cluster.exchanger.RemoteDataNodeExchanger;
import com.alipay.sofa.registry.server.data.multi.cluster.executor.MultiClusterExecutorManager;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManager;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManagerImpl;
import com.alipay.sofa.registry.server.data.multi.cluster.storage.MultiClusterDatumService;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaojian.xj
 * @version : MultiClusterDataConfiguration.java, v 0.1 2022年05月06日 15:46 xiaojian.xj Exp $
 */
@Configuration
@EnableConfigurationProperties
public class MultiClusterDataConfiguration {

  @Configuration
  public static class RemoteDataConfigConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MultiClusterDataServerConfigBean multiClusterDataServerConfig() {
      return new MultiClusterDataServerConfigBean();
    }

    @Bean
    public MultiClusterExecutorManager multiClusterExecutorManager(
        MultiClusterDataServerConfig multiClusterDataServerConfig) {
      return new MultiClusterExecutorManager(multiClusterDataServerConfig);
    }
  }

  @Configuration
  public static class RemoteClusterExchangerConfiguration {

    @Bean
    public RemoteDataNodeExchanger remoteDataNodeExchanger() {
      return new RemoteDataNodeExchanger();
    }

    @Bean(name = "remoteDataServerHandlers")
    public Collection<AbstractServerHandler> remoteDataServerHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(multiClusterSlotDiffDigestRequestHandler());
      list.add(multiClusterSlotDiffPublisherRequestHandler());
      return list;
    }

    @Bean(name = "remoteDataClientHandlers")
    public Collection<AbstractClientHandler> remoteDataClientHandlers() {
      Collection<AbstractClientHandler> list = new ArrayList<>();
      list.add(remoteDataChangeNotifyHandler());
      return list;
    }

    @Bean
    public AbstractServerHandler multiClusterSlotDiffDigestRequestHandler() {
      return new MultiClusterSlotDiffDigestRequestHandler();
    }

    @Bean
    public AbstractServerHandler multiClusterSlotDiffPublisherRequestHandler() {
      return new MultiClusterSlotDiffPublisherRequestHandler();
    }

    @Bean
    public AbstractClientHandler remoteDataChangeNotifyHandler() {
      return new RemoteDataChangeNotifyHandler();
    }
  }

  @Configuration
  public static class RemoteDataStorageConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MultiClusterSlotManager multiClusterSlotManager() {
      return new MultiClusterSlotManagerImpl();
    }

    @Bean
    public MultiSyncDataAcceptorManager multiSyncDataAcceptorManager() {
      return new MultiSyncDataAcceptorManager();
    }

    @Bean
    public MultiClusterDatumService multiClusterDatumService() {
      return new MultiClusterDatumService();
    }
  }
}
