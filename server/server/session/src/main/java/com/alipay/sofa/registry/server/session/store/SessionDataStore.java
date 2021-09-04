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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: SessionDataStore.java, v 0.1 2017-12-01 18:14 shangyu.wh Exp $
 */
public class SessionDataStore extends AbstractDataManager<Publisher> implements DataStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionDataStore.class);

  @Autowired SlotTableCache slotTableCache;

  private final SlotStore<Publisher> store = new SlotStore<>(this::slotOf);

  public SessionDataStore() {
    super(LOGGER);
  }

  @Override
  public boolean add(Publisher publisher) {
    ParaCheckUtil.checkNotNull(publisher.getVersion(), "publisher.version");
    ParaCheckUtil.checkNotNull(publisher.getRegisterTimestamp(), "publisher.registerTimestamp");

    PublisherUtils.internPublisher(publisher);
    Tuple<Publisher, Boolean> ret = addData(publisher);
    return ret.o2;
  }

  @Override
  public Map<String, Map<String, Publisher>> getDataInfoIdPublishers(int slotId) {
    return store.copyMap(slotId);
  }

  @Override
  protected Store<Publisher> getStore() {
    return store;
  }

  @VisibleForTesting
  public void setSlotTableCache(SlotTableCache slotTableCache) {
    this.slotTableCache = slotTableCache;
  }

  protected int slotOf(String dataInfoId) {
    return slotTableCache.slotOf(dataInfoId);
  }
}
