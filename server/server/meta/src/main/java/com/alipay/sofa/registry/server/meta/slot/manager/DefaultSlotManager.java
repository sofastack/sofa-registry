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
package com.alipay.sofa.registry.server.meta.slot.manager;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.UnblockingObserver;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.remoting.notifier.Notifier;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Dec 02, 2020
 */
@Component
public class DefaultSlotManager extends SimpleSlotManager implements SlotManager {

  @Autowired(required = false)
  private List<Notifier> notifiers;

  @Autowired private MetaLeaderService metaLeaderService;

  public DefaultSlotManager() {}

  public DefaultSlotManager(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
  }

  @PostConstruct
  public void postConstruct() throws Exception {
    LifecycleHelper.initializeIfPossible(this);
  }

  @Override
  protected void doInitialize() throws InitializeException {
    super.doInitialize();
    addObserver(new SlotTableChangeNotification());
  }

  @Override
  public boolean refresh(SlotTable slotTable) {
    // if we are not leader, could not refresh table
    // this maybe happens in fgc:
    // before arrange we are leader, but calc takes too much time
    if (!metaLeaderService.amILeader()) {
      throw new IllegalStateException(
          "not leader, concurrent leader is:" + metaLeaderService.getLeader());
    }
    if (super.refresh(slotTable)) {
      notifyObservers(slotTable);
      return true;
    }
    return false;
  }

  private final class SlotTableChangeNotification implements UnblockingObserver {

    @Override
    public void update(Observable source, Object message) {
      if (message instanceof SlotTable) {
        if (notifiers == null || notifiers.isEmpty()) {
          return;
        }
        notifiers.forEach(
            notifier -> {
              try {
                notifier.notifySlotTableChange((SlotTable) message);
              } catch (Throwable th) {
                logger.error("[notify] notifier [{}]", notifier.getClass().getSimpleName(), th);
              }
            });
      }
    }
  }

  @VisibleForTesting
  public SimpleSlotManager setNotifiers(List<Notifier> notifiers) {
    this.notifiers = notifiers;
    return this;
  }
}
