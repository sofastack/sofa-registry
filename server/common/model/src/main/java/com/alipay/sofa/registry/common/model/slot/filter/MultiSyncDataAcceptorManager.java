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
package com.alipay.sofa.registry.common.model.slot.filter;

import com.alipay.sofa.registry.common.model.console.MultiSegmentSyncSwitch;
import com.alipay.sofa.registry.common.model.constants.MultiValueConstants;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : RemoteSyncSlotAcceptorManager.java, v 0.1 2022年05月13日 20:04 xiaojian.xj Exp $
 */
public class MultiSyncDataAcceptorManager implements Serializable {

  private static final long serialVersionUID = 5218066919220398566L;

  private Map<String, RemoteSyncDataAcceptorManager> remoteManagers = Maps.newConcurrentMap();

  public void updateFrom(Collection<MultiSegmentSyncSwitch> syncConfigs) {

    Map<String, RemoteSyncDataAcceptorManager> remoteManagers =
        Maps.newHashMapWithExpectedSize(syncConfigs.size());
    for (MultiSegmentSyncSwitch syncConfig : syncConfigs) {

      Set<SyncSlotAcceptor> acceptors = Sets.newConcurrentHashSet();
      acceptors.add(MultiValueConstants.DATUM_SYNCER_SOURCE_FILTER);
      acceptors.add(new SyncPublisherGroupAcceptor(syncConfig.getSynPublisherGroups()));
      acceptors.add(
          new SyncSlotDataInfoIdAcceptor(
              syncConfig.getSyncDataInfoIds(), syncConfig.getIgnoreDataInfoIds()));
      RemoteSyncDataAcceptorManager manager = new RemoteSyncDataAcceptorManager(acceptors);
      remoteManagers.put(syncConfig.getRemoteDataCenter(), manager);
    }

    synchronized (this) {
      this.remoteManagers = remoteManagers;
    }
  }

  public synchronized SyncSlotAcceptorManager getSyncSlotAcceptorManager(String dataCenter) {
    return remoteManagers.get(dataCenter);
  }

  public final class RemoteSyncDataAcceptorManager extends BaseSyncSlotAcceptorManager {

    public RemoteSyncDataAcceptorManager(Set<SyncSlotAcceptor> acceptors) {
      super(acceptors);
    }
  }
}
