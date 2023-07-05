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
package com.alipay.sofa.registry.jdbc.repository.impl;

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.ProvideData.CLIENT_MANAGER_UPDATE_COUNTER;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerResult;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.jdbc.config.MetadataConfig;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.domain.ClientManagerAddressDomain;
import com.alipay.sofa.registry.jdbc.informer.BaseInformer;
import com.alipay.sofa.registry.jdbc.mapper.ClientManagerAddressMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressJdbcRepository.java, v 0.1 2021年05月12日 19:27 xiaojian.xj Exp $
 */
public class ClientManagerAddressJdbcRepository
    implements ClientManagerAddressRepository, RecoverConfig {

  private static final Logger LOG = LoggerFactory.getLogger("CLIENT-MANAGER", "[ClientManager]");

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Autowired private ClientManagerAddressMapper clientManagerAddressMapper;

  @Autowired private DateNowRepository dateNowRepository;

  @Autowired private MetadataConfig metadataConfig;

  private ThreadPoolExecutor clientManagerExecutor;

  final Informer informer;

  public ClientManagerAddressJdbcRepository() {
    informer = new Informer();
  }

  public ClientManagerAddressJdbcRepository(MetadataConfig metadataConfig) {
    informer = new Informer();
    this.metadataConfig = metadataConfig;
  }

  @PostConstruct
  public void init() {
    clientManagerExecutor =
        new ThreadPoolExecutor(
            metadataConfig.getClientManagerExecutorPoolSize(),
            metadataConfig.getClientManagerExecutorPoolSize(),
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(metadataConfig.getClientManagerExecutorQueueSize()),
            new NamedThreadFactory("ClientManagerExecutor"),
            new CallerRunsPolicy());

    informer.setEnabled(true);
    informer.start();
  }

  @Override
  public ClientManagerResult clientOpen(Set<AddressVersion> ipSet) {
    try {
      long maxId = doStorage(ipSet, ValueConstants.CLIENT_OPEN);
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
      return ClientManagerResult.buildSuccess(maxId);
    } catch (Throwable t) {
      LOG.error("clientOpen:{} error.", ipSet, t);
      return ClientManagerResult.buildFailRet();
    }
  }

  @Override
  public ClientManagerResult clientOff(Set<AddressVersion> ipSet) {
    try {
      long maxId = doStorage(ipSet, ValueConstants.CLIENT_OFF);
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
      return ClientManagerResult.buildSuccess(maxId);
    } catch (Throwable t) {
      LOG.error("clientOff:{} error.", ipSet, t);
      return ClientManagerResult.buildFailRet();
    }
  }

  @Override
  public ClientManagerResult reduce(Set<AddressVersion> ipSet) {
    try {
      long maxId = doStorage(ipSet, ValueConstants.REDUCE);
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
      return ClientManagerResult.buildSuccess(maxId);
    } catch (Throwable t) {
      LOG.error("clientOff:{} error.", ipSet, t);
      return ClientManagerResult.buildFailRet();
    }
  }

  @Override
  public ClientManagerAddress queryClientOffData() {
    long version = informer.getLastLoadId();
    ClientManagerAddressContainer.ClientManagerAddress query =
        informer.getContainer().queryClientManagerAddress();
    return new ClientManagerAddress(version, query.getClientOffData(), query.getReduces());
  }

  @Override
  public void waitSynced() {
    informer.waitSynced();
  }

  @Override
  public List<String> getExpireAddress(Date date, int limit) {
    return clientManagerAddressMapper.getExpireAddress(
        defaultCommonConfig.getClusterId(tableName()), date, limit);
  }

  @Override
  public int cleanExpired(List<String> expireAddress) {
    if (CollectionUtils.isEmpty(expireAddress)) {
      return 0;
    }

    return clientManagerAddressMapper.cleanExpired(
        defaultCommonConfig.getClusterId(tableName()), expireAddress);
  }

  @Override
  public int getClientOffSizeBefore(Date date) {
    return clientManagerAddressMapper.getClientOffSizeBefore(
        defaultCommonConfig.getClusterId(tableName()), date);
  }

  @Override
  public void wakeup() {
    informer.watchWakeup();
  }

  private long doStorage(Set<AddressVersion> ipSet, String operation)
      throws ExecutionException, InterruptedException, TimeoutException {
    List<Future<Tuple<Boolean, Long>>> futures = Lists.newArrayList();
    for (AddressVersion address : ipSet) {
      Future<Tuple<Boolean, Long>> future =
          clientManagerExecutor.submit(
              () -> {
                long maxId = 0;
                ClientManagerAddressDomain update =
                    new ClientManagerAddressDomain(
                        defaultCommonConfig.getClusterId(tableName()),
                        address.getAddress(),
                        operation,
                        address.isPub(),
                        address.isSub());
                int effectRows = clientManagerAddressMapper.update(update);

                if (effectRows == 0) {
                  clientManagerAddressMapper.insertOnReplace(update);
                  LOG.info("address: {} replace.", update);
                  maxId = update.getId();
                } else {
                  LOG.info("address: {} exist, skip replace.", update);
                }
                return new Tuple<>(true, maxId);
              });
      futures.add(future);
    }

    long maxId = 0;
    for (Future<Tuple<Boolean, Long>> future : futures) {
      Tuple<Boolean, Long> tuple = future.get(3000, TimeUnit.MILLISECONDS);
      if (tuple.o1 == null || !tuple.o1) {
        throw new SofaRegistryRuntimeException(
            StringFormatter.format("ips: {}, operation:{} execute error.", ipSet, operation));
      }
      if (tuple.o2 > maxId) {
        maxId = tuple.o2;
      }
    }

    return maxId;
  }

  @Override
  public String tableName() {
    return TableEnum.CLIENT_MANAGER_ADDRESS.getTableName();
  }

  class Informer extends BaseInformer<ClientManagerAddressDomain, ClientManagerAddressContainer> {

    public Informer() {
      super("ClientManager", LOG);
    }

    @Override
    protected ClientManagerAddressContainer containerFactory() {
      return new ClientManagerAddressContainer();
    }

    @Override
    protected List<ClientManagerAddressDomain> listFromStorage(long start, int limit) {
      return clientManagerAddressMapper.queryAfterThanByLimit(
          defaultCommonConfig.getClusterId(tableName()), start, limit);
    }

    @Override
    protected Date getNow() {
      return dateNowRepository.getNow();
    }
  }

  /**
   * Setter method for property <tt>clientManagerAddressMapper</tt>.
   *
   * @param clientManagerAddressMapper value to be assigned to property clientManagerAddressMapper
   * @return ClientManagerAddressJdbcRepository
   */
  @VisibleForTesting
  public ClientManagerAddressJdbcRepository setClientManagerAddressMapper(
      ClientManagerAddressMapper clientManagerAddressMapper) {
    this.clientManagerAddressMapper = clientManagerAddressMapper;
    return this;
  }

  /**
   * Setter method for property <tt>defaultCommonConfig</tt>.
   *
   * @param defaultCommonConfig value to be assigned to property defaultCommonConfig
   * @return ClientManagerAddressJdbcRepository
   */
  @VisibleForTesting
  public ClientManagerAddressJdbcRepository setDefaultCommonConfig(
      DefaultCommonConfig defaultCommonConfig) {
    this.defaultCommonConfig = defaultCommonConfig;
    return this;
  }
}
