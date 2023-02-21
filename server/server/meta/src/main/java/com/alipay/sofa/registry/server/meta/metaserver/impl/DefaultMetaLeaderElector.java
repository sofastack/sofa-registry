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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.monitor.Metrics;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector.LeaderInfo;
import com.alipay.sofa.registry.store.api.elector.LeaderAware;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Mar 10, 2021
 */
@Component
public class DefaultMetaLeaderElector extends AbstractLifecycleObservable
    implements MetaLeaderService, LeaderAware {

  private final Logger logger = LoggerFactory.getLogger(DefaultMetaLeaderElector.class);

  private final AtomicBoolean wasLeader = new AtomicBoolean(false);

  private final AtomicReference<LeaderState> leaderState = new AtomicReference<>();

  private final List<MetaLeaderElectorListener> listeners = new CopyOnWriteArrayList<>();

  @Autowired private final LeaderElector leaderElector;

  @Autowired private final MetaServerConfig metaServerConfig;

  public DefaultMetaLeaderElector(LeaderElector leaderElector, MetaServerConfig metaServerConfig) {
    this.leaderElector = leaderElector;
    this.metaServerConfig = metaServerConfig;
  }

  public void registerListener(MetaLeaderElectorListener listener) {
    listeners.add(listener);
  }

  @PostConstruct
  public void postConstruct() throws Exception {
    LifecycleHelper.initializeIfPossible(this);
    LifecycleHelper.startIfPossible(this);
  }

  @Override
  protected void doInitialize() throws InitializeException {
    super.doInitialize();
    leaderElector.registerLeaderAware(this);
  }

  @Override
  public boolean isWarmuped() {
    return leaderState.get() != null
        && System.currentTimeMillis() - leaderState.get().getStartTime() > getWarmupMilli();
  }

  @Override
  public boolean amILeader() {
    return leaderElector.amILeader();
  }

  @Override
  public String getLeader() {
    return leaderElector.getLeaderInfo().getLeader();
  }

  @Override
  public long getLeaderEpoch() {
    return leaderElector.getLeaderInfo().getEpoch();
  }

  @Override
  public LeaderInfo getLeaderInfo() {
    return leaderElector.getLeaderInfo();
  }

  @Override
  public void leaderNotify() {
    if (wasLeader.compareAndSet(false, true)) {
      leaderState.set(
          new LeaderState(LeaderElector.ElectorRole.LEADER, System.currentTimeMillis()));
      logger.info("[becomeLeader] change from follower to elector, {}", this.leaderState.get());
      listeners.forEach(MetaLeaderElectorListener::becomeLeader);
    }
  }

  @Override
  public void followNotify() {
    if (wasLeader.compareAndSet(true, false)) {
      leaderState.set(
          new LeaderState(LeaderElector.ElectorRole.FOLLOWER, System.currentTimeMillis()));
      logger.info("[becomeFollow] change from elector to follower, {}", this.leaderState.get());
      // not leader, clear the leader/follower metrics
      Metrics.DataSlot.clearLeaderNumbers();
      Metrics.DataSlot.clearFollowerNumbers();
      listeners.forEach(MetaLeaderElectorListener::loseLeader);
    }
  }

  private long getWarmupMilli() {
    return metaServerConfig.getMetaLeaderWarmupMillis();
  }

  private static final class LeaderState {
    private final LeaderElector.ElectorRole state;
    private final long startTime;

    public LeaderState(LeaderElector.ElectorRole state, long startTime) {
      this.state = state;
      this.startTime = startTime;
    }

    public long getStartTime() {
      return startTime;
    }

    @Override
    public String toString() {
      return "LeaderState{" + "state=" + state + ", startTime=" + startTime + '}';
    }
  }
}
