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
package com.alipay.sofa.registry.server.session.provideData;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.provideData.FetchClientOffAddressService.ClientOffAddressStorage;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: FetchClientOffAddressService.java, v 0.1 2021年05月16日 18:01 xiaojian.xj Exp $
 */
public class FetchClientOffAddressService
    extends AbstractFetchSystemPropertyService<ClientOffAddressStorage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchClientOffAddressService.class);

  private final ClientManagerProcessor clientManagerProcessor = new ClientManagerProcessor();

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private ConnectionsService connectionsService;

  @Autowired private Registry sessionRegistry;

  public FetchClientOffAddressService() {
    super(ValueConstants.CLIENT_OFF_ADDRESS_DATA_ID);
    storage.set(new ClientOffAddressStorage(INIT_VERSION, Sets.newHashSet(), null));
    ConcurrentUtils.createDaemonThread("ClientManagerProcessor", clientManagerProcessor).start();
  }

  @Override
  public boolean doProcess(ClientOffAddressStorage expect, ProvideData data) {
    if (expect.updating.get() != null) {
      return false;
    }
    Set<String> news = (Set<String>) data.getProvideData().getObject();
    Set<String> olds = expect.clientOffAddress;

    SetView toBeRemove = Sets.difference(olds, news);
    SetView toBeAdd = Sets.difference(news, olds);

    try {

      ClientOffAddressStorage update =
          new ClientOffAddressStorage(
              data.getVersion(), news, new ClientOffTable(toBeAdd, toBeRemove));
      if (!compareAndSet(expect, update)) {
        return false;
      }
    } catch (Throwable t) {
      LOGGER.error("update clientOffAddress:{} error.", data, t);
    }

    LOGGER.info(
        "olds clientOffAddress:{}, toBeAdd:{}, toBeRemove:{}, current clientOffAddress:{}",
        olds,
        toBeAdd,
        toBeRemove,
        news);
    return true;
  }

  protected class ClientOffAddressStorage
      extends AbstractFetchSystemPropertyService.SystemDataStorage {
    final Set<String> clientOffAddress;

    final AtomicReference<ClientOffTable> updating;

    public ClientOffAddressStorage(
        long version, Set<String> clientOffAddress, ClientOffTable updating) {
      super(version);
      this.clientOffAddress = clientOffAddress;
      this.updating = new AtomicReference<>(updating);
    }
  }

  final class ClientOffTable {
    final Set<String> adds;

    final Set<String> removes;

    public ClientOffTable(Set<String> adds, Set<String> removes) {
      this.adds = adds;
      this.removes = removes;
    }

    /**
     * Getter method for property <tt>adds</tt>.
     *
     * @return property value of adds
     */
    public Set<String> getAdds() {
      return adds;
    }

    /**
     * Getter method for property <tt>removes</tt>.
     *
     * @return property value of removes
     */
    public Set<String> getRemoves() {
      return removes;
    }
  }

  private final class ClientManagerProcessor extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      processUpdating();
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          sessionServerConfig.getClientManagerIntervalMillis(), TimeUnit.MILLISECONDS);
    }
  }

  boolean processUpdating() {
    final ClientOffTable table = storage.get().updating.getAndSet(null);
    if (table == null) {
      return true;
    }

    Set<String> adds = table.adds;
    Set<String> removes = table.removes;

    if (CollectionUtils.isEmpty(adds) && CollectionUtils.isEmpty(removes)) {
      return true;
    }

    if (CollectionUtils.isNotEmpty(adds)) {
      doTrafficOff(adds);
    }

    if (CollectionUtils.isNotEmpty(removes)) {
      doTrafficOn(removes);
    }
    return true;
  }

  private void doTrafficOff(Set<String> ipSet) {
    List<ConnectId> conIds = connectionsService.getIpConnects(ipSet);

    if (CollectionUtils.isNotEmpty(conIds)) {
      LOGGER.info("clientOff conIds: {}", conIds.toString());
    }
    sessionRegistry.clientOff(conIds);
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
    return storage.get().clientOffAddress;
  }
}
