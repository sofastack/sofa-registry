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

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.CLIENT_OFF;
import static com.alipay.sofa.registry.server.session.registry.ClientManagerMetric.ADDRESS_LOAD_DELAY_HISTOGRAM;
import static com.alipay.sofa.registry.server.session.registry.ClientManagerMetric.CLIENT_OFF_GAUGE;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.loggers.Loggers;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService.ClientOffAddressResp;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService.ClientOffAddressStorage;
import com.alipay.sofa.registry.server.session.registry.ClientManagerMetric;
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

  private static final Logger LOGGER = Loggers.CLIENT_OFF_LOG;

  protected final ClientManagerProcessor clientManagerProcessor = new ClientManagerProcessor();
  protected final ClientOpenFailWatchDog clientOpenFailWatchDog = new ClientOpenFailWatchDog();

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private ConnectionsService connectionsService;

  @Autowired private Registry sessionRegistry;

  @Autowired private ClientManagerAddressRepository clientManagerAddressRepository;

  public FetchClientOffAddressService() {
    super(
        ValueConstants.CLIENT_OFF_ADDRESS_DATA_ID,
        new ClientOffAddressStorage(
            INIT_VERSION, Collections.emptyMap(), Collections.emptySet(), null));
  }

  @PostConstruct
  public void postConstruct() {
    CLIENT_OFF_GAUGE.func(() -> storage.get().clientOffAddress.size());
    ConcurrentUtils.createDaemonThread("ClientManagerProcessor", clientManagerProcessor).start();
    ConcurrentUtils.createDaemonThread("clientOpenFailWatchDog", clientOpenFailWatchDog).start();
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getClientManagerAddressIntervalMillis();
  }

  @Override
  protected ClientOffAddressResp fetchFromPersistence() {
    ClientManagerAddress clientManagerAddress = clientManagerAddressRepository.queryClientOffData();
    return new ClientOffAddressResp(
        clientManagerAddress.getVersion(),
        clientManagerAddress.getClientOffAddress(),
        clientManagerAddress.getReduces());
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
    Set<String> toBeClientOpen = Sets.difference(toBeRemove, data.reduces);
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
              data.reduces,
              new ClientOffTable(toBeAdd, toBeRemove, toBeClientOpen));
      if (!compareAndSet(expect, update)) {
        LOGGER.error("update clientOffAddress:{} fail.", update.getVersion());
        return false;
      }
    } catch (Throwable t) {
      LOGGER.error("update clientOffAddress:{} error.", data, t);
    }

    afterPropertySet(oldVersion, adds);
    LOGGER.info(
        "olds clientOffAddress:{}, oldVersion:{}, toBeAdd.size:{}, toBeAdd:{}, toBeClientOpen.size:{}, toBeClientOpen:{}, current clientOffAddress:{}, newVersion:{}",
        olds,
        oldVersion,
        toBeAdd.size(),
        toBeAdd,
        toBeClientOpen.size(),
        toBeClientOpen,
        storage.get().clientOffAddress.keySet(),
        storage.get().getVersion());
    return true;
  }

  private void afterPropertySet(long oldVersion, Map<String, AddressVersion> adds) {
    if (oldVersion == INIT_VERSION) {
      return;
    }
    long load = System.currentTimeMillis();
    for (AddressVersion value : adds.values()) {
      long cost = load - value.getVersion();
      ADDRESS_LOAD_DELAY_HISTOGRAM.observe(cost);
      LOGGER.info(
          "[LoadDelay]address:{}, version:{}, loaded:{}, cost:{}",
          value.getAddress(),
          value.getVersion(),
          load,
          cost);
    }
  }

  protected static class ClientOffAddressStorage extends SystemDataStorage {
    final Map<String, AddressVersion> clientOffAddress;

    final Set<String> reduces;

    final AtomicReference<ClientOffTable> updating;

    public ClientOffAddressStorage(
        long version,
        Map<String, AddressVersion> clientOffAddress,
        Set<String> reduces,
        ClientOffTable updating) {
      super(version);
      this.clientOffAddress = clientOffAddress;
      this.reduces = reduces;
      this.updating = new AtomicReference<>(updating);
    }
  }

  protected static class ClientOffAddressResp extends SystemDataStorage {
    final Map<String, AddressVersion> clientOffAddress;

    final Set<String> reduces;

    public ClientOffAddressResp(
        long version, Map<String, AddressVersion> clientOffAddress, Set<String> reduces) {
      super(version);
      this.clientOffAddress = clientOffAddress;
      this.reduces = reduces;
    }
  }

  static final class ClientOffTable {
    final Set<String> addAddress;

    final Set<String> removes;

    final Set<String> clientOpens;

    public ClientOffTable(Set<String> addAddress, Set<String> removes, Set<String> clientOpens) {
      this.addAddress = addAddress;
      this.removes = removes;
      this.clientOpens = clientOpens;
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
    Set<String> removes = table.removes;
    Set<String> clientOpens = table.clientOpens;

    if (CollectionUtils.isEmpty(addAddress)
        && CollectionUtils.isEmpty(removes)
        && CollectionUtils.isEmpty(clientOpens)) {
      return true;
    }

    if (CollectionUtils.isNotEmpty(addAddress)) {
      doTrafficOff(addAddress);
    }

    if (CollectionUtils.isNotEmpty(removes)) {
      unMarkChannel(removes);
    }

    if (CollectionUtils.isNotEmpty(clientOpens)) {
      doTrafficOn(clientOpens);
    }
    return true;
  }

  private void unMarkChannel(Set<String> ipSet) {
    connectionsService.markChannelAndGetIpConnects(ipSet, CLIENT_OFF, null);
  }

  private void doTrafficOff(Set<String> ipSet) {
    List<ConnectId> conIds =
        connectionsService.markChannelAndGetIpConnects(ipSet, CLIENT_OFF, Boolean.TRUE);

    if (CollectionUtils.isEmpty(conIds)) {
      return;
    }
    LOGGER.info("clientOff conIds: {}", conIds.toString());
    sessionRegistry.clientOff(conIds);
  }

  private void doTrafficOn(Set<String> ipSet) {
    List<String> connections = connectionsService.closeIpConnects(Lists.newArrayList(ipSet));
    if (CollectionUtils.isNotEmpty(connections)) {
      ClientManagerMetric.CLIENT_OPEN_COUNTER.inc(connections.size());
      LOGGER.info("clientOpen conIds: {}", connections);
    }
  }

  protected final class ClientOpenFailWatchDog extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      processClientOpen();
    }

    @Override
    public void waitingUnthrowable() {
      int clientOpenIntervalSecs = sessionServerConfig.getClientOpenIntervalSecs();
      ConcurrentUtils.sleepUninterruptibly(clientOpenIntervalSecs, TimeUnit.SECONDS);
    }
  }

  protected void processClientOpen() {
    List<Channel> channels = connectionsService.getAllChannel();
    ClientOffAddressStorage storage = this.storage.get();
    Map<String, AddressVersion> clientOffAddress = storage.clientOffAddress;
    Set<String> reduces = storage.reduces;

    Set<String> retryClientOpen = Sets.newHashSetWithExpectedSize(8);
    for (Channel channel : channels) {
      String key = NetUtil.toAddressString(channel.getRemoteAddress());
      String ip = connectionsService.getIpFromConnectId(key);

      BoltChannel boltChannel = (BoltChannel) channel;
      Object value = boltChannel.getConnAttribute(CLIENT_OFF);
      if (Boolean.TRUE.equals(value) && !clientOffAddress.containsKey(ip)) {

        if (reduces.contains(ip)) {
          unMarkChannel(Collections.singleton(ip));
        } else {
          LOGGER.warn("[ClientOpenFail] ip:{} client open fail.", ip);
          retryClientOpen.add(ip);
        }
      }
    }

    if (CollectionUtils.isNotEmpty(retryClientOpen)) {
      LOGGER.info("[ClientOpenRetry] ips:{} retry client open.", retryClientOpen);
      doTrafficOn(retryClientOpen);
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

  public AddressVersion getAddress(String address) {
    return storage.get().clientOffAddress.get(address);
  }

  public long lastLoadVersion() {
    return storage.get().getVersion();
  }

  public void wakeup() {
    watchDog.wakeup();
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
   * @return FetchClientOffAddressService
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
   * @return FetchClientOffAddressService
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
