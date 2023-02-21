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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : BaseSyncSlotAcceptorManager.java, v 0.1 2022年05月24日 21:41 xiaojian.xj Exp $
 */
public class BaseSyncSlotAcceptorManager implements SyncSlotAcceptorManager {

  protected final Set<SyncSlotAcceptor> acceptors;

  public BaseSyncSlotAcceptorManager(Set<SyncSlotAcceptor> acceptors) {
    this.acceptors = acceptors;
  }

  public boolean accept(SyncAcceptorRequest request) {

    for (SyncSlotAcceptor acceptor :
        Optional.ofNullable(acceptors).orElse(Collections.emptySet())) {
      if (acceptor.filterOut(request)) {
        return false;
      }
    }
    for (SyncSlotAcceptor acceptor :
        Optional.ofNullable(acceptors).orElse(Collections.emptySet())) {
      if (acceptor.accept(request)) {
        return true;
      }
    }
    return false;
  }
}
