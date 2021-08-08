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

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RocksDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import com.alipay.sofa.registry.jraft.repository.impl.*;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionHeartbeatRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : xingpeng
 * @date : 2021-07-06 16:16
 **/
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(
    value = SpringContext.PERSISTENCE_PROFILE_ACTIVE,
    havingValue = SpringContext.META_STORE_API_RAFT)
public class RaftConfiguration {

  @Configuration
  public static class MetadataBeanConfiguration{
    @Bean
    public DefaultCommonConfig defaultCommonConfig(){
      return new DefaultCommonConfigBean();
    }

    @Bean
    public MetaElectorConfig metaElectorConfig(){
      return new MetaElectorConfigBean();
    }

    @Bean
    public MetadataConfig metadataConfig() {
      return new MetadataConfigBean();
    }
  }
  

  @Configuration
  public static class RheaKVBeanConfiguration{

      @Bean()
      @ConditionalOnMissingBean(RheaKVStore.class)
      public RheaKVStore rheaKVStore(){
        DefaultRheaKVStore defaultRheaKVStore = new DefaultRheaKVStore();
        String address = DefaultConfigs.ADDRESS;
        StoreEngineOptions storeOpts = StoreEngineOptionsConfigured.newConfigured()
                .withStorageType(StorageType.RocksDB)
                .withRocksDBOptions(RocksDBOptionsConfigured.newConfigured().withDbPath(DefaultConfigs.DB_PATH).config())
                .withRaftDataPath(DefaultConfigs.RAFT_DATA_PATH)
                .withServerAddress(new Endpoint(address, 8181))
                .config();

        PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true)
                .config();

        RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured.newConfigured()
                .withStoreEngineOptions(storeOpts)
                .withPlacementDriverOptions(pdOpts)
                .withInitialServerList(address+":"+"8181")
                .config();

        defaultRheaKVStore.init(rheaKVStoreOptions);
        return defaultRheaKVStore;
      }

//    @Bean
//    public AppRevisionHeartbeatBatchCallable appRevisionHeartbeatBatchCallable() {
//      return new AppRevisionHeartbeatBatchCallable();
//    }
  }

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

    @Bean
    public AppRevisionHeartbeatRepository appRevisionHeartbeatRaftRepository() {
      return new AppRevisionHeartbeatRaftRepository();
    }

    @Bean
    public ProvideDataRepository provideDataRepository(){
      return new ProvideDataRaftRepository();
    }
  }
  
}
