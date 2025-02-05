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

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/** */
public class PublisherStoreTest {

  @Test
  public void test() {
    String dataInfoId = "dataInfoId";
    String registerId = "registerId";

    String dataInfoId00 = "dataInfoId00";
    String registerId00 = "registerId00";

    SlotTableCache slotTableCache = Mockito.mock(SlotTableCache.class);
    Mockito.when(slotTableCache.slotOf(dataInfoId)).thenReturn(0);
    Mockito.when(slotTableCache.slotOf(dataInfoId00)).thenReturn(1);
    PublisherStoreImpl publisherStore = new PublisherStoreImpl(slotTableCache);

    long time = System.currentTimeMillis();
    Publisher publisher = new Publisher();
    publisher.setDataInfoId(dataInfoId);
    publisher.setRegisterId(registerId);
    publisher.setVersion(1L);
    publisher.setClientRegisterTimestamp(time);
    publisher.setSourceAddress(new URL("192.168.1.2", 9000));
    publisher.setTargetAddress(new URL("127.0.0.1", 34567));

    Publisher publisher00 = new Publisher();
    publisher00.setDataInfoId(dataInfoId00);
    publisher00.setRegisterId(registerId00);
    publisher00.setVersion(1L);
    publisher00.setClientRegisterTimestamp(time);
    publisher00.setSourceAddress(new URL("192.168.1.2", 9000));
    publisher00.setTargetAddress(new URL("127.0.0.1", 34567));

    publisherStore.add(publisher);
    publisherStore.add(publisher00);

    Collection<Publisher> slotPublishers = publisherStore.getBySlotId(1);
    Assert.assertEquals(1, slotPublishers.size());
    Publisher publisher1 = slotPublishers.stream().findAny().get();
    Assert.assertEquals(publisher00, publisher1);
  }
}
