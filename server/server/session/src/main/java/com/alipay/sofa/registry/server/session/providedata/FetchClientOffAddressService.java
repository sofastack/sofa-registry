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
package com.alipay.sofa.registry.server.session.providedata;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.GaugeFunc;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService.ClientOffAddressResp;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService.ClientOffAddressStorage;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchPersistenceSystemProperty;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: FetchClientOffAddressService.java, v 0.1 2021年05月16日 18:01 xiaojian.xj Exp $
 */
public class FetchClientOffAddressService
    extends AbstractFetchPersistenceSystemProperty<ClientOffAddressStorage, ClientOffAddressResp> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchClientOffAddressService.class);

  protected final ClientManagerProcessor clientManagerProcessor = new ClientManagerProcessor();

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private ConnectionsService connectionsService;

  @Autowired private Registry sessionRegistry;

  @Autowired private ClientManagerAddressRepository clientManagerAddressRepository;

  private static final GaugeFunc CLIENT_OFF_GAUGE =
      GaugeFunc.build()
          .namespace("session")
          .subsystem("client_off")
          .name("address_total")
          .help("client off address total")
          .register();

  public FetchClientOffAddressService() {
    super(
        ValueConstants.CLIENT_OFF_ADDRESS_DATA_ID,
        new ClientOffAddressStorage(INIT_VERSION, Collections.emptyMap(), null));
  }

  @PostConstruct
  public void postConstruct() {
    CLIENT_OFF_GAUGE.func(() -> storage.get().clientOffAddress.size());
    ConcurrentUtils.createDaemonThread("ClientManagerProcessor", clientManagerProcessor).start();
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getClientManagerAddressIntervalMillis();
  }

  @Override
  protected ClientOffAddressResp fetchFromPersistence() {
    ClientManagerAddress clientManagerAddress = clientManagerAddressRepository.queryClientOffData();
    return new ClientOffAddressResp(
        clientManagerAddress.getVersion(), clientManagerAddress.getClientOffAddress());
  }

  @Override
  protected boolean doProcess(ClientOffAddressStorage expect, ClientOffAddressResp data) {
    if (expect.updating.get() != null) {
      return false;
    }
    Set<String> news = data.clientOffAddress.keySet();
    Set<String> olds = expect.clientOffAddress.keySet();
    long oldVersion = expect.getVersion();

    Set<String> toBeRemove = Sets.difference(olds, news);
    Set<String> toBeAdd = Sets.difference(news, olds);

    Map<String, AddressVersion> adds = Maps.newHashMapWithExpectedSize(toBeAdd.size());
    for (String addAddress : toBeAdd) {
      AddressVersion add = data.clientOffAddress.get(addAddress);
      adds.put(addAddress, add);
    }
    ParaCheckUtil.checkEquals(toBeAdd.size(), adds.size(), "toBeAdd");
    try {

      ClientOffAddressStorage update =
          new ClientOffAddressStorage(
              data.getVersion(),
              data.clientOffAddress,
              new ClientOffTable(toBeAdd, adds, toBeRemove));
      if (!compareAndSet(expect, update)) {
        LOGGER.error("update clientOffAddress:{} fail.", update.getVersion());
        return false;
      }
    } catch (Throwable t) {
      LOGGER.error("update clientOffAddress:{} error.", data, t);
    }

    LOGGER.info(
        "olds clientOffAddress:{}, oldVersion:{}, toBeAdd:{}, toBeRemove:{}, current clientOffAddress:{}, newVersion:{}",
        olds,
        oldVersion,
        toBeAdd,
        toBeRemove,
        storage.get().clientOffAddress.keySet(),
        storage.get().getVersion());
    return true;
  }

  protected static class ClientOffAddressStorage extends SystemDataStorage {
    final Map<String, AddressVersion> clientOffAddress;

    final AtomicReference<ClientOffTable> updating;

    public ClientOffAddressStorage(
        long version, Map<String, AddressVersion> clientOffAddress, ClientOffTable updating) {
      super(version);
      this.clientOffAddress = clientOffAddress;
      this.updating = new AtomicReference<>(updating);
    }
  }

  protected static class ClientOffAddressResp extends SystemDataStorage {
    final Map<String, AddressVersion> clientOffAddress;

    public ClientOffAddressResp(long version, Map<String, AddressVersion> clientOffAddress) {
      super(version);
      this.clientOffAddress = clientOffAddress;
    }
  }

  static final class ClientOffTable {
    final Set<String> addAddress;

    final Map<String, AddressVersion> adds;

    final Set<String> removes;

    public ClientOffTable(
        Set<String> addAddress, Map<String, AddressVersion> adds, Set<String> removes) {
      this.addAddress = addAddress;
      this.adds = adds;
      this.removes = removes;
    }
  }

  protected final class ClientManagerProcessor extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      processUpdating();
    }

    @Override
    public void waitingUnthrowable() {
      int clientManagerIntervalMillis = sessionServerConfig.getClientManagerIntervalMillis();
      ConcurrentUtils.sleepUninterruptibly(clientManagerIntervalMillis, TimeUnit.MILLISECONDS);
    }
  }

  boolean processUpdating() {
    final ClientOffTable table = storage.get().updating.getAndSet(null);
    if (table == null) {
      return true;
    }

    Set<String> addAddress = table.addAddress;
    Map<String, AddressVersion> adds = table.adds;
    Set<String> removes = table.removes;

    if (CollectionUtils.isEmpty(addAddress) && CollectionUtils.isEmpty(removes)) {
      return true;
    }

    if (CollectionUtils.isNotEmpty(addAddress)) {
      doTrafficOff(addAddress, adds);
    }

    if (CollectionUtils.isNotEmpty(removes)) {
      doTrafficOn(removes);
    }
    return true;
  }

  private void doTrafficOff(Set<String> ipSet, Map<String, AddressVersion> adds) {
    List<ConnectId> conIds = connectionsService.getIpConnects(ipSet);

    if (CollectionUtils.isEmpty(conIds)) {
      return;
    }
    LOGGER.info("clientOff conIds: {}", conIds.toString());

    Map<ConnectId, Long> connectIds = Maps.newHashMapWithExpectedSize(conIds.size());
    for (ConnectId conId : conIds) {
      AddressVersion addressVersion = adds.get(conId.getClientHostAddress());
      ParaCheckUtil.checkNotNull(addressVersion, "addressVersion");
      connectIds.put(conId, addressVersion.getVersion());
    }
    ParaCheckUtil.checkEquals(conIds.size(), connectIds.size(), "connectIds");
    sessionRegistry.clientOffWithTimestampCheck(connectIds);
  }

  private void doTrafficOn(Set<String> ipSet) {
    List<String> connections = connectionsService.closeIpConnects(Lists.newArrayList(ipSet));
    if (CollectionUtils.isNotEmpty(connections)) {
      LOGGER.info("clientOpen conIds: {}", connections);
    }
  }

  /**
   * Getter method for property <tt>clientOffAddress</tt>.
   *
   * @return property value of clientOffAddress
   */
  public Set<String> getClientOffAddress() {
    return storage.get().clientOffAddress.keySet();
  }


  public boolean contains(String address) {
    return storage.get().clientOffAddress.containsKey(address);
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   */
  @VisibleForTesting
  public FetchClientOffAddressService setSessionServerConfig(
      SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }

  /**
   * Setter method for property <tt>connectionsService</tt>.
   *
   * @param connectionsService value to be assigned to property connectionsService
   */
  @VisibleForTesting
  public FetchClientOffAddressService setConnectionsService(ConnectionsService connectionsService) {
    this.connectionsService = connectionsService;
    return this;
  }

  /**
   * Setter method for property <tt>sessionRegistry</tt>.
   *
   * @param sessionRegistry value to be assigned to property sessionRegistry
   */
  @VisibleForTesting
  public FetchClientOffAddressService setSessionRegistry(Registry sessionRegistry) {
    this.sessionRegistry = sessionRegistry;
    return this;
  }

  @VisibleForTesting
  public ClientOffAddressStorage getStorage() {
    return storage.get();
  }
}
