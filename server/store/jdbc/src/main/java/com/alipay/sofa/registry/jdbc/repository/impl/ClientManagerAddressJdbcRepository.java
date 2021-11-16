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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
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
import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressJdbcRepository.java, v 0.1 2021年05月12日 19:27 xiaojian.xj Exp $
 */
public class ClientManagerAddressJdbcRepository
    implements ClientManagerAddressRepository, RecoverConfig {

  private static final Logger LOG = LoggerFactory.getLogger("META-PROVIDEDATA", "[ClientManager]");

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Autowired private ClientManagerAddressMapper clientManagerAddressMapper;

  @Autowired private DateNowRepository dateNowRepository;

  final Informer informer;

  public ClientManagerAddressJdbcRepository() {
    informer = new Informer();
  }

  @PostConstruct
  public void init() {
    informer.setEnabled(true);
    informer.start();
  }

  @Override
  public boolean clientOpen(Set<AddressVersion> ipSet) {

    try {
      doStorage(ipSet, ValueConstants.CLIENT_OPEN);
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
    } catch (Throwable t) {
      LOG.error("clientOpen:{} error.", ipSet, t);
      return false;
    }
    return true;
  }

  @Override
  public boolean clientOff(Set<AddressVersion> ipSet) {
    try {
      doStorage(ipSet, ValueConstants.CLIENT_OFF);
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
    } catch (Throwable t) {
      LOG.error("clientOff:{} error.", ipSet, t);
      return false;
    }
    return true;
  }

  @Override
  public boolean reduce(Set<AddressVersion> ipSet) {
    try {

      doStorage(ipSet, ValueConstants.REDUCE);
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
    } catch (Throwable t) {
      LOG.error("clientOff:{} error.", ipSet, t);
      return false;
    }
    return true;
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

  private void doStorage(Set<AddressVersion> ipSet, String operation) {
    for (AddressVersion address : ipSet) {
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
      }
    }
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
   */
  @VisibleForTesting
  public void setClientManagerAddressMapper(ClientManagerAddressMapper clientManagerAddressMapper) {
    this.clientManagerAddressMapper = clientManagerAddressMapper;
  }
}
