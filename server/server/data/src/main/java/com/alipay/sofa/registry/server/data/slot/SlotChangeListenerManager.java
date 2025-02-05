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
package com.alipay.sofa.registry.server.data.slot;

import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author xiaojian.xj
 * @version : SlotChangeListenerManager.java, v 0.1 2022年05月31日 10:20 xiaojian.xj Exp $
 */
public class SlotChangeListenerManager {

  private final List<SlotChangeListener> localSlotChangeListeners = new ArrayList<>();

  private final List<SlotChangeListener> remoteSlotChangeListeners = new ArrayList<>();

  @Resource private DatumStorageDelegate datumStorageDelegate;

  @PostConstruct
  public void init() {
    localSlotChangeListeners.add(datumStorageDelegate.getSlotChangeListener(true));

    remoteSlotChangeListeners.add(datumStorageDelegate.getSlotChangeListener(false));
  }

  public List<SlotChangeListener> localUpdateListeners() {
    return Lists.newArrayList(localSlotChangeListeners);
  }

  public List<SlotChangeListener> remoteListeners() {
    return Lists.newArrayList(remoteSlotChangeListeners);
  }

  /**
   * Setter method for property <tt>datumStorageDelegate</tt>.
   *
   * @param datumStorageDelegate value to be assigned to property datumStorageDelegate
   */
  @VisibleForTesting
  public void setDatumStorageDelegate(DatumStorageDelegate datumStorageDelegate) {
    this.datumStorageDelegate = datumStorageDelegate;
    init();
  }
}
