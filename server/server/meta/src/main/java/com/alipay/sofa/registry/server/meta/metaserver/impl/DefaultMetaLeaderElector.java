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
import com.alipay.sofa.registry.store.api.elector.LeaderAware;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chen.zhu
 * <p>
 * Mar 10, 2021
 */
@Component
public class DefaultMetaLeaderElector extends AbstractLifecycleObservable implements MetaLeaderService, LeaderAware {

  private final Logger logger = LoggerFactory.getLogger(DefaultMetaLeaderElector.class);

  private final AtomicBoolean wasLeader = new AtomicBoolean(false);

  private final AtomicReference<LeaderState> leaderState = new AtomicReference<>();

  @Autowired
  private LeaderElector leaderElector;

  @Autowired
  private MetaServerConfig metaServerConfig;

  @Autowired(required = false)
  private List<MetaLeaderElectorListener> listeners;

  public DefaultMetaLeaderElector() {
  }

  public DefaultMetaLeaderElector(LeaderElector leaderElector,
                                  MetaServerConfig metaServerConfig,
                                  List<MetaLeaderElectorListener> listeners) {
    this.leaderElector = leaderElector;
    this.metaServerConfig = metaServerConfig;
    this.listeners = listeners;
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
  public boolean isWarmup() {
    return leaderState.get() != null
            && System.currentTimeMillis() - leaderState.get().getStartTime() > getWarmupMilli();
  }

  @Override
  public boolean amILeader() {
    return leaderElector.amILeader();
  }

  @Override
  public String getLeader() {
    return leaderElector.getLeader();
  }

  @Override
  public long getLeaderEpoch() {
    return leaderElector.getLeaderEpoch();
  }

  @Override
  public void leaderNotify() {
    if (wasLeader.compareAndSet(false, true)) {
      leaderState.set(new LeaderState(LeaderElector.ElectorRole.LEADER, System.currentTimeMillis()));
      becomeLeader();
    }
  }

  @Override
  public void followNotify() {
    if (wasLeader.compareAndSet(true, false)) {
      leaderState.set(new LeaderState(LeaderElector.ElectorRole.FOLLOWER, System.currentTimeMillis()));
      loseLeader();
    }
  }

  protected void becomeLeader() {
    if (logger.isInfoEnabled()) {
      logger.info("[becomeLeader] change from follower to elector");
    }
    if (listeners != null && !listeners.isEmpty()) {
      listeners.forEach(listener -> {
        listener.becomeLeader();
      });
    }
  }

  private long getWarmupMilli() {
    return metaServerConfig.getMetaLeaderWarmupMilli();
  }

  protected void loseLeader() {
    if (logger.isInfoEnabled()) {
      logger.info("[becomeFollow] change from elector to follower");
    }
    if (listeners != null && !listeners.isEmpty()) {
      listeners.forEach(listener -> {
        listener.loseLeader();
      });
    }
  }

  private static final class LeaderState {
    private final LeaderElector.ElectorRole state;
    private final long startTime;

    public LeaderState(LeaderElector.ElectorRole state, long startTime) {
      this.state = state;
      this.startTime = startTime;
    }

    public LeaderElector.ElectorRole getState() {
      return state;
    }

    public long getStartTime() {
      return startTime;
    }

    @Override
    public String toString() {
      return "LeaderState{" +
              "state=" + state +
              ", startTime=" + startTime +
              '}';
    }
  }
}
