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
package com.alipay.sofa.registry.server.session.store.engine;

import com.alipay.sofa.registry.common.model.store.StoreData;
import java.util.Collection;

/** A storage engine that supports aggregation by a specific key. */
public interface SlotStoreEngine<T extends StoreData<String>> extends StoreEngine<T> {
  /**
   * All StoreData belonging to the same slot.
   *
   * @param slotId slot id
   * @return StoreData belonging to the same slot
   */
  Collection<T> getSlotStoreData(int slotId);
}
